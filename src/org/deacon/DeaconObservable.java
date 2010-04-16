package org.deacon;

import java.util.ArrayList;

public class DeaconObservable {

	private ArrayList<DeaconObserver> observers = new ArrayList<DeaconObserver>();
	
	public void register(DeaconObserver observer){
		observers.add(observer);
	}
	
	public void unregister(DeaconObserver observer){
		observers.remove(observer);
	}
	
	public void notifyObservers(DeaconResponse response){
		for (DeaconObserver obs: observers){
			obs.notify(response);
		}
	}
	
}
