package edu.ncku.uscc.process;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.TooManyListenersException;

import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.Log;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class ConnectionScanner extends Thread implements SerialPortEventListener{

	public static final int DEHUMIDIFIERS_A_ROOM = 8;

	public static final int ROOM_ID_MIN = 2;
	public static final int ROOM_ID_MAX = 5;
	
	private final Object lock = new Object();
	
	private final LinkedList<Command> cmdQueue = new LinkedList<Command>();
	
	private InputStream inputStream;
	private OutputStream outputStream;
	private SerialPort serialPort;

	private byte rxBuf;

	private Command currentCmd;	
	
	public OutputStream getOutputStream() {
		OutputStream ops;
		synchronized (lock) {
			ops = outputStream;
		}
		return ops;
	}

	public InputStream getInputStream() {
		InputStream is;
		synchronized (lock) {
			is = inputStream;
		}
		return is;
	}

	public ConnectionScanner(SerialPort serialPort, DataStoreManager dataStoreManager, int timeout) throws IOException, TooManyListenersException {
		// TODO Auto-generated constructor stub
		// open the streams
		this.serialPort = serialPort;
		this.inputStream = serialPort.getInputStream();
		this.outputStream = serialPort.getOutputStream();

		// add event listeners
		serialPort.addEventListener(this);
		serialPort.notifyOnDataAvailable(true);
		
		for (int roomScanIndex = ROOM_ID_MIN; roomScanIndex <= ROOM_ID_MAX; roomScanIndex++) {
			for (int did = 0; did < DEHUMIDIFIERS_A_ROOM; did++) {
				cmdQueue.add(new ScanDehumidifierCmd(this, dataStoreManager, roomScanIndex, did, timeout));
			}
		}
		cmdQueue.add(new ScanPanelCmd(this, dataStoreManager, timeout));

		setDaemon(true);
		start();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			nextCmd(null);
			while (currentCmd != null) {
				currentCmd.start();
//				Thread.sleep(300);
			}
		} catch (Exception e) {
			Log.error(e, e);
		} 
	}

	@Override
	public void serialEvent(SerialPortEvent oEvent) {
		// TODO Auto-generated method stub
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {

			try {
				int available = inputStream.available();
				byte chunk[] = new byte[available];

				if (getInputStream() == null)
					return;
				
				inputStream.read(chunk, 0, available);

				for (byte b : chunk) {
					rxBuf = b;
				}

				synchronized (lock) {
					lock.notifyAll();
				}

			} catch (IOException e) {
				Log.error(serialPort.getName() + " disconnected!", e);
			} catch (Exception e) {
				Log.error(e, e);
			}

		}
	}

	public Object getLock() {
		// TODO Auto-generated method stub
		return lock;
	}

	public void setRxBuf(byte value) {
		// TODO Auto-generated method stub
		rxBuf = value;
	}

	public byte getRxBuf() {
		// TODO Auto-generated method stub
		return rxBuf;
	}

	public SerialPort getSerialPort() {
		// TODO Auto-generated method stub
		return serialPort;
	}
	
	public String getPortName() {
		return serialPort.getName();
	}

	public synchronized void nextCmd(Command cmd) throws Exception {
		// TODO Auto-generated method stub
		if (cmd != null)
			cmdQueue.add(cmd);

		currentCmd = (cmdQueue != null) ? cmdQueue.pollFirst() : null;

		if (currentCmd == null)
			throw new Exception("current command is null.");
	}

}
