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
import org.deacon.DeaconService;

public class DemoMain implements DeaconObserver{
	
	public static void main(String args[]) throws UnknownHostException, IOException {
		DemoMain testMain = new DemoMain();
		testMain.run();
	}
	
	public boolean error = false;
	
	public void run() throws UnknownHostException, IOException {
		DeaconService myService;
		try {
			myService = new DeaconService("home.daverea.com",4670);
			//myService.pingTimeout(/* Set lower than meteord.conf:PingInterval for testing */);
			//myService.catchUpTimeOut(/* Set longer than pingTimeout if you want to recover from a single broken pipe */);
			myService.joinChannel("2sec", 0);
			myService.register(this);
			
			try {
				System.out.println("Starting: " + myService.toString());
				myService.start();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			try {
				Thread.sleep(15*1000);
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
		System.out.println("What the deuce?! There was an error: "+err.getErrorMsg());
	}

	@Override
	public void onReconnect() {
		error=false;
		System.out.println("Oh Hai! O_o Deacon is reconnected");
	}
	
	@Override
	public void onDisconnect(DeaconError err){
		error=true;
		System.out.println("Forgot to pay utility bill, Deacon is disconnected");
	}
	
}
