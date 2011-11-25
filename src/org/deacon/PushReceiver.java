/*
 * COPYRIGHT (c) 2011 The Members of the Deacon Project <http://www.deaconproject.org/>
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

abstract class PushReceiver extends DeaconObservable {
	
	// Members
	
	protected boolean running       = false;
	protected boolean connected     = false;
	protected boolean autoReconnect = true;
	protected Integer timeout       = 0;
	
	// Methods
	
	/**
	 * Start the client, establishing connections to begin receiving push messages.
	 */
	abstract public void start();
	
	/**
	 * Stop the client; close connections and stop receiving push messages.
	 */
	abstract public void stop();
	
	/**
	 * Get a description of the controller and its state
	 * @return A String containing the description
	 */
	abstract public String toString();
	
	/**
	 * Determine if the client has been started with the start() method.
	 * @return True if the client is running
	 */
	public boolean isRunning() { return running; }
	
	/**
	 * Determine if the client is connected to a server and ready to receive push messages.
	 * @return True if the client is connected
	 */
	public boolean isConnected() { return connected; }
	
	/**
	 * Set the general timeout after which operations should be abandoned.
	 * @param seconds The timeout duration
	 */
	void timeout(final Integer seconds) {
		if(seconds > 0 && seconds < Integer.MAX_VALUE) {
			timeout = seconds;
		}
	}
	
	/**
	 * Get the currently-configured timeout after which operations will be abandoned.
	 * @return The configured timeout in seconds
	 */
	Integer timeout() { return timeout; }
	
	/**
	 * Set whether client will try to automatically re-connect after loss of connectivity 
	 * @param restart True to enable auto-reconnect
	 */
	void autoReconnect(boolean reconn) { autoReconnect = reconn; }
	
	/**
	 * Get the current auto-reconnect configuration
	 * @return True if auto-reconnect enable
	 */
	boolean autoReconnect() { return autoReconnect; }
	
	/**
	 * Shortcut to DeaconObservable's notifyState method
	 */
	protected void notifyState() {
		System.out.println("Sending: " + running + " " + connected);
		notifyState(running, connected);
	}

}
