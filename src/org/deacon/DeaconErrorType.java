package org.deacon;

/**
 * Describe the different error types for a DeaconError
 */
public enum DeaconErrorType {

	UnknownError ("An unknown error"),
	ConnectionError ("A connection Error"),
	UnknownHostError ("An unknown host error");
	
	private String message;
	
	/**
	 * Default constructor for the enum
	 * @param message is the error type's string
	 */
	private DeaconErrorType(String message){
		this.message = message;
	}
	
	/**
	 * Gets the error type's string
	 * @return the error type's string
	 */
	public String getErrorTypeMessage(){ return this.message; }
	
	/**
	 * Simple main method to test the enum
	 * @param args
	 */
	public static void main(String[] args) {
		DeaconErrorType e = UnknownHostError;
		
		System.out.println(e);
		System.out.println(e.getErrorTypeMessage());
		
		System.out.println("\n\nAll error types:");
		for (DeaconErrorType j: DeaconErrorType.values()){
			System.out.println(j+": "+j.getErrorTypeMessage());
		}
	}
}
