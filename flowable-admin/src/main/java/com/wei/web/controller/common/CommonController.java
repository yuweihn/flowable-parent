package com.wei.web.controller.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;


/**
 * 通用请求处理
 * 
 * @author ruoyi
 */
@Deprecated
@RestController
public class CommonController {
    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

//    /**
//     * 通用下载请求
//     *
//     * @param fileName 文件名称
//     * @param delete 是否删除
//     */
//    @GetMapping("/common/download")
//    public void fileDownload(String fileName, Boolean delete, HttpServletResponse response, HttpServletRequest request) {
//        try {
//            if (!FileUtils.checkAllowDownload(fileName)) {
//                throw new Exception(StringUtils.format("文件名称({})非法，不允许下载。 ", fileName));
//            }
//            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
//            String filePath = AppConf.getDownloadPath() + fileName;
//
//            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
//            FileUtils.setAttachmentResponseHeader(response, realFileName);
//            FileUtils.writeBytes(filePath, response.getOutputStream());
//            if (delete) {
//                FileUtils.deleteFile(filePath);
//            }
//        } catch (Exception e) {
//            log.error("下载文件失败", e);
//        }
//    }

//    /**
//     * 通用上传请求
//     */
//    @PostMapping("/common/upload")
//    public AjaxResult uploadFile(MultipartFile file) throws Exception {
//        try {
//            // 上传文件路径
//            String filePath = AppConf.getUploadPath();
//            // 上传并返回新文件名称
//            String fileName = FileUploadUtils.upload(filePath, file);
//            String url = AppConf.getDomain() + "/" + fileName;
//            AjaxResult ajax = AjaxResult.success();
//            ajax.put("fileName", fileName);
//            ajax.put("url", url);
//            return ajax;
//        } catch (Exception e) {
//            return AjaxResult.error(e.getMessage());
//        }
//    }

//    /**
//     * 本地资源通用下载
//     */
//    @GetMapping("/common/download/resource")
//    public void resourceDownload(String resource, HttpServletRequest request, HttpServletResponse response)
//            throws Exception {
//        try {
//            if (!FileUtils.checkAllowDownload(resource)) {
//                throw new Exception(StringUtils.format("资源文件({})非法，不允许下载。 ", resource));
//            }
//            // 本地资源路径
//            String localPath = AppConf.getProfile();
//            // 数据库资源地址
//            String downloadPath = localPath + StringUtils.substringAfter(resource, Constants.RESOURCE_PREFIX);
//            // 下载名称
//            String downloadName = StringUtils.substringAfterLast(downloadPath, "/");
//            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
//            FileUtils.setAttachmentResponseHeader(response, downloadName);
//            FileUtils.writeBytes(downloadPath, response.getOutputStream());
//        } catch (Exception e) {
//            log.error("下载文件失败", e);
//        }
//    }
}
