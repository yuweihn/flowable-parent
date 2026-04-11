package com.wei.system.service;


import com.wei.system.domain.vo.FileVo;
import com.yuweix.kuafu.core.Response;

import javax.servlet.http.HttpServletResponse;


/**
 * 文件处理
 * @author yuwei
 */
public interface FileService {
	/**
	 * 文件上传
	 * 返回文件存储路径和访问地址
	 *
	 * subDir    保存文件的子目录，字符串前后不含斜杠
	 * fileName  文件名(包含扩展名，如"abc.txt")
	 **/
	Response<Boolean, FileVo> upload(byte[] fileData, String subDir, String originFileName, String fileName);

	/**
	 * 文件下载
	 **/
	void download(String key, String fileName, HttpServletResponse resp);
}
