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
import java.net.UnknownHostException;
import java.util.ArrayList;
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
	private final String host;
	private final int port;
	private final long hostid;
	private ArrayList<Subscription> subscriptions;
	private Socket sock = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private boolean running = false;
	private boolean error   = false;
	private Integer catchUpTimeOut = 0;
	private long lastStop = 0;
	private Thread deaconThread = null;
	
	/**
	 * Runnable to execute Meteor HTTP GETs and collect the results;
	 * this runnable implements the server interaction mode
	 */
	private class DeaconRunnable implements Runnable {
		
		@Override
		public void run() {
			String response = "";
			while (running){ 
				
				try {
					sock = new Socket(host, port);
					out  = new PrintWriter(sock.getOutputStream(), true);
					in   = new BufferedReader(new InputStreamReader(sock.getInputStream()), 1024);
					if(error){
						notifyObserversReconnect();
						error = false;
					}
					
				} catch (UnknownHostException e) {
					error = true;
					notifyObserversError(new DeaconError(e));
					stop();
				} catch (IOException e) {
					error = true;
					notifyObserversError(new DeaconError(e));
					stop();
				}
				
				if(!error){
					// Construct the subscription string
					String serverstring = "GET /push/" + hostid + "/longpoll";
					for(Subscription sub : subscriptions) {
						serverstring += "/" + sub.channel;
						if(sub.backtrack > 0 && running) {
							serverstring += ".b" + sub.backtrack;
							// Backtrack retrieval is one-time-only; reset to zero after backtrack request made
							sub.backtrack = 0;
						}
						else if(sub.catchup > 0 && running) {
							// System.out.println("Found catchup="+sub.catchup+" for chan="+sub.channel);
							serverstring += ".r" + sub.catchup;
							// Catchup retrieval is one-time-only; reset to zero after catchup request made
							sub.catchup = 0;
						}
					}
					serverstring += " HTTP/1.1\r\n\r\n";
					
					// Subscribe to the channel
					out.println(serverstring);
					
					try {
						// Wait for a response from the channel
						while( (response=in.readLine()) != null && running) {
							//System.out.println("Got response: " + response);
							socketLine(response);
						}
						out.close();
						in.close();
						sock.close();
					}
					catch(IOException e){
						error = true;
						notifyObserversError(new DeaconError(e));
						stop();
					}
				}
					
			}
			
		}
	}
	
	/**
	 * Creates a new DeaconService
	 * @param host Meteor server to which this client should connect
	 * @param port TCP port on Meteor server that is awaiting connections
	 * @throws UnknownHostException if host is unreachable
	 * @throws IOException if connection cannot be established
	 * @throws Exception if port value is invalid
	 */
	public DeaconService(String host, Integer port) throws UnknownHostException, IOException, Exception{
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
	 * Will return "0" if no timeout.
	 * @return the currently-configured timeout in seconds, or 0 if catchup is disabled
	 */
	public int catchUpTimeOut() {
		return catchUpTimeOut;
	}
	
	/**
	 * Called when a new line is received from the Meteor server; enables standalone testing.
	 * Intended to be overridden by Deacon wrapper class with thread-safe communication mechanism within Android.
	 * @param line The received line to be parsed
	 */
	protected void socketLine(final String line) {
		parse(line);
	}
	
	/**
	 * Adds a subscription in the DeaconService to the specified Meteor server channel
	 * @param chan The channel name on the Meteor server
	 * @param backtrack The number of previously-pushed messages to retrieve upon subscribing
	 */
	public synchronized void joinChannel(final String chan, Integer backtrack){
		// Bounds-check backtrack; should be positive integer
		if(backtrack < 0) backtrack = 0;
		
		//System.out.println("Joining channel: " + chan + " with backtrack=" + backtrack);
		Subscription sub = new Subscription();
		sub.channel = chan;
		sub.backtrack = backtrack;
		this.subscriptions.add(sub);
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
		for(Subscription sub : subscriptions){
			if(sub.channel.equals(chan)) {
				this.subscriptions.remove(sub);
			}
		}
	}
	
	/**
	 * Initiates or re-opens the connection with the Meteor server
	 * @throws Exception if Deacon is already running when the start() method is called
	 */
	public void start() throws Exception{
		if((deaconThread != null && deaconThread.isAlive()) || running) {
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
		this.running = true;
		deaconThread = new Thread(new DeaconRunnable());
		deaconThread.start();
	}
	
	/**
	 * Closes the connection to the Meteor server; Takes effect after the present polling interval terminates.
	 */
	public void stop(){
		this.running = false;
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
