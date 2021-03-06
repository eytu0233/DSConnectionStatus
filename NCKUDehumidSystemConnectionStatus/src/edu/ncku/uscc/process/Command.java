package edu.ncku.uscc.process;

import java.io.OutputStream;

public abstract class Command {

	public static final int UNACK = -1;
	public static final int SKIP = -1;
	public static final int PROPERTY_CMD = -2;

	private Object referenceLock;

	protected ConnectionScanner scanner;

	private int timeout;
	private int init_tolerance = 2;
	private int err_tolerance = init_tolerance;
	private byte txBuf;

	private boolean ack;
	private boolean follow;

	/**
	 * Advance constructor which can set tolerance initial value
	 * 
	 * @param controller
	 * @param tolerance
	 *            the initial value for err_tolerance field
	 */
	public Command(ConnectionScanner scanner, int timeout) {
		super();
		this.scanner = scanner;
		this.referenceLock = scanner.getLock();
		this.timeout = timeout;
		this.init();
	}

	public byte getTxBuf() {
		return txBuf;
	}

	public boolean isAck() {
		return ack;
	}

	/**
	 * Starts this command
	 * 
	 * @throws Exception
	 */
	public final void start() throws Exception {
		
		scanner.setRxBuf((byte)UNACK);
		txBuf = requestHandler();
		
		// If txBuf is set as PROPERTY_CMD, it means that the command won't be run. 
		if (txBuf == PROPERTY_CMD) {
			init();
			finishHandler();
			return;
		}

		// When skip flag is true, it won't emit data and handle reply 
		if (txBuf != SKIP) {

			/* Emit the txBuf data */
			emit();

			/* The hook method which handles reply */
			ack = replyHandler(scanner.getRxBuf());			

			if(follow) {
				init();
				return;
			}

			/* When unack, it means timeout */
			if (!ack) {
				timeout();
				if (overTolerance()) {
					init();
					timeoutHandler();
				}
				return;
			}
		}

		/*
		 * If this command acks or skip flag is true, it will start
		 * subCommand(if exists) and finishHandler hook method
		 */
		if (ack || txBuf == SKIP) {
			init();
			finishHandler();
		}
	}

	/**
	 * Initial method
	 */
	private void init() {
		this.ack = false;
		this.follow = false;
		this.err_tolerance = init_tolerance;
	}

	/**
	 * Handles the process that transmits data to the outputStream, and it would
	 * wait until timeout
	 * 
	 * @throws Exception
	 */
	private void emit() throws Exception {
		synchronized (referenceLock) {
			OutputStream output = scanner.getOutputStream();
			if (output != null) {
//				Log.debug("txBuf : " + txBuf);
				output.write(txBuf);
			} else {
				 throw new NullPointerException("OutputSream is null");				
			}
			referenceLock.wait(timeout);
		}
	}

	/**
	 * Set this command timeout
	 */
	private void timeout() {
		--err_tolerance;
	}

	/**
	 * 
	 * @return does this command over timeout tolerance
	 */
	private boolean overTolerance() {
		return err_tolerance <= 0;
	}

	/**
	 * Sets the request command
	 * 
	 * @return the command data of the protocol
	 * @throws Exception
	 */
	abstract protected byte requestHandler() throws Exception;

	/**
	 * Handles the result of this command
	 * 
	 * @param rxBuf
	 *            the result of this command
	 * @return whether this command acked or not
	 * @throws Exception
	 */
	abstract protected boolean replyHandler(byte rxBuf) throws Exception;

	/**
	 * Handles how this command finishes
	 * 
	 * @throws Exception
	 */
	abstract protected void finishHandler() throws Exception;

	/**
	 * Handles how this command
	 * 
	 * @throws Exception
	 */
	abstract protected void timeoutHandler() throws Exception;

}
