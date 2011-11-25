package org.deacon.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import junit.framework.TestCase;

import org.deacon.DeaconError;
import org.deacon.DeaconObserver;
import org.deacon.DeaconResponse;
import org.deacon.MeteorPushReceiver;

/**
 * Unit tests for the DeaconService class.
 * <p><strong>NOTE:</strong> The JUnit library must have <em>higher
 * precedence</em> on the classpath than android.jar, or all tests will fail 
 * with a "Stub!" exception.</p>
 * 
 * @author dave
 *
 */

public class MeteorPushReceiverTest extends TestCase implements DeaconObserver {
	
	private static final String host = "home.daverea.com";
	private static final int port = 4670; 
	private MeteorPushReceiver testReceiver = null;
	private DeaconResponse response = null;
	
	private boolean latestRunning = false;
	private boolean latestConnected = false;

	protected void setUp() throws Exception {
		super.setUp();
		this.testReceiver = new MeteorPushReceiver(host, port);
		this.testReceiver.joinChannel("testonly", 0);
		this.testReceiver.register(this);
		this.testReceiver.pingTimeout(2);
	}
	
	public void testToString() {
		assertEquals("Deacon stopped @ home.daverea.com:4670 disconnected", this.testReceiver.toString());
		
	}
	
	public void testJoinChannel() {
		assertTrue(this.testReceiver.checkChannel("testonly"));
	}
	
	public void testLeaveChannel() {
		this.testReceiver.leaveChannel("testonly");
		assertFalse(this.testReceiver.checkChannel("testonly"));
	}
	
	public void testCatchUpTimeOut() {
		this.testReceiver.catchUpTimeOut(10);
		assertEquals(10, this.testReceiver.catchUpTimeOut());
		this.testReceiver.catchUpTimeOut(0);
		assertEquals(0, this.testReceiver.catchUpTimeOut());
		this.testReceiver.catchUpTimeOut(-10);
		assertEquals(0, this.testReceiver.catchUpTimeOut());
	}
	
	public void testParse() {
		
		// Subclass DeaconService to gain access to protected methods
		class parseTester extends MeteorPushReceiver {
			public parseTester(String host, int port) throws UnknownHostException, IOException, Exception {
				super(host, port);
				super.register(MeteorPushReceiverTest.this);
			}
			public void testParse(String test) {
				parse(test);
			}
		}
		
		// Run the test
		try {
			parseTester tester = new parseTester(MeteorPushReceiverTest.host, MeteorPushReceiverTest.port);
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
		this.testReceiver.joinChannel("testonly", 0); // Subscribe to 'testonly' if not already subscribed
		assertTrue(this.testReceiver.checkChannel("testonly"));
		this.response = null;
		try {
			this.testReceiver.start();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception caught: Could not start DeaconService");
		}
		assertTrue(this.testReceiver.isRunning());
		assertTrue(this.latestRunning);
		
		try {
			Thread.sleep(500);
		} catch(InterruptedException e) {
			e.printStackTrace();
			fail("Exception caught: could not sleep");
		}
		
		assertTrue(this.testReceiver.isConnected());
		assertTrue(this.latestConnected);
		
		// Attempt to send a message
		Socket serverControlChannel = null;
		PrintWriter serverOut = null;
		try {
			serverControlChannel = new Socket(MeteorPushReceiverTest.host, 4671);
			serverOut = new PrintWriter(serverControlChannel.getOutputStream(), true);
			serverOut.println("ADDMESSAGE testonly messagetext");
			serverOut.close();
			try {
				serverControlChannel.close();
			} catch (IOException e) {
				fail("Exception caught: Unable to close connection to " + MeteorPushReceiverTest.host);
			}
		}
		catch (UnknownHostException e)
		{
			fail("Exception caught: Could not resolve " + MeteorPushReceiverTest.host);
		}
		catch (IOException e)
		{
			fail("Exception caught: Could not connect to " + MeteorPushReceiverTest.host);
		}
		
		try {
			Thread.sleep(100);
		} catch(InterruptedException e) {
			e.printStackTrace();
			fail("Exception caught: could not sleep");
		}
		
		assertNotNull(this.response);
		assertEquals(this.response.getChannel(), "testonly");
		assertEquals(this.response.getPayload(), "messagetext");
	}
	
	public void testStop() {
		this.testReceiver.stop();
		assertFalse(this.testReceiver.isRunning());
		assertFalse(this.testReceiver.isConnected());
		assertFalse(this.latestRunning);
		assertFalse(this.latestConnected);
	}
	
	public void testTimeout() {
		latestConnected = true;
		latestRunning = false;
		try {
			MeteorPushReceiver rx = new MeteorPushReceiver(host, port - 10);
			rx.register(this);
			assertEquals(false, rx.isConnected());
			assertEquals(false, rx.isRunning());
			rx.start();
			System.out.println("OK here goes check...");
			assertEquals(false, rx.isConnected());
			assertEquals(true, rx.isRunning());
			assertEquals(false, latestConnected);
			assertEquals(true, latestRunning);
			Thread.sleep(2050);
			assertEquals(false, rx.isConnected());
			assertEquals(false, rx.isRunning());
			assertEquals(false, latestConnected);
			assertEquals(false, latestRunning);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception caught");
		}
	}
	
	/**
	 * Overridden methods from DeaconObserver interface
	 * These are necessary in order for the test to receive Deacon events.
	 */

	@Override
	public void onError(DeaconError err) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPush(DeaconResponse response) {
		System.out.println("Received test push " + response.getPayload());
		this.response = response;
	}
	
	@Override
	public void onDisconnect(DeaconError error) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onReconnect() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStateChanged(boolean running, boolean connected) {
		System.out.println("Got notification " + running + " " + connected);
		latestRunning = running;
		latestConnected = connected;
	}
	
}
