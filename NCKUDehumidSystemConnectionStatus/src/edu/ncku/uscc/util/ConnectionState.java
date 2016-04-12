package edu.ncku.uscc.util;

public class ConnectionState {

	private boolean portConnection;
	
	private boolean scanFinish;

	private boolean panel;
	
	private boolean[][] dehumidifiers = new boolean[4][8];
	
	public boolean isPortConnection() {
		return portConnection;
	}

	public void setPortConnection(boolean portConnection) {
		this.portConnection = portConnection;
	}
	
	public void setPanel(boolean b){
		panel = b;
	}

	public boolean isScanFinish() {
		return scanFinish;
	}

	public void setScanFinish(boolean scanFinish) {
		this.scanFinish = scanFinish;
	}
	
	public boolean isPanel(){
		return panel;
	}
	
	public void setDehumidifier(int roomIndex, int did, boolean b){
		dehumidifiers[roomIndex - 2][did] = b;
	}
	
	public boolean isDehumidifier(int roomIndex, int did){
		return dehumidifiers[roomIndex - 2][did];
	}
}
