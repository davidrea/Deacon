package org.deacon.interfaces;

import org.deacon.DeaconError;
import org.deacon.DeaconResponse;

public interface DeaconServiceObserver {

	public void onPush(DeaconResponse response);
	
	public void onError(DeaconError err);
	
	public void onReconnect();
	
}
