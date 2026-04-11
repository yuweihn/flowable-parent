package com.wei.web.controller.system;


import java.util.ArrayList;
import java.util.List;

import com.wei.common.constant.HttpStatus;
import com.wei.common.enums.AgsCompany;
import com.wei.system.domain.vo.DropDownVo;
import com.yuweix.kuafu.core.Response;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.wei.common.annotation.Log;
import com.wei.common.annotation.RepeatSubmit;
import com.wei.common.constant.UserConstants;
import com.wei.common.core.controller.BaseController;
import com.wei.common.core.domain.AjaxResult;
import com.wei.common.core.page.TableDataInfo;
import com.wei.common.enums.BusinessType;
import com.wei.common.utils.SecurityUtils;
import com.wei.common.utils.poi.ExcelUtil;
import com.wei.system.domain.SysConfig;
import com.wei.system.service.ISysConfigService;

import static org.springframework.web.bind.annotation.RequestMethod.GET;


/**
 * 参数配置 信息操作处理
 * 
 * @author ruoyi
 */
@RestController
public class SysConfigController extends BaseController {
    @Autowired
    private ISysConfigService configService;


    /**
     * 获取参数配置列表
     */
    @PreAuthorize("@ss.hasPermi('system:config:list')")
    @GetMapping("/system/config/list")
    public TableDataInfo list(SysConfig config) {
        startPage();
        List<SysConfig> list = configService.selectConfigList(config);
        return getDataTable(list);
    }

    @Log(title = "参数管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('system:config:export')")
    @GetMapping("/system/config/export")
    public AjaxResult export(SysConfig config) {
        List<SysConfig> list = configService.selectConfigList(config);
        ExcelUtil<SysConfig> util = new ExcelUtil<SysConfig>(SysConfig.class);
        return util.exportExcel(list, "参数数据");
    }

    /**
     * 根据参数编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:config:query')")
    @GetMapping(value = "/system/config/{configId}")
    public AjaxResult getInfo(@PathVariable Long configId) {
        return AjaxResult.success(configService.selectConfigById(configId));
    }

    /**
     * 根据参数键名查询参数值
     */
    @GetMapping(value = "/system/config/configKey/{configKey}")
    public AjaxResult getConfigKey(@PathVariable String configKey) {
        return AjaxResult.success(configService.selectConfigByKey(configKey));
    }

    /**
     * 新增参数配置
     */
    @PreAuthorize("@ss.hasPermi('system:config:add')")
    @Log(title = "参数管理", businessType = BusinessType.INSERT)
    @PostMapping(value = "/system/config")
    @RepeatSubmit
    public AjaxResult add(@Validated @RequestBody SysConfig config) {
        if (UserConstants.NOT_UNIQUE.equals(configService.checkConfigKeyUnique(config))) {
            return AjaxResult.error("新增参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        config.setCreateBy(SecurityUtils.getUsername());
        return toAjax(configService.insertConfig(config));
    }

    /**
     * 修改参数配置
     */
    @PreAuthorize("@ss.hasPermi('system:config:edit')")
    @Log(title = "参数管理", businessType = BusinessType.UPDATE)
    @PutMapping(value = "/system/config")
    public AjaxResult edit(@Validated @RequestBody SysConfig config) {
        if (UserConstants.NOT_UNIQUE.equals(configService.checkConfigKeyUnique(config))) {
            return AjaxResult.error("修改参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        config.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(configService.updateConfig(config));
    }

    /**
     * 删除参数配置
     */
    @PreAuthorize("@ss.hasPermi('system:config:remove')")
    @Log(title = "参数管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/system/config/{configIds}")
    public AjaxResult remove(@PathVariable Long[] configIds) {
        return toAjax(configService.deleteConfigByIds(configIds));
    }

    /**
     * 清空缓存
     */
    @PreAuthorize("@ss.hasPermi('system:config:remove')")
    @Log(title = "参数管理", businessType = BusinessType.CLEAN)
    @DeleteMapping("/system/config/clearCache")
    public AjaxResult clearCache() {
        configService.clearCache();
        return AjaxResult.success();
    }

    @ApiOperation(value = "乙方企业下拉列表", notes = "......")
    @RequestMapping(value = "/system/config/ags-company/drop-down-list", method = GET)
    @ResponseBody
    public Response<Integer, List<DropDownVo>> queryAgsCompanyDropDownList() {
        AgsCompany[] items = AgsCompany.values();
        List<DropDownVo> list = new ArrayList<>();
        if (items != null && items.length > 0) {
            for (AgsCompany itm: items) {
                list.add(DropDownVo.builder().id(itm.getCode()).name(itm.getName()).build());
            }
        }
        return new Response<>(HttpStatus.SUCCESS, "ok", list);
    }
}
