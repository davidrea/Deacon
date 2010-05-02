package org.deacon;

// =======> CODE SMELL! This is shaping up to be a data class. Is there any functionality necessary here?!

public class DeaconResponse {

	private final String channel;
	private final String payload;
	
	DeaconResponse(String chan, String resp){
		channel = chan;
		payload = resp;
	}

	public String getPayload(){
		return payload;
	}

	public String getChannel() {
		return channel;
	}
	
}