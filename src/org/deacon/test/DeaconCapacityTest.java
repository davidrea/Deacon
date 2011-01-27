package org.deacon.test;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.traces.Trace2DReplacing;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JFrame;

import org.deacon.DeaconError;
import org.deacon.DeaconObserver;
import org.deacon.DeaconResponse;
import org.deacon.DeaconService;

/**
 * Class to test various capacity limits of the Meteor server and Deacon client
 * <p>Implements the following tests:
 * <ul><li>Simultaneous subscribers - Measures push message latency as channel 
 * subscriber count is increased, and stops when a configurable maximum latency 
 * is observed.</li>
 * <li>Channel count - Measure push message latency as the number of parallel 
 * channels on the Meteor server is increased.</ul>
 * @author dave
 *
 */

public class DeaconCapacityTest implements DeaconObserver {
	
	public static final int MAX_LATENCY_MS   = 1000;
	public static final long TEST_MAX 		 = 25000;	// Stop here if latency OK
	public static final String SERVER		 = "10.37.57.1";
	public static final int CLIENT_PORT		 = 4670;
	public static final int CONTROLLER_PORT	 = 4671;
	public static final String CHANNEL		 = "testchan";
	
	private ArrayList<DeaconService> DeaconInstances = new ArrayList<DeaconService>();
	private int maxLatency 							 = 0;
	private Hashtable<Integer, Integer> simulSubsReuslts	 = new Hashtable<Integer, Integer>();
	
	/**
	 * Entry point and main runtime for DeaconCapacityTest class
	 * @param args standard command line arguments
	 */
	public static void main(String[] args) {
		DeaconCapacityTest test = new DeaconCapacityTest();
		
		test.simultaneousSubscribers();
		
		// Wait for all outstanding pushes to arrive
		try {
			Thread.sleep(5000);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		//test.channelCount();
	}
	
	private void simultaneousSubscribers() {
		
		// Run the Simultaneous Subscribers test
		
		System.out.println("Starting Simultaneous Subscribers test");
		maxLatency = 0;

		Chart2D chart = new Chart2D();
		ITrace2D simulSubTrace = new Trace2DSimple();
		chart.addTrace(simulSubTrace);
		simulSubTrace.setTracePainter(new TracePainterDisc());
		simulSubTrace.setPhysicalUnits("Subscriber Count", "Latency (ms)");
		
		while(maxLatency <= MAX_LATENCY_MS && DeaconInstances.size() <= TEST_MAX) {
			
			// Pause a moment
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			
			// Add subscribers
			try {
				DeaconService deacon = new DeaconService(SERVER, CLIENT_PORT);
				deacon.joinChannel(CHANNEL, 0);
				deacon.register(this);
				deacon.start();
				DeaconInstances.add(deacon);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// Send a push message to all subscribers
			// The message payload is the current time in milliseconds
			// This enables calculation of the latency
			sendPush(CHANNEL, DeaconInstances.size() + "," + Long.toString(System.currentTimeMillis()));
			
			System.out.println("  Progress ... " + DeaconInstances.size() + " subscribers, worst latency = " + maxLatency);
		}
		
		// Test done
		for(DeaconService sub : DeaconInstances) {
			sub.stop();
		}
		
		// Print report
		System.out.println("Simultaneous Subscribers test results");
		System.out.println("=====================================");
		System.out.println("  Reached " + DeaconInstances.size() + " subscribers");
		System.out.println("  Maximum latency: " + maxLatency + "ms\n");
		
		Enumeration<Integer> keys = simulSubsReuslts.keys();
		System.out.println("SimulSubsResults = " + simulSubsReuslts.size());
		while(keys.hasMoreElements()) {
			Integer count = keys.nextElement();
			simulSubTrace.addPoint(count, simulSubsReuslts.get(count));
		}
		
		JFrame frame = new JFrame("Simultaneous Subscriber Test Results");
		frame.getContentPane().add(chart);
		frame.setSize(640,480);
		frame.addWindowListener(
				new WindowAdapter() {
					public void windowClosingEvent(WindowEvent e) {
						System.exit(0);
					}
				} );
		frame.setVisible(true);

		DeaconInstances.clear();
		
	}
	
	private void channelCount() {
		
		// Run the Channel Count test
		
		maxLatency = 0;
		while(maxLatency <= MAX_LATENCY_MS && DeaconInstances.size() <= TEST_MAX) {
			
			// Pause a moment
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			
			// Add channel
			try {
				DeaconService deacon = new DeaconService(SERVER, CLIENT_PORT);
				deacon.joinChannel("chan" + DeaconInstances.size(), 0);
				deacon.register(this);
				deacon.start();
				DeaconInstances.add(deacon);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// Send push messages to all channels
			for (DeaconService sub : DeaconInstances) {
				sendPush("chan" + DeaconInstances.indexOf(sub), 
						 DeaconInstances.size() + "," + Long.toString(System.currentTimeMillis()));
			}
			
			System.out.println("  Progress ... " + DeaconInstances.size() + " channels, worst latency = " + maxLatency);
		}
		
		// Test done
		for(DeaconService sub : DeaconInstances) {
			sub.stop();
		}
		
		// Print report
		System.out.println("Channel Count test results");
		System.out.println("==========================");
		System.out.println("  Reached " + DeaconInstances.size() + " channels");
		System.out.println("  Maximum latency: " + maxLatency + "ms\n");

		DeaconInstances.clear();
		
	}
	
	// Helper methods
	
	private void sendPush(String channel, String message) {		
		Socket serverControlChannel;
		PrintWriter serverOut;
		try {
			serverControlChannel = new Socket(SERVER, CONTROLLER_PORT);
			serverOut = new PrintWriter(serverControlChannel.getOutputStream(), true);
			serverOut.println("ADDMESSAGE " + channel + " " + message);
			serverOut.close();
			try {
				serverControlChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Interface method overrides

	@Override
	public synchronized void onPush(DeaconResponse response) {
		// Extract latency and count-when-push-sent
		Long now = System.currentTimeMillis();
		int count = Integer.parseInt(response.getPayload().split(",")[0]);
		int thislatency = (int)(now - Long.parseLong(response.getPayload().split(",")[1]));
		
		// Update the worst-case latency for test termination
		if( thislatency > maxLatency) maxLatency = thislatency;
		
		// Update the result dataset
		// XYDataset is used to track the smallest and largest subscriber counts at each latency
		//   X = Latency in milliseconds
		//   Y = Subscriber count
		if(response.getChannel().equals(CHANNEL)) {
			// Simultaneous subscribers test
			if(simulSubsReuslts.containsKey(count)) {
				if(simulSubsReuslts.get(count) < count) {
					simulSubsReuslts.remove(count);
					simulSubsReuslts.put(count, thislatency);
				}
			}
			else
				simulSubsReuslts.put(count, thislatency);
		}
		else {
			// Channel count test
		}
	}

	@Override
	public void onError(DeaconError error) { }

	@Override
	public void onReconnect() { }

	@Override
	public void onDisconnect(DeaconError error) { }

}
