package com.baidu.ueditor;

import java.io.File;

public interface CloudStorageInteface {
	/**
	 * @param file 待上传的文件
	 * @param key 上传到云存储的key
	 * @return 返回对象的访问路径url，若为null则表示失败
	 * */
	public  String upload(File file,String key);
}
