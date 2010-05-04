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

public class DeaconService extends DeaconObservable {

	private final String host;
	private final int port;
	private final long hostid;
	
	private ArrayList<String> subscriptions;
	private Socket sock = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private boolean running=false;
	
	/**
	 * Thread to execute Meteor HTTP GETs and collect the results;
	 * this thread implements the server interaction mode
	 */
	private Thread deaconThread = new Thread(new Runnable() {
		
		@Override
		public void run() {
			String response = "";
			while (running){ 
				
				try {
					sock = new Socket(host, port);
//					System.out.println("Opened socket connection");
					out  = new PrintWriter(sock.getOutputStream(), true);
					in   = new BufferedReader(new InputStreamReader(sock.getInputStream()), 1024);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// Construct the subscription string
				String serverstring = "GET /push/" + hostid + "/longpoll";
				for(String channel : subscriptions) {
					serverstring += "/" + channel;
				}
				serverstring += " HTTP/1.1\r\n\r\n";
				
				// Subscribe to the channel
//				System.out.println("Server string: " + serverstring);
				out.println(serverstring);
				
				try {
					// Wait for a response from the channel
					while( (response=in.readLine()) != null && running) {
//						System.out.println("Got response: " + response);
						socketLine(response);
					}
					out.close();
					in.close();
					sock.close();
				}
				catch(IOException e){
					e.printStackTrace();
				}
					
			}
			
		}
	});
	
	/**
	 * DeaconService class constructor
	 * @param String host Meteor server to which this client should connect
	 * @param int port TCP port on Meteor server that is awaiting connections
	 * @throws UnknownHostException if host is unreachable
	 * @throws IOException if connection cannot be established
	 */
	public DeaconService(String host, int port) throws UnknownHostException, IOException{
		this.host = host;
		this.port = port;
		this.hostid = System.currentTimeMillis();
		this.subscriptions = new ArrayList<String>();
	}
	
	/**
	 * Called when a new line is received from the Meteor server; enables standalone testing
	 * Overridden by Android-specific wrapper class
	 * @param line The received line to be parsed
	 */
	protected void socketLine(String line) {
		parse(line);
	}
	
	/**
	 * Adds a subscription in the DeaconService to the specified Meteor server channel
	 * @param String chan The channel name on the Meteor server
	 * @param int backtrack The number of previously-pushed messages to request upon susbcribing
	 */
	public synchronized void joinChannel(String chan, int backtrack){
		System.out.println("Joining channel: " + chan + " with backtrack=" + backtrack);
		boolean wasrunning = false;
		if(deaconThread.isAlive()) {
			wasrunning = true;
			running = false;
			while(deaconThread.isAlive()) {}	// Wait for Deaconthread to die
		}
		System.out.println("Got into joinChannel with chan="+chan);
		this.subscriptions.add(chan);
		if(wasrunning) deaconThread.start();
		
		// TODO still need to accommodate the backtrack
	}
	
	/**
	 * Unsubscribes this DeaconService from the Meteor channel; Takes effect after the present polling interval terminates.
	 * @param String chan The channel name on the Meteor server
	 */
	public synchronized void leaveChannel(String chan) {
		this.subscriptions.remove(chan);
	}
	
	/**
	 * Initiates the connection with the Meteor server
	 */
	public void start(){
		running = true;
		if (!deaconThread.isAlive()){
			deaconThread.start();
		}
		
	}
	
	/**
	 * Closes the connection to the Meteor server; Takes effect after the present polling interval terminates.
	 */
	public void stop(){
		running = false;
	}
	
	/**
	 * Returns a description of the DeaconService
	 */
	public String toString(){
		return "Deacon Service @" + host + ":" + port;
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
	 * In lieu of outside protocol documentation, Meteor message format is captured here:
	 * 		HeaderTemplate HTTP/1.1 ~status~\r\nChannel(~channelinfo~)\r\n
	 * 		MessageTemplate m.p.<~id~>."~channel~"."{[~text~]}"\r\n
	 * 		ChannelInfoTemplate "~channel~".~lastMsgID~
	 * 		PingMessage m.p
	 * 		SubscriberShutdownMsg m.sd
	 * TODO - details of the Deacon-specific Meteor configuration should be moved into the Deacon Wiki
	 * 
	 * @param meteorMessage
	 */
	protected synchronized void parse(String meteorMessage) {
		// TODO This is probably well-suited to a command pattern
		// Tried to implement this but figured we should get it working for POC first, then refactor
//		System.out.println("DeaconService.parse: Got meteorMessage="+meteorMessage);
		Pattern p = Pattern.compile("m\\.(.*)");
		Matcher m = p.matcher(meteorMessage);
		int pass = 0;
		if(m.find()) {
			while(pass<=m.groupCount()) {
				String thisgroup = m.group(pass).trim();
//				System.out.println("DeaconService.parse: got group="+thisgroup);
				if(thisgroup.split("\\.")[0].equals("p")) {
					Pattern message = Pattern.compile("p\\.<(\\d*)>\\.\"(.*)\"\\.\"\\{\\[(.*)\\]\\}\"");
					Matcher parameters = message.matcher(thisgroup);
					if(parameters.find()) {
//						System.out.println("  Message ID = " + parameters.group(1));
//						System.out.println("  Channel    = " + parameters.group(2));
//						System.out.println("  Message    = " + parameters.group(3));
						this.notifyObservers(new DeaconResponse(parameters.group(2), parameters.group(3)));
					}
				}
				pass++;
			}
		}
	}

}
