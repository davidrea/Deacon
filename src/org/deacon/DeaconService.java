/*
 * COPYRIGHT (c) 2010 The Members of the Deacon Project <http://deacon.daverea.com/core-team/>
 * 
 * This file is part of Deacon
 * 
 * Deacon is free software: you can redistribute it and/or modify it under the terms of the GNU 
 * Lesser General Public License as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * 
 * Deacon is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License and the GNU Lesser General 
 * Public License along with Deacon.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.deacon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements the core operation of the Deacon Meteor client. 
 */
public class DeaconService extends DeaconObservable {
	
	/**
	 * Class to encapsulate a Meteor channel subscription as an object
	 */
	private class Subscription {
		public String channel="";
		public Integer backtrack=0;
		public Integer lastMessageReceived=0;
		public Integer catchup = 0;
		
		public String toString() {
			return "SUB{chan="+channel+"/backtrack="+backtrack+"/LMR="+lastMessageReceived+"}";
		}
	}

	/**
	 * Member attributes
	 */
	
	// Configuration
	private final String host;
	private final int port;
	private final long hostid;
	private Integer catchUpTimeOut = 0;
	protected boolean autoRestart = true;
	private Integer pingTimeout = 0;
	private Integer maxRetries = 10;
	
	// State
	private ArrayList<Subscription> subscriptions;
	private boolean running = false;
	private boolean connected = false;
	private long lastStop = 0;
	private Thread deaconThread = null;

	// Resources
	private Socket sock = null;
	private class DeaconRunnable implements Runnable {
		
		private PrintWriter out = null;
		private InputStreamReader stream = null;
		private BufferedReader in = null;
		private boolean error = false;
		private int retries = 0;
		
		@Override
		public void run() {
			String response = "";
			while (running){
				
				// Only try to reconnect up to the max retry count
				if(retries > maxRetries) {
					notifyObserversError(new DeaconError(new Exception("MaxRetries"), DeaconErrorType.TimeoutPermanent));
					stop();
					break;
				}
				
				// Perform crude (linear) back-off
				if(retries > 0) {
					try {
						Thread.sleep(1000*10*retries);
					} catch (InterruptedException e) {
						// If can't back-off, stop trying
						notifyObserversError(new DeaconError(e, DeaconErrorType.BackoffFailed));
						running = false;
						break;
					}
				}
				
				// Connect
				try {
					++retries;
					sock = new Socket(host, port);
					out  = new PrintWriter(sock.getOutputStream(), true);
					stream = new InputStreamReader(sock.getInputStream());
					in   = new BufferedReader(stream, 1024);
					if(error){
						notifyObserversReconnect();
						error = false;
					}
					
					// Set a timeout on the socket
					// This prevents getting stuck in readline() if the pipe breaks
					sock.setSoTimeout(pingTimeout * 1000);
					
					retries = 0;
					
				} catch (UnknownHostException e) {
					error = true;
					notifyObserversError(new DeaconError(e, DeaconErrorType.UnknownHostError));
					stop();
				} catch (IOException e) {
					error = true;
					notifyObserversDisconnect(new DeaconError(e, DeaconErrorType.ConnectionError));
					stop();
				}
				
				if(!error && running){
					// Construct the subscription string
					String serverstring = "GET /push/" + hostid + "/longpoll";
					for(Subscription sub : subscriptions) {
						serverstring += "/" + sub.channel;
						if(sub.backtrack > 0 && running) {
							serverstring += ".b" + sub.backtrack;
							// Backtrack retrieval is one-time-only; 
							//reset to zero after backtrack request made
							sub.backtrack = 0;
						}
						else if(sub.catchup > 0 && running) {
							serverstring += ".r" + sub.catchup;
							// Catchup retrieval is one-time-only; reset to zero after catchup request made
							sub.catchup = 0;
						}
					}
					serverstring += " HTTP/1.1\r\n\r\n";
					
					// Join/re-join to the channel
					out.println(serverstring);
					connected = true;
					
					try {
						// Wait for a response from the channel
						while(running && (response=in.readLine()) != null) {
							// Got a response
							//killConnection.cancel();
							parse(response);
						}
						out.close();
						in.close();
						sock.close();
					}
					catch(IOException e) {
						error = true;
						if(e instanceof SocketTimeoutException) {
							notifyObserversError(new DeaconError(e, DeaconErrorType.TimeoutRetrying));
						}
						else if(e instanceof SocketException) {
							notifyObserversDisconnect(new DeaconError(e));
							// This exception is thrown by sock.close(); already in stop()
						}
						else {
							notifyObserversError(new DeaconError(e));
							stop();
						}
					}
				}
				else {
					// An error was encountered when trying to connect
					connected = false;
				}	
			}
		}
		// Return --> Thread terminates
	}
	
	/**
	 * Creates a new DeaconService
	 * @param host Meteor server to which this client should connect
	 * @param port TCP port on Meteor server that is awaiting connections
	 * @throws UnknownHostException if host is unreachable
	 * @throws IOException if connection cannot be established
	 * @throws Exception if port value is invalid
	 */
	public DeaconService(String host, Integer port) throws UnknownHostException, IOException, Exception {
		// Bounds-check port; should be positive integer
		if(port < 0) throw new Exception("Cannot instantiate Deacon with negative port value.");
		this.host = host;
		this.port = port;
		this.hostid = System.currentTimeMillis();
		this.subscriptions = new ArrayList<Subscription>();
	}
	
	/**
	 * Set the timeout after which Deacon will no longer try to retrieve pushes missed while shut down
	 * (This applies to all subscriptions)
	 * @param seconds The timeout in seconds (0 = no timeout)
	 */
	public void catchUpTimeOut(final Integer seconds) {
		this.catchUpTimeOut = (seconds > 0) ? seconds : 0;	// Should be positive or zero
	}
	
	/**
	 * Get the timeout after which Deacon will no longer try to retrieve pushes missed while shut down
	 * (This applies to all subscriptions)
	 * @return the currently-configured timeout in seconds, or 0 if catchup is disabled
	 */
	public int catchUpTimeOut() {
		return catchUpTimeOut;
	}
	
	/**
	 * Set the timeout before which a Meteor ping must be heard
	 * <p>If this function is called after subscribing to a channel, the ping configuration will not
	 * take effect until <strong>after</strong> the next push or Meteor ping message is received.</p>
	 * @param seconds The timeout in seconds (0 = no timeout)
	 */
	public void pingTimeout(final Integer seconds) {
		pingTimeout = seconds;
	}
	
	/**
	 * Get the timeout before hich a Meteor ping must be heard
	 * @return the currently-configured timeout in seconds, or 0 if no timeout is configured
	 */
	public int pingTimeout() {
		return pingTimeout;
	}
	
	/**
	 * Adds a subscription in the DeaconService to the specified Meteor server channel
	 * @param chan The channel name on the Meteor server
	 * @param backtrack The number of previously-pushed messages to retrieve upon subscribing
	 */
	public synchronized void joinChannel(final String chan, Integer backtrack){
		// Bounds-check backtrack; should be positive integer
		if(backtrack < 0) backtrack = 0;
		
		// Check to make sure channel isn't already subscribed
		boolean alreadySubscribed = false;
		for(Subscription sub : this.subscriptions) {
			if(sub.channel.equals(chan)) alreadySubscribed = true;
		}
		
		// Record the new subscription
		if(alreadySubscribed == false){
			Subscription sub = new Subscription();
			sub.channel = chan;
			sub.backtrack = backtrack;
			this.subscriptions.add(sub);
		}
	}
	
	/**
	 * Checks to see if the DeaconService is subscribed to the specified channel
	 * @param chan The channel to check
	 * @return a Boolean, true if the specified channel is subscribed
	 */
	public synchronized Boolean checkChannel(final String chan){
		for(Subscription s : subscriptions){
			if(s.channel.equals(chan)) return true;
		}
		return false;
	}
	
	/**
	 * Unsubscribes this DeaconService from the specified Meteor channel; Takes effect after the present polling interval terminates.
	 * @param chan The channel name on the Meteor server
	 */
	public synchronized void leaveChannel(final String chan) {
		Subscription removeMe = null;
		for(Subscription sub : subscriptions){
			if(chan.equals(sub.channel)) removeMe = sub;
		}
		if(removeMe != null)
		{
			subscriptions.remove(removeMe);
		}
	}
	
	/**
	 * Initiates or re-opens the connection with the Meteor server
	 * @throws Exception if Deacon is already running when the start() method is called
	 */
	public void start() throws Exception {
		if(running == true) {
			throw new Exception("Deacon is already running!");
		}
		// Check to see if a timeout is set and it is expired
		if((this.catchUpTimeOut != 0) && (this.lastStop != 0)) {
			// Deacon is "resuming" from a previous stop
			long timedelta = System.currentTimeMillis() - lastStop;
			if (timedelta > (((long)this.catchUpTimeOut) * 1000)) {
				// We're past the timeout; zero the catchup parameters of all subscriptions
				for(Subscription sub : subscriptions) {
					sub.catchup = 0;
				}
			}
		}
		// Start the client
		deaconThread = new Thread(new DeaconRunnable());
		deaconThread.start();
		this.running = true;
	}
	
	/**
	 * Closes the connection to the Meteor server; Takes effect after the present polling interval terminates.
	 */
	public void stop(){
		this.running = false;
		if(sock != null && sock.isConnected()) {
			try {
				sock.close();
			} catch (IOException e) {
				// Unable to close socket?!
				// In this case, don't need to take any particular action;
				// The socket will eventually time out and running=false will
				// cause the runnable's run() method to return.
			}
		}
		// Set up each subscription to automatically catch itself up if Deacon is restarted
		for(Subscription sub : this.subscriptions) {
			if(sub.lastMessageReceived != 0) {
				sub.catchup = sub.lastMessageReceived + 1;
			}
		}
		this.lastStop = System.currentTimeMillis();
	}
	
	/**
	 * Returns a description of the DeaconService
	 */
	public String toString(){
		return "Deacon @ " + host + ":" + port;
	}

	/**
	 * Checks the status of the DeaconService
	 * @return true if DeaconService is running; false if DeaconService is stopped
	 */
	public boolean isRunning() {
		return this.running;
	}
	
	public boolean isConnected() {
		return this.connected;
	}
	
	/**
	 * Configures auto-restart behavior (i.e. should Deacon automatically 
	 * restart after network connectivity has been lost and restored?)
	 * @param restart
	 */
	public void setAutoRestart(boolean restart)
	{
		this.autoRestart = restart;
	}
	
	/**
	 * Parses incoming Meteor commands (in the form of messages returned from the server) and acts on them
	 * 
	 * <!--In lieu of outside protocol documentation, Meteor message format is captured here:
	 * 		HeaderTemplate HTTP/1.1 ~status~\r\nChannel(~channelinfo~)\r\n
	 * 		MessageTemplate m.p.<~id~>."~channel~"."{[~text~]}"\r\n
	 * 		ChannelInfoTemplate "~channel~".~lastMsgID~
	 * 		PingMessage m.p
	 * 		SubscriberShutdownMsg m.sd -->
	 * <p>TODO - details of the Deacon-specific Meteor configuration should be moved into the Deacon Wiki
	 * 
	 * @param meteorMessage The string received from the Meteor server, to be parsed
	 */
	protected synchronized void parse(final String meteorMessage) {
		// TODO This is probably well-suited to a command pattern
		// Tried to implement this but figured we should get it working for POC first, then refactor
		// Regex match "m.*" portion of message
		Pattern p = Pattern.compile("m\\.(.*)");
		Matcher m = p.matcher(meteorMessage);
		Integer pass = 0;
		if(m.find()) {
			while(pass<=m.groupCount()) {
				// Split match push notification messages
				String thisgroup = m.group(pass).trim();
				if(thisgroup.split("\\.")[0].equals("p")) {
					// Regex extract channel and payload
					Pattern message = Pattern.compile("p\\.<(\\d*)>\\.\"(.*)\"\\.\"\\{\\[(.*)\\]\\}\"");
					Matcher parameters = message.matcher(thisgroup);
					if(parameters.find()) {
						// Create DeaconResponse object for parsed push notification
						// TODO Pull parameters out into final local variables
						// TODO Assumes Meteor will only push from subscribed channels; should check incoming messages against subscription list
						notifyObservers(new DeaconResponse(parameters.group(2), parameters.group(3)));
						// Update serial numbers of last messages received in subscription list (for catchup)
						for(Subscription sub : subscriptions) {
							if(sub.channel.equals(parameters.group(2))) {
								sub.lastMessageReceived = Integer.parseInt(parameters.group(1));
								//System.out.println(sub.toString());
							}
						}
					}
				}
				pass++;
			}
		}
	}

}
