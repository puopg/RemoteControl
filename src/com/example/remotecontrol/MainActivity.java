package com.example.remotecontrol;

import java.net.DatagramPacket;

import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.view.View;

import android.os.AsyncTask;
import android.os.Bundle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;

public class MainActivity extends Activity {

	/**
	 * Private Members
	 */
	private boolean _isServerActive = false;
	private Context _context; // For determining IP of device
	private boolean _isCanceled = false; // For determining if stop was called
											// or a timeout had occurred
											// Technically, right now stop
											// simply causes a timeout, need to
											// look into using DatagramChannel
	private Ball screenLocation; // So that we don't create more balls for touch
									// location

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		_context = getApplicationContext();
		// Display the Server IP on the Server activity
		displayServerIP();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.client:
			Intent intent = new Intent(this, ClientActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/*
	 * Set the Displayed IP on the View
	 */
	public void displayServerIP() {
		TextView serverIP = (TextView) findViewById(R.id.serverIPDisplayed);
		serverIP.setText(Utils.getIPAddress(_context));
	}

	/*
	 * Called when the Start/Stop button is pressed Set corresponding status
	 * flags when called.
	 */
	public void onClick(View view) {
		TextView serverStatus = (TextView) findViewById(R.id.serverStatus);
		Button startButton = (Button) findViewById(R.id.startButton);
		UDPServer server = new UDPServer();
		if (!_isServerActive) {
			_isServerActive = true;
			startButton.setText("Stop");
			serverStatus.setText("Running...");
			_isCanceled = false;
			server.execute();
		} else {
			serverStatus.setText("Stopping...");
			_isServerActive = false;
			server.cancel(true);
		}
	}

	/**
	 * The class extends the AsyncTask class so that it can update the UI Thread
	 * but also run the server in the background. All packets are sent to port
	 * 4444, chosen arbitrarily.
	 */
	private class UDPServer extends AsyncTask<Void, Void, Void> {
		public static final int SERVERPORT = 4444;
		public static final int timeout = 10000;
		private float xFromClient, yFromClient, xDim, yDim = 0;
		// private boolean isRunning = false;
		private String lastMessage = "";
		DatagramSocket socket = null;

		/*
		 * Server implementation. Will always be listening for a packet when
		 * running. If a packet is not received within 10 seconds, the server
		 * will timeout
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Void doInBackground(Void... params) {
			byte[] lmessage = new byte[4096];
			DatagramPacket packet = new DatagramPacket(lmessage,
					lmessage.length);

			try {
				String myIP = Utils.getIPAddress(_context);
				InetAddress serverAddr = InetAddress.getByName(myIP);
				socket = new DatagramSocket(SERVERPORT, serverAddr);
				socket.setSoTimeout(timeout);

				// Let the server continue to listen for incoming packets
				// Upon receiving a packet, send the data to publishProgress to
				// determine what to update in the main view
				while (true) {
					socket.receive(packet);
					Thread.sleep(200);
					lastMessage = new String(packet.getData(),
							packet.getOffset(), packet.getLength());
					processData();
					publishProgress();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				socket.close();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void v) {
			TextView serverStatus = (TextView) findViewById(R.id.serverStatus);
			Button startButton = (Button) findViewById(R.id.startButton);
			if (!_isCanceled)
				serverStatus.setText("Timeout");
			else
				serverStatus.setText("Stopped");
			_isServerActive = false;
			startButton.setText("Start");
		}

		/*
		 * Cleanup for server
		 */
		@Override
		protected void onCancelled() {
			try {
				_isCanceled = true;
				Thread.sleep(timeout + 5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/*
		 * Update UI Thread from Background Thread I decided to use a switch
		 * statement to determine what to do given the packet data. Since I
		 * cannot switch on a string, I switch on an enum
		 */
		@Override
		protected void onProgressUpdate(Void... v) {
			Command cmd = Command.valueOf(lastMessage);
			ToggleButton tglBtn = (ToggleButton) findViewById(R.id.toggleButton1);
			Button btn = (Button) findViewById(R.id.buttonA);
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) btn
					.getLayoutParams();
			switch (cmd) {
			case Button1:
				tglBtn = (ToggleButton) findViewById(R.id.toggleButton1);
				updateToggleButton(tglBtn);
				break;
			case Button2:
				tglBtn = (ToggleButton) findViewById(R.id.toggleButton2);
				updateToggleButton(tglBtn);
				break;
			case Button3:
				tglBtn = (ToggleButton) findViewById(R.id.toggleButton3);
				updateToggleButton(tglBtn);
				break;
			case Up:
				layoutParams.topMargin -= 10;
				btn.setLayoutParams(layoutParams);
				break;
			case Down:
				layoutParams.topMargin += 10;
				btn.setLayoutParams(layoutParams);
				break;
			case Left:
				layoutParams.leftMargin -= 10;
				btn.setLayoutParams(layoutParams);
				break;
			case Right:
				layoutParams.leftMargin += 10;
				btn.setLayoutParams(layoutParams);
				break;
			case Ping:
				// If the packet is to ping a screen location, transform the
				// coordinates from the client to servers dimensions
				Display display = getWindowManager().getDefaultDisplay();
				float width = display.getWidth(); // deprecated but using since
													// my device is API level 8
													// max... gg
				float height = display.getHeight();
				RelativeLayout main = (RelativeLayout) findViewById(R.id.serverLayout);
				main.removeView(screenLocation);
				screenLocation = new Ball(_context, xFromClient
						* (width / xDim), yFromClient * (height / yDim), 8);
				main.addView(screenLocation);
				break;
			}
		}

		private void updateToggleButton(ToggleButton tglBtn) {
			if (tglBtn.isChecked())
				tglBtn.setChecked(false);
			else
				tglBtn.setChecked(true);
		}

		/*
		 * Since I did not use a header to identify the data, I will just use
		 * delimiters to denote different parts of data Here, its pretty crappy,
		 * but basically there's only 1 case that I need more information from
		 * and that's when the screen location is needed. I need to know the
		 * dimensions of the device sending the packet and also the raw x and y
		 * coordinates. This allows me to transform it to the correct screen
		 * location on a different device
		 */
		private void processData() {
			if (lastMessage.contains("_")) {
				String[] message = lastMessage.split("_");
				lastMessage = message[0];
				xFromClient = Float.parseFloat(message[1]);
				yFromClient = Float.parseFloat(message[2]);
				xDim = Float.parseFloat(message[3]);
				yDim = Float.parseFloat(message[4]);
			}
		}
	}
}
