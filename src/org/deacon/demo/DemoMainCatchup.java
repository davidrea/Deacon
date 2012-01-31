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

package org.deacon.demo;

import java.io.IOException;
import java.net.UnknownHostException;

import org.deacon.DeaconError;
import org.deacon.DeaconObserver;
import org.deacon.DeaconResponse;
import org.deacon.MeteorPushReceiver;

public class DemoMainCatchup implements DeaconObserver{
	
	public static void main(String args[]) throws UnknownHostException, IOException {
		DemoMainCatchup testMain = new DemoMainCatchup();
		testMain.run();
	}
	
	public boolean error = false;
	
	public void run() throws UnknownHostException, IOException {
		MeteorPushReceiver myService;
		try {
			myService = new MeteorPushReceiver("home.daverea.com",4670);
			myService.joinChannel("2sec", 3);
			myService.register(this);
			myService.catchUpTimeOut(10);
			
			// Start Deacon
			System.out.println("Starting Deacon Service for: "+myService.toString());
			try {
				myService.start();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			try {
				Thread.sleep(5*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// Stop Deacon
			System.out.println("Stopping Deacon");
			myService.stop();
			try {
				Thread.sleep(5*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// Restart Deacon
			System.out.println("Restarting Deacon");
			try {
				myService.start();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			try {
				Thread.sleep(5*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(!error){
				System.out.println("Stopping Deacon Service");
				myService.stop();
			}
			else{
				System.out.println("Oops, there was an error. Move along, nothing to see...");
			}
		} catch (Exception e2) {
			System.out.println("Attempted to start DeaconService with invalid port number.");
		}
	}

	@Override
	public void onPush(DeaconResponse response) {
		System.out.println("DemoMain.onPush on channel="+response.getChannel()+", got payload="+response.getPayload());
	}

	@Override
	public void onError(DeaconError err) {
		error = true;
		System.out.println("What the deuce, there was an ERROR: "+err.getErrorMsg());
	}

	@Override
	public void onReconnect() {
		error = false;
		System.out.println("Oh Hai!~ I'm reconnected");
	}
	
	@Override
	public void onDisconnect(DeaconError err) {
		error = false;
		System.out.println("Forgot to pay the utilities, I'm disconnected");
	}

	@Override
	public void onStateChanged(boolean running, boolean connected) {
		// TODO Auto-generated method stub
		
	}
	
}
