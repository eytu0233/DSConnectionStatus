package edu.ncku.uscc.process;

import edu.ncku.uscc.util.DataStoreManager;
import edu.ncku.uscc.util.Log;

public class ScanPanelCmd extends Command implements IPanelProtocal {

	DataStoreManager dataStoreManager;
	
	public ScanPanelCmd(ConnectionScanner scanner, DataStoreManager dataStoreManager, int timeout) {
		super(scanner, timeout);
		// TODO Auto-generated constructor stub
		this.dataStoreManager = dataStoreManager;
	}

	@Override
	protected byte requestHandler() throws Exception {
		// TODO Auto-generated method stub
		
		return (byte) PANEL_REQ_ONOFF;
	}

	@Override
	protected boolean replyHandler(byte rxBuf) throws Exception {
		// TODO Auto-generated method stub
		if (rxBuf == PANEL_REP_ON || rxBuf == PANEL_REP_OFF) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void finishHandler() throws Exception {
		// TODO Auto-generated method stub
		Log.debug("Panel echo : " + scanner.getPortName());
		dataStoreManager.setPanelState(scanner.getPortName(), true);
		scanner.nextCmd(this);
	}

	@Override
	protected void timeoutHandler() throws Exception {
		// TODO Auto-generated method stub
		Log.debug("Panel timeout : " + scanner.getPortName());
		dataStoreManager.setPanelState(scanner.getPortName(), false);
		scanner.nextCmd(this);
	}

}
