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


public class DeaconObservable {

	protected ArrayList<DeaconObserver> observers = new ArrayList<DeaconObserver>();
	
	public void register(DeaconObserver observer){
		observers.add(observer);
	}
	
	public void unregister(DeaconObserver observer){
		observers.remove(observer);
	}
	
	protected void notifyObservers(DeaconResponse response){
		for (DeaconObserver obs: observers){
			obs.onPush(response);
		}
	}
	
	protected void notifyObserversError(DeaconError err){
		for (DeaconObserver obs: observers){
			obs.onError(err);
		}
	}
	
	protected void notifyObserversDisconnect(){
		for (DeaconObserver obs: observers){
			obs.onDisconnect();
		}
	}
	
	protected void notifyObserversReconnect(){
		for (DeaconObserver obs: observers){
			obs.onReconnect();
		}
	}
	
}
