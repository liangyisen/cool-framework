package com.eiff.framework.fs.sftp.utils;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang3.StringUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * 
 */
public class ScpUtil {
    /**
     * scp 文件上传
     * 
     * @param bean  登陆远程服务器配置信息
     * @param remoteFilePath  远程文件地址，绝对地址
     * @param fis  本地文件流
     * @return
     * @throws IOException 
     */
    public static boolean upload(ScpBean bean,String remoteFilePath,FileInputStream fis) throws IOException {
        if(StringUtils.isBlank(remoteFilePath)){
        	throw new IOException("远程文件地址不能为空");
        }
        if(fis==null){
        	throw new IOException("本地文件流对象为null");
        }
        if (!bean.validate()) {
        	throw new IOException("登陆远程服务器配置信息校验失败");
        }

        Session session = null;
        Channel channel = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(bean.getUsername(), bean.getRemoteAddress(),
                bean.getRemotePort());
            ScpUserInfo userInfo = new ScpUserInfo(bean.getPassword());
            session.setUserInfo(userInfo);
            session.connect();

            // exec 'scp -t rfile' remotely
            String command = "scp " + " -t " + remoteFilePath;
            channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            if (checkAck(in) != 0) {
                throw new Exception("标准输入流读取出错！");
            }

            // send "C0644 filesize filename", where filename should not include '/'
            long filesize = fis.available();
            command = "C0644 " + filesize + " ";
            command += "\n";
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) {
                throw new Exception("标准输入流读取出错！");
            }

            // send a content of lfile
            byte[] buf = new byte[1024];
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0)
                    break;
                out.write(buf, 0, len); //out.flush();
            }

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
            if (checkAck(in) != 0) {
                throw new Exception("标准输入流读取出错！");
            }
            out.close();
            in.close();
        } catch (JSchException e) {
        	throw new IOException("Scp文件上传失败，JSchException", e);
        } catch (IOException e) {
        	throw new IOException("Scp文件上传失败，IOException", e);
        } catch (Exception e) {
        	throw new IOException("Scp文件上传失败，Exception", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                    fis = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
        return true;
    }

    /**
     * scp文件下载
     * 
     * @param bean  登陆远程服务器配置信息
     * @param remoteFilePath  远程文件地址，绝对地址
     * @param fos  本地文件输出流
     * @return
     * @throws IOException 
     */
    public static boolean download(ScpBean bean, String remoteFilePath, FileOutputStream fos) throws IOException {
        if(StringUtils.isBlank(remoteFilePath)){
        	throw new IOException("远程文件地址不能为空");
        } 
        if(fos==null){
        	throw new IOException("本地文件流对象不能为null");
        }
        if (!bean.validate()) {
        	throw new IOException("登陆远程服务器配置信息校验失败");
        }
        Session session = null;
        Channel channel = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(bean.getUsername(), bean.getRemoteAddress(),
                bean.getRemotePort());
            ScpUserInfo userInfo = new ScpUserInfo(bean.getPassword());
            session.setUserInfo(userInfo);
            session.connect();

            channel = session.openChannel("exec");
            String command = "scp -f " + remoteFilePath;
            ((ChannelExec) channel).setCommand(command);

            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            byte[] buf = new byte[1024];
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            while (true) {
                int c = checkAck(in);
                if (c != 'C') {
                    break;
                }
                in.read(buf, 0, 5);

                long filesize = 0L;
                while (true) {
                    if (in.read(buf, 0, 1) < 0) {
                        // error
                        break;
                    }
                    if (buf[0] == ' ')
                        break;
                    filesize = filesize * 10L + (long) (buf[0] - '0');
                }

                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();

                int foo;
                while (true) {
                    if (buf.length < filesize)
                        foo = buf.length;
                    else
                        foo = (int) filesize;
                    foo = in.read(buf, 0, foo);
                    if (foo < 0) {
                        // error 
                        break;
                    }
                    fos.write(buf, 0, foo);
                    filesize -= foo;
                    if (filesize == 0L)
                        break;
                }

                if (checkAck(in) != 0) {
                    throw new Exception("文件流未被完整读取！");
                }
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();
            }
            in.close();
            out.close();
        } catch (JSchException e) {
        	throw new IOException("Scp文件下载失败，JSchException：", e);
        } catch (IOException e) {
        	throw new IOException("Scp文件下载失败，IOException：", e);
        } catch (Exception e) {
        	throw new IOException("Scp文件下载失败，Exception：", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                    fos = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
        return true;
    }

    private static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if (b == 0)
            return b;
        if (b == -1)
            return b;

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            } while (c != '\n');
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }
}
