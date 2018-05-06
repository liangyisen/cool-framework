package com.eiff.framework.fs.fastdfs.token;

public class AntiStealToken {
	private int ts;
	private String token;

	public AntiStealToken(int ts, String token) {
		this.ts = ts;
		this.token = token;
	}

	public int getTs() {
		return ts;
	}

	public void setTs(int ts) {
		this.ts = ts;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTokenUrlParam() {
		return "ts=" + this.ts + "&token=" + this.token;
	}

	public static AntiStealToken emptyToken() {
		return new AntiStealToken(0, "");
	}
}
