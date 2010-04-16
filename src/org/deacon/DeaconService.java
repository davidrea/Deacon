package org.deacon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class DeaconService extends DeaconObservable {

	private String channel;
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
					sock = new Socket("data.meteorserver.org", 80);
					out  = new PrintWriter(sock.getOutputStream(), true);
					in   = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				// Subscribe to the channel
				//out.println("GET /push/" + System.currentTimeMillis() + "/longpoll/demo HTTP/1.1\r\n\r\n");
				out.println("GET "+ channel +" HTTP/1.1\r\n\r\n");
				
				try {
					String newResponse = "";
					// Wait for a response from the channel
					while( (response=in.readLine()) != null && running) {
						//System.out.println("Got response: " + response);
						newResponse+=response;
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
	
	
	DeaconService(String location, int port, String channel) throws UnknownHostException, IOException{
		setLocation(location, port);
		setChannel(channel);
		
	}


	public synchronized void setLocation(String location, int port) throws UnknownHostException, IOException{
		sock = new Socket(location, port);
	}
	
	public synchronized void setChannel(String chan){
		channel = chan;
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
		return "Deacon Service @"+sock.toString()+" on channel: "+channel;
	}
	
}
