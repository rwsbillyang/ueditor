package com.baidu.ueditor.upload;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.ueditor.ConfigManager;
import com.baidu.ueditor.define.AppInfo;
import com.baidu.ueditor.define.BaseState;
import com.baidu.ueditor.define.State;
import com.utils.CosUtil;

public class StorageManager {
	private static Logger log = LoggerFactory.getLogger(StorageManager.class);
	
	public static final int BUFFER_SIZE = 8192;

	public StorageManager() {
	}

	/**
	 * @path physicalPath
	 * @cosKey formattedSavePath
	 * */
	public static State saveBinaryFile(byte[] data, String path,String formattedSavePath) {
		File file = new File(path);

		State state = valid(file);

		if (!state.isSuccess()) {
			return state;
		}

		try {
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(file));
			bos.write(data);
			bos.flush();
			bos.close();
		} catch (IOException ioe) {
			return new BaseState(false, AppInfo.IO_ERROR);
		}

		if(ConfigManager.ENABLE_COS)
		{
			return uploadToCOS(file, formattedSavePath);	
		}
		
		state = new BaseState(true, file.getAbsolutePath());
		state.putInfo("url", formattedSavePath);
		state.putInfo( "size", data.length );
		state.putInfo( "title", file.getName() );
		return state;
	}

	public static State saveFileByInputStream(InputStream is, String path,
			long maxSize,String formattedSavePath) {
		State state = null;

		File tmpFile = getTmpFile();

		byte[] dataBuf = new byte[ 2048 ];
		BufferedInputStream bis = new BufferedInputStream(is, StorageManager.BUFFER_SIZE);

		try {
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(tmpFile), StorageManager.BUFFER_SIZE);

			int count = 0;
			while ((count = bis.read(dataBuf)) != -1) {
				bos.write(dataBuf, 0, count);
			}
			bos.flush();
			bos.close();

			if (tmpFile.length() > maxSize) {
				tmpFile.delete();
				return new BaseState(false, AppInfo.MAX_SIZE);
			}

			state = saveTmpFile(tmpFile, path,formattedSavePath);

			if (!state.isSuccess()) {
				tmpFile.delete();
			}

			return state;
			
		} catch (IOException e) {
		}
		return new BaseState(false, AppInfo.IO_ERROR);
	}

	public static State saveFileByInputStream(InputStream is, String path,String cosKey) {
		State state = null;

		File tmpFile = getTmpFile();

		byte[] dataBuf = new byte[ 2048 ];
		BufferedInputStream bis = new BufferedInputStream(is, StorageManager.BUFFER_SIZE);

		try {
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(tmpFile), StorageManager.BUFFER_SIZE);

			int count = 0;
			while ((count = bis.read(dataBuf)) != -1) {
				bos.write(dataBuf, 0, count);
			}
			bos.flush();
			bos.close();

			state = saveTmpFile(tmpFile, path,cosKey);

			if (!state.isSuccess()) {
				tmpFile.delete();
			}

			return state;
		} catch (IOException e) {
		}
		return new BaseState(false, AppInfo.IO_ERROR);
	}
	
	private static File getTmpFile() {
		File tmpDir = FileUtils.getTempDirectory();
		String tmpFileName = (Math.random() * 10000 + "").replace(".", "");
		return new File(tmpDir, tmpFileName);
	}



	private static State valid(File file) {
		File parentPath = file.getParentFile();

		if ((!parentPath.exists()) && (!parentPath.mkdirs())) {
			return new BaseState(false, AppInfo.FAILED_CREATE_FILE);
		}

		if (!parentPath.canWrite()) {
			return new BaseState(false, AppInfo.PERMISSION_DENIED);
		}

		return new BaseState(true);
	}
	
	//激活cos了话，将上传至cos
	private static State saveTmpFile(File tmpFile, String targetFilePath,String formattedSavePath) {
		return moveTmpFileToTarge(tmpFile, targetFilePath,  formattedSavePath);
	}
	
	private static BaseState moveTmpFileToTarge(File tmpFile, String targetFilePath, String formattedSavePath) {
		File targetFile = new File(targetFilePath);
		try {
			if (targetFile.exists()) {
				if (targetFile.canWrite()) {
					FileUtils.moveFile(tmpFile, targetFile);
				} else {
					log.error("file exsit,and can't overwrite: targetFilePath=" + targetFilePath);
					return new BaseState(false, AppInfo.IO_ERROR);
				}
			} else {
				FileUtils.moveFile(tmpFile, targetFile);
			}
			if(ConfigManager.ENABLE_COS)
			{
				return uploadToCOS(targetFile, formattedSavePath);	
			}
		} catch (SecurityException se) {
			log.error("SecurityException: targetFilePath=" + targetFilePath);
			return new BaseState(false, AppInfo.PERMISSION_DENIED);
		} catch (IOException e) {
			log.error("IOException: targetFilePath=" + targetFilePath);
			return new BaseState(false, AppInfo.IO_ERROR);
		}
		BaseState state = new BaseState(true);
		state.putInfo("url", formattedSavePath);
		state.putInfo("size", targetFile.length());
		state.putInfo("title", targetFile.getName());
		log.info("successfull to upload:" + state.toJSONString());
		return state;
	}
	
	private static BaseState uploadToCOS(File file,String formattedSavePath)
	{
		String cosKey = formattedSavePath;
		if(cosKey.startsWith("/")) cosKey = cosKey.substring(1);	
		
		String url = CosUtil.upload(file, cosKey);
		if(url==null) { 
			log.error("fail to upload file to cos,file="+file.getAbsolutePath());
			return new BaseState(false, AppInfo.COS_UPLOAD_ERROR);	
		}else
		{
			BaseState state = new BaseState(true);
			state.putInfo("url", url);
			state.putInfo( "size", file.length() );
			state.putInfo( "title", file.getName() );
			log.info("successfull to upload:" + state.toJSONString());
			return state;
		}		
	}
}
