package com.wei.web.controller.common;


import com.wei.common.constant.HttpStatus;
import com.wei.system.domain.vo.FileVo;
import com.wei.system.service.FileService;
import com.yuweix.kuafu.core.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;


/**
 * @author yuwei
 */
@Api(tags = {"文件"})
@Controller
public class FileController {
	@Resource
	private FileService fileService;
	
	
	/**
	 * 文件上传
	 * 返回访问路径
	 **/
	@ApiOperation(value = "文件上传", notes = "......")
	@RequestMapping(value = "/file/upload", method = POST)
	@ResponseBody
	public Response<Integer, FileVo> upload(@RequestParam(value = "file", required = true) MultipartFile file
			, @RequestParam(value = "subDir", required = false) String subDir) throws Exception {
		String originFileName = file.getOriginalFilename().toLowerCase();
		String extName = originFileName.lastIndexOf(".") > -1
				? originFileName.substring(originFileName.lastIndexOf("."))
				: "";
		String fileName = UUID.randomUUID().toString().replace("-", "") + extName;
		Response<Boolean, FileVo> resp = fileService.upload(file.getBytes(), subDir, originFileName, fileName);
		if (!resp.getCode()) {
			return new Response<>(HttpStatus.ERROR, resp.getMsg());
		}
		return new Response<>(HttpStatus.SUCCESS, "ok", resp.getData());
	}

	@ApiOperation(value = "文件下载", notes = "......")
	@RequestMapping(value = "/file/download", method = {GET, POST})
	public void download(@RequestParam(value = "key", required = true) String key
			, @RequestParam(value = "fileName", required = false) String fileName, HttpServletResponse resp) {
		fileService.download(key, fileName, resp);
	}
}
