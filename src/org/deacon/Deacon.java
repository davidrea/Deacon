package org.deacon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Deacon {
	
	public static void main(String[] args) throws IOException {
		
		Socket sock = null;
		PrintWriter out = null;
		BufferedReader in = null;
		
		
		int count = 20;
		String response;
		
		// Get 10 messages
		while(--count >= 0) {
			
			System.out.println("DEBUG: Entering loop at count " + (count+1));
			
			// Make the stuff
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
			out.println("GET /push/auswee/longpoll/demo HTTP/1.1\r\n\r\n");
			
			// Wait for a response from the channel
			while( (response=in.readLine()) != null ) {
				System.out.println("Got response: " + response);
			}

			out.close();
			in.close();
			sock.close();
			
		}
		
	}

}
