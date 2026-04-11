package com.wei.system.service.impl;


import cn.hutool.core.io.FileUtil;
import com.wei.common.config.AppConf;
import com.wei.system.domain.vo.FileVo;
import com.wei.system.service.FileService;
import com.yuweix.kuafu.core.DateUtil;
import com.yuweix.kuafu.core.Response;
import com.yuweix.kuafu.core.cloud.CosUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.Date;


/**
 * 文件处理
 * @author yuwei
 */
@Slf4j
@Service("fileService")
public class FileServiceImpl implements FileService {
	@Resource
	private CosUtil cosUtil;
	
	
	@Override
	public Response<Boolean, FileVo> upload(byte[] fileData, String subDir, String originFileName, String fileName) {
		log.info("Upload start...");
		if (fileData == null || fileData.length <= 0) {
			log.error("Empty file.");
			return new Response<>(false, "Empty file.");
		}
		if (subDir == null || subDir.trim().equals("")) {
			subDir = "";
		} else {
			subDir = subDir.trim() + "/";
		}
		
		String key = subDir + DateUtil.formatDate(new Date(), "yyyy/MM/dd") + "/" + fileName;
		String url = cosUtil.uploadFile(fileData, key);
		String url2 = AppConf.getDomain() + "/file/download?key=" + key + "&fileName=" + originFileName;
		FileVo fileVo = FileVo.builder().fileName(originFileName).key(key).url(url).url2(url2).build();
		log.info("Upload end...");
		return Response.of(true, "ok", fileVo);
	}

	@Override
	public void download(String key, String fileName, HttpServletResponse resp) {
		if (fileName == null) {
			fileName = FileUtil.getName(key);
		}
		byte[] bytes = cosUtil.downloadFile(key);

		ByteArrayInputStream bais = null;
		BufferedInputStream bis = null;
		try {
			resp.setHeader("content-type", "application/octet-stream");
			resp.setContentType("application/octet-stream");
			resp.setCharacterEncoding("utf-8");
			resp.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
			resp.setHeader("_filename", URLEncoder.encode(fileName, "utf-8"));
			resp.setHeader("Access-Control-Expose-Headers", "_filename");

			bais = new ByteArrayInputStream(bytes);
			bis = new BufferedInputStream(bais);
			OutputStream os = resp.getOutputStream();

			byte[] buffer = new byte[1024];
			int i = bis.read(buffer);
			while (i != -1) {
				os.write(buffer, 0, i);
				i = bis.read(buffer);
			}
			os.flush();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException ignored) {
				}
			}
			if (bais != null) {
				try {
					bais.close();
				} catch (IOException ignored) {
				}
			}
		}
	}
}
