package com.eiff.framework.common.utils.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class IPUtil {

	/**
	 * [127.0.0.1, 0:0:0:0:0:0:0:1, fe80:0:0:0:d461:da3e:c04:a9f4%13,
	 * 172.16.100.51, fe80:0:0:0:207d:aa2:a34e:e3de%15,
	 * fe80:0:0:0:a0e3:394b:11c4:d218%16, fe80:0:0:0:485c:f1f3:a57b:430c%17,
	 * fe80:0:0:0:28e6:1026:c1ac:dc98%18, 192.168.56.1,
	 * fe80:0:0:0:285b:a590:8ab1:934b%20, fe80:0:0:0:0:ffff:ffff:fffe%28]
	 * 
	 * 
	 * @return
	 */
	public static List<String> getLocalInetAddress() {
		List<String> inetAddressList = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> enumeration = NetworkInterface
					.getNetworkInterfaces();
			while (enumeration.hasMoreElements()) {
				NetworkInterface networkInterface = enumeration.nextElement();
				Enumeration<InetAddress> addrs = networkInterface
						.getInetAddresses();
				while (addrs.hasMoreElements()) {
					inetAddressList.add(addrs.nextElement().getHostAddress());
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException("get local inet address fail", e);
		}

		return inetAddressList;
	}

	public static String localhostAddress() {
		try {
			InetAddress addr = InetAddress.getLocalHost();
			return addr.getHostAddress();
		} catch (Throwable e) {
		}
		return "";
	}
}
