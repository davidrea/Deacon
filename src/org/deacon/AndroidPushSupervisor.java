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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

public class AndroidPushSupervisor {

	private Context parent = null;
	private PushReceiver supervisee = null;
	
	public AndroidPushSupervisor(Context context, PushReceiver rx) {
		parent = context;
		supervisee = rx;
		this.parent.registerReceiver(bcr, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
	}
	
	/**
	 * BroadcastReceiver for receiving CONNECTIVITY_ACTIONs
	 * Used to detect changes in network state instead of polling....
	 * http://stackoverflow.com/questions/2294971/intent-action-for-network-events-in-android-sdk
	 */
	private BroadcastReceiver bcr = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("Deacon","Intent Received");
			if (intent.getAction().equals(android.net.ConnectivityManager.CONNECTIVITY_ACTION)) {
				Bundle extras = intent.getExtras();
				if(extras.containsKey("networkInfo")) {
					NetworkInfo netinfo = (NetworkInfo) extras.get("networkInfo");
					if(netinfo.isConnected()) {
						// Network is connected
						if(!supervisee.isRunning() && supervisee.autoReconnect()) {
							supervisee.start();
						}
					}
					else {
						supervisee.stop();
					}
				}
				else if(extras.containsKey("noConnectivity")) {
					supervisee.stop();
				}
		    }
		}
	};
	
	/**
	 * Called by GC when Deacon object is GC'd.
	 * Call from Application context before Android onDestroy method returns.
	 */
	protected void finalize() {
		supervisee.stop();
		this.parent.unregisterReceiver(bcr);
	}

}
