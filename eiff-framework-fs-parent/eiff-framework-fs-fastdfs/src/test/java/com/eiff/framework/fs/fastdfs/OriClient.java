package com.eiff.framework.fs.fastdfs;

import java.io.IOException;

import com.eiff.framework.fs.fastdfs.common.MyException;
import com.eiff.framework.fs.fastdfs.common.NameValuePair;
import com.eiff.framework.fs.fastdfs.conf.NonGlobalConfig;

public class OriClient extends StorageClient {

	public OriClient(NonGlobalConfig config) {
		super(config);
	}
	
	@Override
	public String[] upload_file(String local_filename, String file_ext_name, NameValuePair[] meta_list)
			throws IOException, MyException {
		// TODO Auto-generated method stub
		return super.upload_file(local_filename, file_ext_name, meta_list);
	}
	
	@Override
	public String[] upload_file(String group_name, String master_filename, String prefix_name, String local_filename,
			String file_ext_name, NameValuePair[] meta_list) throws IOException, MyException {
		// TODO Auto-generated method stub
		return super.upload_file(group_name, master_filename, prefix_name, local_filename, file_ext_name, meta_list);
	}
}
