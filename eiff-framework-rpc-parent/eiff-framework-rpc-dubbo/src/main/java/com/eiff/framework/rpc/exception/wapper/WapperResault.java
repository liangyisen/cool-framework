package com.eiff.framework.rpc.exception.wapper;

public class WapperResault {

	private Throwable throwable;
	private boolean monitored;
	private boolean loggable;

	public WapperResault(Throwable throwable, boolean monitored) {
		super();
		this.throwable = throwable;
		this.monitored = monitored;
	}

	public WapperResault(Throwable throwable) {
		super();
		this.throwable = throwable;
		this.monitored = true;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}

	public boolean isMonitored() {
		return monitored;
	}

	public void setMonitored(boolean monitored) {
		this.monitored = monitored;
	}

	public boolean isLoggable() {
		return loggable;
	}

	public void setLoggable(boolean loggable) {
		this.loggable = loggable;
	}
}
