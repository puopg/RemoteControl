package com.example.remotecontrol;

import android.os.Bundle;
import android.app.Activity;

import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.os.Build;
import android.annotation.SuppressLint;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

/*
 * This class I should have used AsyncTask as well, but I wanted to try implementing it in a different way. 
 * This was implemented prior to my knowledge of AsyncTask but I figured, this is nice learning task, I might as well try to see
 * what limitations creating a thread has.
 */
public class ClientActivity extends Activity {
	private UDPClient _udpClient = null;
	private String _cmd = null;
	private Ball screenLocation;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client);
	}

	/*
	 * When screen is touched, get the X and Y coordinates and send those along
	 * with the current devices dimensions.
	 * 
	 * @see android.app.Activity#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		float XInput = event.getX();
		float YInput = event.getY();
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth(); // deprecated
		int height = display.getHeight();
		RelativeLayout main = (RelativeLayout) findViewById(R.id.clientLayout);
		main.removeView(screenLocation);
		screenLocation = new Ball(this, XInput, YInput, 8);
		main.addView(screenLocation);
		try {
			_cmd = "Ping" + "_" + XInput + "_" + YInput + "_" + width + "_"
					+ height;
			Log.v("blah", _cmd);
			_udpClient = new UDPClient();
			_udpClient.start();
		} catch (Exception e) {
			e.printStackTrace();

		}
		return false;
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public class UDPClient extends Thread {
		public static final int SERVERPORT = 4444;

		/**
		 * Method to send the messages from client to server
		 * 
		 * @param message
		 *            the message to send to the server
		 */
		public void sendMessage(String message) {
		}

		@Override
		public void run() {
			super.run();
			byte[] lmessage = new byte[4096];
			DatagramSocket socket = null;
			// Hardcode a message for now
			lmessage = _cmd.getBytes();

			// Send the message to localhost and port 4444
			try {
				EditText serverIP = (EditText) findViewById(R.id.ServerIPChoose);
				InetAddress serverAddr = InetAddress.getByName(serverIP
						.getText().toString());
				socket = new DatagramSocket();
				DatagramPacket packet = new DatagramPacket(lmessage,
						lmessage.length, serverAddr, SERVERPORT);
				socket.send(packet);
			} catch (Throwable e) {
				e.printStackTrace();
			} finally {
				socket.close();
			}
		}
	}

	public void onClick(View view) {
		_cmd = "Button1";
		_udpClient = new UDPClient();
		_udpClient.start();
	}

	public void onClick2(View view) {
		_cmd = "Button2";
		_udpClient = new UDPClient();
		_udpClient.start();
	}

	public void onClick3(View view) {
		_cmd = "Button3";
		_udpClient = new UDPClient();
		_udpClient.start();
	}

	public void onArrowUpClick(View view) {
		_cmd = "Up";
		_udpClient = new UDPClient();
		_udpClient.start();
	}

	public void onArrowDownClick(View view) {
		_cmd = "Down";
		_udpClient = new UDPClient();
		_udpClient.start();
	}

	public void onArrowLeftClick(View view) {
		_cmd = "Left";
		_udpClient = new UDPClient();
		_udpClient.start();
	}

	public void onArrowRightClick(View view) {
		_cmd = "Right";
		_udpClient = new UDPClient();
		_udpClient.start();
	}
}
