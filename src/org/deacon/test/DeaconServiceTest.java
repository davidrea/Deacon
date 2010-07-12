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
		fail("Test not yet implemented");
	}
	
	public void testLeaveChannel() {
		fail("Test not yet implemented");
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
		fail("Test not yet implemented");
	}
	
	public void testStop() {
		fail("Test not yet implemented");
	}

	@Override
	public void onError(DeaconError err) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPush(DeaconResponse response) {
		// TODO Auto-generated method stub
		this.response = response;
	}

	@Override
	public void onReconnect() {
		// TODO Auto-generated method stub
		
	}

}
