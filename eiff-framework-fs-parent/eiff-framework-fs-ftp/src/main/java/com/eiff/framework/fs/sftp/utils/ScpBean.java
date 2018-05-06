package com.eiff.framework.fs.sftp.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 */
public class ScpBean {
    private String remoteAddress;
    private int    remotePort;
    private String username;
    private String password;

    public ScpBean() {
	}
    
    public ScpBean(String remoteAddress, int remotePort, String username, String password) {
		super();
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
		this.username = username;
		this.password = password;
	}

	public boolean validate() {
        if (remotePort == 0)
            remotePort = 22; //ssh默认端口22

        if (StringUtils.isBlank(remoteAddress) || StringUtils.isBlank(username)
            || StringUtils.isBlank(password) || this.remotePort <= 0) {
            return false;
        }
        return true;
    }

    /**
     * Getter method for property <tt>remoteAddress</tt>.
     * 
     * @return property value of remoteAddress
     */
    public String getRemoteAddress() {
        return remoteAddress;
    }

    /**
     * Setter method for property <tt>remoteAddress</tt>.
     * 
     * @param remoteAddress value to be assigned to property remoteAddress
     */
    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    /**
     * Getter method for property <tt>remotePort</tt>.
     * 
     * @return property value of remotePort
     */
    public int getRemotePort() {
        return remotePort;
    }

    /**
     * Setter method for property <tt>remotePort</tt>.
     * 
     * @param remotePort value to be assigned to property remotePort
     */
    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    /**
     * Getter method for property <tt>username</tt>.
     * 
     * @return property value of username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Setter method for property <tt>username</tt>.
     * 
     * @param username value to be assigned to property username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Getter method for property <tt>password</tt>.
     * 
     * @return property value of password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter method for property <tt>password</tt>.
     * 
     * @param password value to be assigned to property password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
