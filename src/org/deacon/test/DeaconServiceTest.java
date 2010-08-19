package org.deacon.test;

import java.io.IOException;
import java.net.UnknownHostException;

import org.deacon.DeaconService;
import org.deacon.DeaconError;
import org.deacon.DeaconObserver;
import org.deacon.DeaconResponse;

import junit.framework.TestCase;

public class DeaconServiceTest extends TestCase implements DeaconObserver {
	
	private static final String host = "home.daverea.com";
	private static final int port = 4670; 
	private DeaconService testDeacon = null;
	private DeaconResponse response = null;

	protected void setUp() throws Exception {
		super.setUp();
		this.testDeacon = new DeaconService(host, port);
		this.testDeacon.joinChannel("testonly", 0);
		this.testDeacon.joinChannel("2sec", 0);
		this.testDeacon.register(this);
	}
	
	public void testToString() {
		assertEquals("Deacon @ home.daverea.com:4670", this.testDeacon.toString());
	}
	
	public void testJoinChannel() {
		assertTrue(this.testDeacon.checkChannel("testonly"));
		assertTrue(this.testDeacon.checkChannel("2sec"));
	}
	
	public void testLeaveChannel() {
		this.testDeacon.leaveChannel("testonly");
		assertFalse(this.testDeacon.checkChannel("testonly"));
	}
	
	public void testCatchUpTimeOut() {
		this.testDeacon.catchUpTimeOut(10);
		assertEquals(10, this.testDeacon.catchUpTimeOut());
		this.testDeacon.catchUpTimeOut(0);
		assertEquals(0, this.testDeacon.catchUpTimeOut());
		this.testDeacon.catchUpTimeOut(-10);
		assertEquals(0, this.testDeacon.catchUpTimeOut());
	}
	
	public void testParse() {
		
		// Subclass DeaconService to gain access to protected methods
		class parseTester extends DeaconService {
			public parseTester(String host, int port) throws UnknownHostException, IOException, Exception {
				super(host, port);
				super.register(DeaconServiceTest.this);
			}
			public void testParse(String test) {
				socketLine(test);
			}
		}
		
		// Run the test
		try {
			parseTester tester = new parseTester(DeaconServiceTest.host, DeaconServiceTest.port);
			tester.testParse("m.p.<12345>.\"channel\".\"{[Hello Test]}\"\r\n");
			assertNotNull(this.response);
			assertEquals("channel", response.getChannel());
			assertEquals("Hello Test", response.getPayload());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Exception caught");
		}
	}
	
	public void testStart() {
		this.response = null;
		try {
			this.testDeacon.start();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception caught: Could not start DeaconService");
		}
		assertTrue(this.testDeacon.isRunning());
		try {
			Thread.sleep(2100);
			// TODO set up a higher-frequency channel on the test server to reduce test runtime
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("Exception caught: could not sleep");
		}
		assertNotNull(this.response);
		assertEquals(this.response.getChannel(), "2sec");
		assertNotNull(this.response.getPayload());
	}
	
	public void testStop() {
		this.testDeacon.stop();
		assertFalse(this.testDeacon.isRunning());
		this.response = null;
		try {
			Thread.sleep(2100);
		} catch(InterruptedException e) {
			e.printStackTrace();
			fail("Exception caught: could not sleep");
		}
		assertNull(this.response);
	}

	@Override
	public void onError(DeaconError err) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPush(DeaconResponse response) {
		// TODO Auto-generated method stub
		System.out.println("Received push " + response.getPayload());
		this.response = response;
	}

	@Override
	public void onReconnect() {
		// TODO Auto-generated method stub
		
	}

}
