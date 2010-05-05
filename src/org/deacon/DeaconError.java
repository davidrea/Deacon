package org.deacon;

public class DeaconError {

	private Exception error;
	
	public DeaconError(Exception e){
		error = e;
	}
	
	public String getErrorMsg(){
		return error.getMessage();
	}
	
	public Exception getError(){
		return error;
	}
	
}
