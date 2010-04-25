package org.deacon;

import java.io.IOException;
import java.net.UnknownHostException;

import org.deacon.interfaces.PushListener;

import android.content.Context;;

public class Deacon {
	
	private Context		  context;
	private DeaconService service;
	
	// Listeners
	private PushListener onPushListener;
	
	public Deacon(Context c, String host, int port) throws UnknownHostException, IOException {
		this.context = c;
		this.service = new DeaconService(host, port);
	}
	
	/**
	 * Subscribes 
	 * @param channel A string representing the server channel to join
	 * @param backtrack The number of previously-pushed messages to receive upon subscription
	 */
	public void subscribe(final String channel, int backtrack) {
		this.service.joinChannel(channel, backtrack);
		// Create a DeaconObserver that combines a channel (joined in service) with the onPushListener
		// Register the DeaconObserver
		if(!this.service.isRunning()) this.service.start();
	}
	
	/**
	 * Register a callback to be invoked when any of the subscribed push notifications is received.
	 * @param pl The PushListener to call with a push notification
	 */
	public void setOnPushListener(PushListener pl) {
		onPushListener = pl;
	}

}
