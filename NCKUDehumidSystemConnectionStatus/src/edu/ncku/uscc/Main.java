package edu.ncku.uscc;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import edu.ncku.uscc.io.ModbusTCPSlave;
import edu.ncku.uscc.process.ConnectionScanner;
import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.HeartBeatAccepter;
import edu.ncku.uscc.util.Log;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

public class Main {

	private static final int TIME_OUT = 400;
	private static final int BAUD_RATE = 9600;

	private static final CountDownLatch LATCH = new CountDownLatch(1);

	private static final Set<String> portNameSet = new HashSet<String>() {
		{
			add("/dev/ttyUSB0");
			add("/dev/ttyUSB1");
			add("/dev/ttyUSB2");
			add("/dev/ttyUSB3");
		}
	};

	private static int timeout = 300;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length == 1) {
			timeout = Integer.valueOf(args[0]);
		}

		try {
			Log.init();
			
			// start the modbus tcp slave thread
			ModbusTCPSlave slave = new ModbusTCPSlave(9);
			slave.initialize();
			Log.info("Modbus TCP Slave Started...");

			ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
//			service.submit(new PortScanTask(new DataStoreManager(slave)));
			service.submit(new HeartBeatAccepter());

			LATCH.await();
		} catch (Exception e) {
			Log.error(e, e);
		}
	}

	
	
	public static class PortScanTask implements Runnable {

		DataStoreManager dataStoreManager;

		public PortScanTask(DataStoreManager dataStoreManager) {
			// TODO Auto-generated constructor stub
			this.dataStoreManager = dataStoreManager;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
				
				while (portEnum.hasMoreElements()) {
					CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
					String portName = currPortId.getName();

					if (portNameSet.contains(portName)) {
						SerialPort serialPort = (SerialPort) currPortId.open(this.getClass().getName(), TIME_OUT);
						serialPort.setSerialPortParams(BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
								SerialPort.PARITY_NONE);
						serialPort.getName();
						
						Log.debug("start scan " + portName);

						new ConnectionScanner(serialPort, dataStoreManager, timeout);
					}
				}
			} catch (Exception e) {
				Log.error(e, e);
				LATCH.countDown();
			}

		}

	}

}
