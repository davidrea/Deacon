package org.deacon;

public class DeaconResponse {

	private String response;
	
	DeaconResponse(String resp){
		response = resp;
	}

	public String getResponse(){
		return response;
	}
	
	public void setResponse(String resp){
		response = resp;
	}
	
}

