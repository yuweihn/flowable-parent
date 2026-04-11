package com.wei.web.controller.system;


import com.wei.common.constant.HttpStatus;
import com.wei.system.domain.vo.ProductOptionVo;
import com.yuweix.kuafu.core.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;


/**
 * @author yuwei
 */
@Api(tags = {"系统产品信息"})
@Controller
public class SysProductController {
    @ApiOperation(value = "标准产品端口列表", notes = "......")
    @RequestMapping(value = "/system/product/port/options", method = GET)
    @ResponseBody
    public Response<Integer, List<ProductOptionVo>> queryProductPortOptions() {
        return new Response<>(HttpStatus.SUCCESS, "ok", ProductOptionVo.PORT_LIST);
    }

    @ApiOperation(value = "标准产品设备列表", notes = "......")
    @RequestMapping(value = "/system/product/device/options", method = GET)
    @ResponseBody
    public Response<Integer, List<ProductOptionVo>> queryProductDeviceOptions() {
        return new Response<>(HttpStatus.SUCCESS, "ok", ProductOptionVo.DEVICE_LIST);
    }
}
