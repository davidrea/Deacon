package org.deacon;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;

public class DeaconManager {

	private static HashMap<String,DeaconService> deaconServices = new HashMap<String, DeaconService>();
	
	public void newDeaconService(String name, String location, int port, String channel) throws UnknownHostException, IOException{
		deaconServices.put(name, new DeaconService(location, port, channel));
	}
	
	
}
