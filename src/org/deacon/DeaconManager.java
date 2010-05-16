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

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;

public class DeaconManager {

	private static HashMap<String,DeaconService> deaconServices = new HashMap<String, DeaconService>();
	
	public void newDeaconService(String name, String location, int port, String channel) throws UnknownHostException, IOException{
		//deaconServices.put(name, new DeaconService(location, port, channel));
		deaconServices.put(name, new DeaconService(location, port));
		deaconServices.get(name).joinChannel(channel, 0);
	}
	
	
}
