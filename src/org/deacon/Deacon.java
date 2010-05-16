package org.deacon;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Set;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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
	 * BroadcastReceiver for receiving CONNECTIVITY_ACTIONs
	 * We can detect changes in network state instead of polling....
	 * http://stackoverflow.com/questions/2294971/intent-action-for-network-events-in-android-sdk
	 */
	BroadcastReceiver bcr = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("Deacon","Intent Received");
			if (intent.getAction().equals(android.net.ConnectivityManager.CONNECTIVITY_ACTION)) {
				Log.d(Deacon.class.getSimpleName(), "action: " + intent.getAction());
				Bundle extras = intent.getExtras();
				Set<String> keys = extras.keySet();
				for(String key : keys) {
					System.out.println("Got key: " + key +  ", with value = " + extras.get(key));
				}
				if(extras.containsKey("networkInfo")) {
					NetworkInfo netinfo = (NetworkInfo) extras.get("networkInfo");
					if(netinfo.isConnected()) {
						Log.d("Deacon","Network has connection!");
						start();
					}
					else {
						Log.d("Deacon","Network is disconnected!");
						stop();
					}
				}
				else if(extras.containsKey("noConnectivity")) {
					Log.d("Deacon","Network is disconnected!");
					stop();
				}
				else {
					Log.d("Deacon", "Received CONNECTIVITY_ACTION intent but no networkInfo or noConnectivty extra data");
				}
		    }

			
		}
	};
	
	/**
	 * Method to return the broadcast receiver, you must register 
	 * this BroadcastReceiver from an Activity.
	 */
	public BroadcastReceiver getBroadcastReceiver(){
		return bcr;
	}
	
	
	/**
	 * @param host The Meteor server to which this client should connect
	 * @param port The client port that the Meteor server is listening on (default is usually 4670) 
	 * @param activity The activity that creates this class. Used to register an IntentFilter.//Not a great idea...
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public Deacon(String host, int port, Activity activity) throws UnknownHostException, IOException {
		super(host, port);
		activity.registerReceiver(this.getBroadcastReceiver(), new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
		
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
