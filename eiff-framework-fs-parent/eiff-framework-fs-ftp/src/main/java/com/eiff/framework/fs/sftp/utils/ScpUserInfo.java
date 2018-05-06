package com.eiff.framework.fs.sftp.utils;


import com.jcraft.jsch.UserInfo;

/**
 * 
 */
public class ScpUserInfo implements UserInfo {
    private String passwd;
    public ScpUserInfo(String passwd){
        this.passwd=passwd;
    }

    /** 
     * @see com.jcraft.jsch.UserInfo#getPassphrase()
     */
    public String getPassphrase() {
        return null;
    }

    /** 
     * @see com.jcraft.jsch.UserInfo#getPassword()
     */
    public String getPassword() {
        return this.passwd;
    }

    /** 
     * @see com.jcraft.jsch.UserInfo#promptPassword(java.lang.String)
     */
    public boolean promptPassword(String message) {
        return true;
    }

    /** 
     * @see com.jcraft.jsch.UserInfo#promptPassphrase(java.lang.String)
     */
    public boolean promptPassphrase(String message) {
        return true;
    }

    /** 
     * @see com.jcraft.jsch.UserInfo#promptYesNo(java.lang.String)
     */
    public boolean promptYesNo(String message) {
        return true;
    }

    /** 
     * @see com.jcraft.jsch.UserInfo#showMessage(java.lang.String)
     */
    public void showMessage(String message) {
    }

}
