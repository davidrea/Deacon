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
