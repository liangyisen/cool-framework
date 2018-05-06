package com.eiff.framework.log.api;

public class EventPair implements Cloneable {

	private String name;
	private String type;
	private boolean successful;

	/**
	 * new EventPair("PaymentChannels", "GuangYinLian") 请不要使用中文
	 * 
	 * @param name
	 * @param type
	 */
	public EventPair(String type, String name) {
		super();
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	@Override
	public EventPair clone() throws CloneNotSupportedException {
		return (EventPair) super.clone();
	}

	@Override
	public String toString() {
		return this.name + "::" + this.type;
	}
}
