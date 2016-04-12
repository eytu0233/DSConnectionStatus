package edu.ncku.uscc.process;

import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.Log;

//import gnu.io.SerialPort;

public class ScanDehumidifierCmd extends Command implements IDehumidProtocal {

	private static final String LCK_REMOVE_CMD = "sudo rm -f /var/lock/LCK..ttyUSB";
	// private SerialPort serialPort;
	private int roomScanIndex;
	private int did;
	private DataStoreManager dataStoreManager;

	public ScanDehumidifierCmd(ConnectionScanner scanner, DataStoreManager dataStoreManager, int roomScanIndex, int did,
			int timeout) {
		// TODO Auto-generated constructor stub
		super(scanner, timeout);
		this.dataStoreManager = dataStoreManager;
		this.roomScanIndex = roomScanIndex;
		this.did = did;
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		// for (int usbIndex = 0; usbIndex < 4; usbIndex++) {
		// Runtime.getRuntime().exec(LCK_REMOVE_CMD + usbIndex);
		// }

		byte txBuf;
		txBuf = (byte) ((roomScanIndex << 3) + did);

		// Log.debug(String.format("Scan %x from %s", ((roomScanIndex << 3) +
		// did), scanner.getSerialPort().getName()));
		// Log.info(String.format("Scan roomIndex : %x in %s",
		// ((int) txBuf & 0xff), serialPort.getName()));
		return txBuf;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == DEHUMID_REP_OK || rxBuf == DEHUMID_REP_HIGH_TEMP_ABNORMAL
				|| rxBuf == DEHUMID_REP_DEFROST_TEMP_ABNORMAL) {
			Log.debug(
					String.format("echo %x from %s", ((roomScanIndex << 3) + did), scanner.getSerialPort().getName()));
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		dataStoreManager.setDehumidifierState(scanner.getPortName(), roomScanIndex, did, true);
		scanner.nextCmd(this);
	}

	@Override
	protected void timeoutHandler() throws Exception {
		// TODO Auto-generated method stub
		dataStoreManager.setDehumidifierState(scanner.getPortName(), roomScanIndex, did, false);
		scanner.nextCmd(this);
	}

}
