package org.deacon;

public class TestListener implements DeaconObserver{

	@Override
	public void notify(DeaconResponse response) {
		System.out.println("Listener got: " + response.getResponse());
		
	}
	
}
