package edu.ncku.uscc.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HeartBeatAccepter implements Runnable, IHeartBeat {

	private boolean isChecked = false;
	private Set<Thread> HBThreads = new HashSet<Thread>();

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			ServerSocket server = new ServerSocket(HB_PORT);
			while (true) {
				Socket socket = server.accept();
				HBThreads.add(new HeartBeatReciever(socket));
				if (!isChecked) {
					Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new HeartBeatChecker(HBThreads), 0, 1,
							TimeUnit.SECONDS);
					isChecked = true;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static class HeartBeatReciever extends Thread {

		private final static int TIME_OUT = 1000 * SERVER_CYCLE_SECONDS;

		private InputStream is;

		public HeartBeatReciever(Socket socket) throws IOException {
			// TODO Auto-generated constructor stub
			socket.setSoTimeout(TIME_OUT);
			is = socket.getInputStream();
			this.start();
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (true) {
				try {
					if (is.read() != HEART_BEAT) {
						throw new IOException("Not expect!");
					} else {
						Log.debug("Receive hello from client...");
					}
				} catch (SocketTimeoutException e) {
					Log.error("Timeout...");
					return;
				} catch (SocketException e) {
					Log.error("SocketException...");
					return;
				} catch (IOException e) {
					Log.error(e);
					return;
				}
			}
		}

	}

	public static class HeartBeatChecker extends Thread {

		final Set<Thread> HBThreads;

		public HeartBeatChecker(Set<Thread> hBThreads) {
			super();
			HBThreads = hBThreads;
			this.setDaemon(true);
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int counter = 0;
			for (Thread t : HBThreads) {
				if (t.isAlive()) {
					counter++;
				}
			}

			if (counter > 0) {
//				Log.debug("There are " + counter + " alive threads...");
			} else {
				System.exit(0);
			}
		}

	}

}
