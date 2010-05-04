package org.deacon;

import java.io.IOException;
import java.net.UnknownHostException;

import android.os.Handler;
import android.os.Message;

/**
 * @author dave
 * 
 * Deacon class
 * A thin wrapper around DeaconService which isolates Android-specific code to maintain standalone testability of the DeaconService class.
 *
 */
public class Deacon extends DeaconService {

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			parse((String)msg.obj);
		}
	};

	/**
	 * @param host The Meteor server to which this client should connect
	 * @param port The client port that the Meteor server is listening on (default is usually 4670) 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public Deacon(String host, int port) throws UnknownHostException, IOException {
		super(host, port);
	}
	
	/**
	 * Android-wrapped version of socketLine; called whenever a line is received from the Meteor server
	 * This version implements Android thread-safe message passing rather than calling parse() directly (from the deaconThread)
	 */
	@Override
	protected void socketLine(String line) {
		Message msg = Message.obtain(handler, 0, line);
		msg.sendToTarget();
	}
	

}
