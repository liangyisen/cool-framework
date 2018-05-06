
package com.eiff.framework.fs.fastdfs;

import java.io.IOException;

import com.eiff.framework.fs.fastdfs.common.MyException;
import com.eiff.framework.fs.fastdfs.common.NameValuePair;
import com.eiff.framework.fs.fastdfs.conf.NonGlobalConfig;
import com.eiff.framework.fs.fastdfs.log.OperationMessage;

public class StorageCommand extends StorageClient {

	/**
	 * constructor
	 */
	public StorageCommand(NonGlobalConfig config) {
		super(config);
	}

	/**
	 * constructor
	 * 
	 * @param trackerServer
	 *            the tracker server, can be null
	 * @param storageServer
	 *            the storage server, can be null
	 */
	public StorageCommand(TrackerServer trackerServer, StorageServer storageServer, NonGlobalConfig config) {
		super(trackerServer, storageServer, config);
		if (storageServer != null) {
			OperationMessage.get().setFileSource(storageServer.getAddrWithPort());
		}
	}

	public static byte splitFileId(String file_id, String[] results) {
		int pos = file_id.indexOf(SPLIT_GROUP_NAME_AND_FILENAME_SEPERATOR);
		if ((pos <= 0) || (pos == file_id.length() - 1)) {
			return ProtoCommon.ERR_NO_EINVAL;
		}

		results[0] = file_id.substring(0, pos); // group name
		results[1] = file_id.substring(pos + 1); // file name
		return 0;
	}

	/**
	 * upload file to storage server (by file name)
	 * 
	 * @param group_name
	 *            the group name to upload file to, can be empty
	 * @param local_filename
	 *            local filename to upload
	 * @param file_ext_name
	 *            file ext name, do not include dot(.), null to extract ext name
	 *            from the local filename
	 * @param meta_list
	 *            meta info array
	 * @return file id(including group name and filename) if success, <br>
	 *         return null if fail
	 */
	public String uploadFile(String group_name, String local_filename, String file_ext_name, NameValuePair[] meta_list)
			throws IOException, MyException {
		String parts[] = this.upload_file(group_name, local_filename, file_ext_name, meta_list);
		if (parts != null) {
			return parts[0] + SPLIT_GROUP_NAME_AND_FILENAME_SEPERATOR + parts[1];
		} else {
			return null;
		}
	}

	/**
	 * upload file to storage server (by file buff)
	 * 
	 * @param group_name
	 *            the group name to upload file to, can be empty
	 * @param file_buff
	 *            file content/buff
	 * @param file_ext_name
	 *            file ext name, do not include dot(.)
	 * @param meta_list
	 *            meta info array
	 * @return file id(including group name and filename) if success, <br>
	 *         return null if fail
	 */
	public String uploadFile(String group_name, byte[] file_buff, String file_ext_name, NameValuePair[] meta_list)
			throws IOException, MyException {
		String parts[] = this.upload_file(group_name, file_buff, file_ext_name, meta_list);
		if (parts != null) {
			return parts[0] + SPLIT_GROUP_NAME_AND_FILENAME_SEPERATOR + parts[1];
		} else {
			return null;
		}
	}

	/**
	 * upload appender file to storage server (by file name)
	 * 
	 * @param group_name
	 *            the group name to upload file to, can be empty
	 * @param local_filename
	 *            local filename to upload
	 * @param file_ext_name
	 *            file ext name, do not include dot(.), null to extract ext name
	 *            from the local filename
	 * @param meta_list
	 *            meta info array
	 * @return file id(including group name and filename) if success, <br>
	 *         return null if fail
	 */
	public String uploadAppenderFile(String group_name, String local_filename, String file_ext_name,
			NameValuePair[] meta_list) throws IOException, MyException {
		String parts[] = this.upload_appender_file(group_name, local_filename, file_ext_name, meta_list);
		if (parts != null) {
			return parts[0] + SPLIT_GROUP_NAME_AND_FILENAME_SEPERATOR + parts[1];
		} else {
			return null;
		}
	}

	/**
	 * upload appender file to storage server (by file buff)
	 * 
	 * @param group_name
	 *            the group name to upload file to, can be empty
	 * @param file_buff
	 *            file content/buff
	 * @param file_ext_name
	 *            file ext name, do not include dot(.)
	 * @param meta_list
	 *            meta info array
	 * @return file id(including group name and filename) if success, <br>
	 *         return null if fail
	 */
	public String uploadAppenderFile(String group_name, byte[] file_buff, String file_ext_name,
			NameValuePair[] meta_list) throws IOException, MyException {
		String parts[] = this.upload_appender_file(group_name, file_buff, file_ext_name, meta_list);
		if (parts != null) {
			return parts[0] + SPLIT_GROUP_NAME_AND_FILENAME_SEPERATOR + parts[1];
		} else {
			return null;
		}
	}

	/**
	 * modify appender file to storage server (by file buff)
	 * 
	 * @param appender_file_id
	 *            the appender file id
	 * @param file_offset
	 *            the offset of appender file
	 * @param file_buff
	 *            file content/buff
	 * @return 0 for success, != 0 for error (error no)
	 */
	public int modifyFile(String appender_file_id, long file_offset, byte[] file_buff) throws IOException, MyException {
		String[] parts = new String[2];
		this.errno = StorageCommand.splitFileId(appender_file_id, parts);
		if (this.errno != 0) {
			return this.errno;
		}

		return this.modify_file(parts[0], parts[1], file_offset, file_buff);
	}

	/**
	 * delete file from storage server
	 * 
	 * @param file_id
	 *            the file id(including group name and filename)
	 * @return 0 for success, none zero for fail (error code)
	 */
	public int deleteFile(String file_id) throws IOException, MyException {
		String[] parts = new String[2];
		this.errno = StorageCommand.splitFileId(file_id, parts);
		if (this.errno != 0) {
			return this.errno;
		}

		return this.delete_file(parts[0], parts[1]);
	}

	/**
	 * truncate appender file to size 0 from storage server
	 * 
	 * @param appender_file_id
	 *            the appender file id
	 * @return 0 for success, none zero for fail (error code)
	 */
	public int truncateFile(String appender_file_id) throws IOException, MyException {
		String[] parts = new String[2];
		this.errno = StorageCommand.splitFileId(appender_file_id, parts);
		if (this.errno != 0) {
			return this.errno;
		}

		return this.truncate_file(parts[0], parts[1]);
	}

	/**
	 * download file from storage server
	 * 
	 * @param file_id
	 *            the file id(including group name and filename)
	 * @return file content/buffer, return null if fail
	 */
	public byte[] downloadFile(String file_id) throws IOException, MyException {
		final long file_offset = 0;
		final long download_bytes = 0;
		return this.downloadFile(file_id, file_offset, download_bytes);
	}

	/**
	 * download file from storage server
	 * 
	 * @param file_id
	 *            the file id(including group name and filename)
	 * @param file_offset
	 *            the start offset of the file
	 * @param download_bytes
	 *            download bytes, 0 for remain bytes from offset
	 * @return file content/buff, return null if fail
	 */
	public byte[] downloadFile(String file_id, long file_offset, long download_bytes) throws IOException, MyException {
		String[] parts = new String[2];
		this.errno = StorageCommand.splitFileId(file_id, parts);
		if (this.errno != 0) {
			return null;
		}

		return this.download_file(parts[0], parts[1], file_offset, download_bytes);
	}

	/**
	 * download file from storage server
	 * 
	 * @param file_id
	 *            the file id(including group name and filename)
	 * @param local_filename
	 *            the filename on local
	 * @return 0 success, return none zero errno if fail
	 */
	public int downloadFile(String file_id, String local_filename) throws IOException, MyException {
		final long file_offset = 0;
		final long download_bytes = 0;

		return this.downloadFile(file_id, file_offset, download_bytes, local_filename);
	}

	/**
	 * download file from storage server
	 * 
	 * @param file_id
	 *            the file id(including group name and filename)
	 * @param file_offset
	 *            the start offset of the file
	 * @param download_bytes
	 *            download bytes, 0 for remain bytes from offset
	 * @param local_filename
	 *            the filename on local
	 * @return 0 success, return none zero errno if fail
	 */
	public int downloadFile(String file_id, long file_offset, long download_bytes, String local_filename)
			throws IOException, MyException {
		String[] parts = new String[2];
		this.errno = StorageCommand.splitFileId(file_id, parts);
		if (this.errno != 0) {
			return this.errno;
		}

		return this.download_file(parts[0], parts[1], file_offset, download_bytes, local_filename);
	}

	/**
	 * get all metadata items from storage server
	 * 
	 * @param file_id
	 *            the file id(including group name and filename)
	 * @return meta info array, return null if fail
	 */
	public NameValuePair[] getMetadata(String file_id) throws IOException, MyException {
		String[] parts = new String[2];
		this.errno = StorageCommand.splitFileId(file_id, parts);
		if (this.errno != 0) {
			return null;
		}

		return this.get_metadata(parts[0], parts[1]);
	}

	/**
	 * get file info decoded from filename
	 * 
	 * @param file_id
	 *            the file id(including group name and filename)
	 * @return FileInfo object for success, return null for fail
	 */
	public FileInfo getFileInfo(String file_id) throws IOException, MyException {
		String[] parts = new String[2];
		this.errno = StorageCommand.splitFileId(file_id, parts);
		if (this.errno != 0) {
			return null;
		}

		return this.get_file_info(parts[0], parts[1]);
	}
}
