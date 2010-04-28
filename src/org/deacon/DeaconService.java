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
					out  = new PrintWriter(sock.getOutputStream(), true);
					in   = new BufferedReader(new InputStreamReader(sock.getInputStream()));
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
				//out.println("GET /push/" + System.currentTimeMillis() + "/longpoll/demo HTTP/1.1\r\n\r\n");
				//out.println("GET "+ channel +" HTTP/1.1\r\n\r\n");
				System.out.println("Server string: " + serverstring);
				out.println(serverstring);
				
				try {
					String newResponse = "";
					// Wait for a response from the channel
					while( (response=in.readLine()) != null && running) {
						//System.out.println("Got response: " + response);
						newResponse += response + "\n";
						parse(newResponse);
					}
					if(running){
						//notifyObservers( new DeaconResponse(newResponse) );
						// TODO move this into parser(), allowing observer notification only in the event of an incoming push
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
	 * Adds a subscription in the DeaconService to the specified Meteor server channel
	 * @param String chan The channel name on the Meteor server
	 * @param int backtrack The number of previously-pushed messages to request upon susbcribing
	 */
	public synchronized void joinChannel(String chan, int backtrack){
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
	 * @param meteorMessage
	 */
	private synchronized void parse(String meteorMessage) {
		// TODO This is probably well-suited to a command pattern
		// Tried to implement this but figured we should get it working for POC first, then refactor
		// First, split the meteorMessage into command and payload
		//Pattern p = Pattern.compile("Meteor\u002E(.*)\u0028(.*)\u0029");
		Pattern p = Pattern.compile("Meteor\\.(.*)\\((.*)\\)");
		Matcher m = p.matcher(meteorMessage);
		int pass = 0;
		if(m.find()) {
			while(pass<=m.groupCount()) {
				System.out.println("Parser: got group "+pass+" = " + m.group(pass++));
			}
		}
		
	}
	
}
