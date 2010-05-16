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

import org.deacon.interfaces.DeaconServiceObserver;

public class DeaconObservable {

	private ArrayList<DeaconServiceObserver> observers = new ArrayList<DeaconServiceObserver>();
	
	public void register(DeaconServiceObserver observer){
		observers.add(observer);
	}
	
	public void unregister(DeaconServiceObserver observer){
		observers.remove(observer);
	}
	
	public void notifyObservers(DeaconResponse response){
		for (DeaconServiceObserver obs: observers){
			obs.onPush(response);
		}
	}
	
	public void notifyObserversError(DeaconError err){
		for (DeaconServiceObserver obs: observers){
			obs.onError(err);
		}
	}
	
	public void notifyObserversReconnect(){
		for (DeaconServiceObserver obs: observers){
			obs.onReconnect();
		}
	}
	
}
