package org.deacon;

import java.util.ArrayList;

import org.deacon.interfaces.OnPushListener;

public class DeaconObservable {

	private ArrayList<OnPushListener> observers = new ArrayList<OnPushListener>();
	
	public void setOnPushListener(OnPushListener observer){
		observers.add(observer);
	}
	
	public void unregister(OnPushListener observer){
		observers.remove(observer);
	}
	
	public void notifyObservers(DeaconResponse response){
		for (OnPushListener obs: observers){
			obs.onPush(response);
		}
	}
	
}
