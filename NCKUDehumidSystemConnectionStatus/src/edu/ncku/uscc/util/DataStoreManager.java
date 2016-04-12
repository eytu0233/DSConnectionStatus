package edu.ncku.uscc.util;

import edu.ncku.uscc.io.ModbusTCPSlave;

public class DataStoreManager {

	private static final int ALL_STATE_ADDR = 0;

	private static final String PORT_NAMES[] = { "/dev/ttyUSB0", // Linux
			"/dev/ttyUSB1", "/dev/ttyUSB2", "/dev/ttyUSB3" };

	private ModbusTCPSlave modbusSlave;

	/**
	 * Constructor
	 * 
	 * @param modbusSlave
	 */
	public DataStoreManager(ModbusTCPSlave modbusSlave) {
		super();
		this.modbusSlave = modbusSlave;
	}

	public void setPortFinish(String port) {
		int i = 0;
		for (i = 0; i < PORT_NAMES.length; i++) {
			if (PORT_NAMES[i].equals(port))
				break;
		}

		if (i == PORT_NAMES.length){
			Log.error("port name not match");
			return;
		}
			

		final int mask = 1 << (i + 4);
		int allState = modbusSlave.getResgister(ALL_STATE_ADDR);

		allState |= mask;
		modbusSlave.setRegister(ALL_STATE_ADDR, allState);
	}

	public void setPanelState(String port, boolean isLink) {
		int i = 0;
		for (i = 0; i < PORT_NAMES.length; i++) {
			if (PORT_NAMES[i].equals(port))
				break;
		}

		if (i == PORT_NAMES.length){
			Log.error("port name not match");
			return;
		}

		final int mask = 1 << i;
		int allState = modbusSlave.getResgister(ALL_STATE_ADDR);

		if (isLink) {
			allState |= mask;
		} else {
			allState &= ~mask;
		}

		modbusSlave.setRegister(ALL_STATE_ADDR, allState);
		setPortFinish(port);
	}

	public void setDehumidifierState(String port, int roomIndex, int did, boolean isLink) {
			
		int i = 0;
		for (i = 0; i < PORT_NAMES.length; i++) {
			if (PORT_NAMES[i].equals(port))
				break;
		}

		if (i == PORT_NAMES.length || roomIndex < 2 || roomIndex > 5 || did < 0 || did > 7)
			return;

		final int mask = 1 << (((roomIndex % 2 == 0) ? 0 : 8) + did);
		final int addr = i * 2 + 1 + ((roomIndex >= 4) ? 1 : 0);
		int value = modbusSlave.getResgister(addr);
		
//		Log.debug(port + " " + roomIndex + " " + did + " " + isLink + " " + value);
		
		if (isLink) {
			value |= mask;
		} else {
			value &= ~mask;
		}

//		Log.debug(value);
		modbusSlave.setRegister(addr, value);
	}

}