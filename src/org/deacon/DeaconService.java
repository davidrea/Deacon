package org.deacon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class DeaconService extends DeaconObservable {

	private final String host;
	private final int port;
	private final long hostid;
	
	private ArrayList<String> subscriptions;
	private Socket sock = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private boolean running=false;
	
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
					}
					if(running){
						notifyObservers( new DeaconResponse(newResponse) );
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
	
	
	public DeaconService(String host, int port) throws UnknownHostException, IOException{
		this.host = host;
		this.port = port;
		this.hostid = System.currentTimeMillis();
		this.subscriptions = new ArrayList<String>();
	}
	
	public void joinChannel(String chan, int backtrack){
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
	
	public void leaveChannel(String chan) {
		this.subscriptions.remove(chan);
	}
	
	public void start(){
		running = true;
		if (!deaconThread.isAlive()){
			deaconThread.start();
		}
		
	}
	
	public void stop(){
		running = false;
		//deaconThread.stop();//not good, thread should shutdown if running is false
		//need a better way to do this...
	}
	
	public String toString(){
		return "Deacon Service @" + host + ":" + port;
	}

	public boolean isRunning() {
		return this.running;
	}
	
}
