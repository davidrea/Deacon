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

import java.util.ArrayList;

/**
 * Implements an observble to be used to register to receive Deacon
 * events such as disconnection and reconnection from a server.
 */
public class DeaconObservable {
	
	// Members

	protected ArrayList<DeaconObserver> observers = new ArrayList<DeaconObserver>();
	
	// Methods
	
	/**
	 * Allows registration to observe events from this observable
	 * @param observer is a DeaconObserver that wants notification
	 * events from this observable
	 */
	public void register(DeaconObserver observer){
		observers.add(observer);
	}
	
	/**
	 * Allows unregistration of event notification
	 * @param observer that wants to unregister
	 */
	public void unregister(DeaconObserver observer){
		observers.remove(observer);
	}
	
	/**
	 * Notifies observers of push events
	 * @param response is the payload response from the server
	 */
	protected void notifyPush(DeaconResponse response){
		for (DeaconObserver obs: observers){
			obs.onPush(response);
		}
	}
	
	/**
	 * Notified observers of state changes
	 * @param running Whether the client is currently running
	 * @param connected Whether the client is currently connected to the server
	 */
	protected void notifyState(boolean running, boolean connected) {
		for (DeaconObserver obs: observers) {
			obs.onStateChanged(running, connected);
		}
	}
	
	/**
	 * Notifies observers of the service's errors
	 * @param error is the error object that is sent to
	 * the observers
	 */
	protected void notifyError(DeaconError error){
		for (DeaconObserver obs: observers){
			obs.onError(error);
		}
	}
	
	// Deprecated Methods
	
	/**
	 * @deprecated
	 */
	protected void notifyObservers(DeaconResponse response) {
		notifyPush(response);
	}
	
	/**
	 * @deprecated
	 * 
	 * Notifies observers of a reconnect to the server event
	 */
	protected void notifyObserversReconnect(){
		for (DeaconObserver obs: observers){
			obs.onReconnect();
		}
	}
	
	/**
	 * @deprecated
	 * 
	 * Notifies observers of a service disconnect
	 * @param error is the error sent to the observer
	 */
	protected void notifyObserversDisconnect(DeaconError error){
		for (DeaconObserver obs: observers){
			obs.onDisconnect(error);
		}
	}
	
}
