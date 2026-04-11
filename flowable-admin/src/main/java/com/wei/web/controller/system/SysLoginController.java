package com.wei.web.controller.system;


import java.util.List;
import java.util.Set;

import com.wei.common.constant.HttpStatus;
import com.wei.common.core.domain.model.WechatLoginResult;
import com.wei.common.utils.WechatUtil;
import com.wei.system.service.ISysUserService;
import com.yuweix.kuafu.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import com.wei.common.constant.Constants;
import com.wei.common.core.domain.AjaxResult;
import com.wei.common.core.domain.entity.SysMenu;
import com.wei.common.core.domain.entity.SysUser;
import com.wei.common.core.domain.model.LoginBody;
import com.wei.common.core.domain.model.LoginUser;
import com.wei.common.utils.ServletUtils;
import com.wei.framework.web.service.SysLoginService;
import com.wei.framework.web.service.SysPermissionService;
import com.wei.framework.web.service.TokenService;
import com.wei.system.service.ISysMenuService;

import static org.springframework.web.bind.annotation.RequestMethod.POST;


/**
 * 登录验证
 * 
 * @author ruoyi
 */
@RestController
public class SysLoginController {
    @Autowired
    private SysLoginService loginService;
    @Autowired
    private ISysUserService userService;
    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private TokenService tokenService;

    @Value("${wechat.appId}")
    private String wechatAppId;
    @Value("${wechat.secret}")
    private String wechatAppSecret;



    /**
     * 登录方法
     * 
     * @param loginBody 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody)
    {
        AjaxResult ajax = AjaxResult.success();
        // 生成令牌
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    /**
     * 微信小程序登录
     */
    @RequestMapping(value = "/wechat/mini/login", method = POST)
    public Response<Integer, String> doWechatMiniLogin(@RequestParam(value = "jsCode", required = true) String jsCode
            , @RequestParam(value = "phoneNo", required = true) String phoneNo) {
        /**
         * 校验手机号码
         */
        SysUser sysUser = userService.selectUserByPhoneNo(phoneNo);
        if (sysUser == null) {
            return new Response<>(HttpStatus.ERROR, "手机号码(" + phoneNo + ")不存在");
        }

        /**
         * 校验jsCode
         */
        Response<Boolean, WechatLoginResult> resp = WechatUtil.login(wechatAppId, wechatAppSecret, jsCode);
        if (!resp.getCode()) {
            return new Response<>(HttpStatus.ERROR, resp.getMsg());
        }

        /**
         * 生成token
         */
        LoginUser loginUser = new LoginUser(sysUser, permissionService.getMenuPermission(sysUser));
        String token = tokenService.createToken(loginUser);
        return new Response<>(HttpStatus.SUCCESS, "ok", token);
    }

    /**
     * 获取用户信息
     * 
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public AjaxResult getInfo()
    {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        SysUser user = loginUser.getUser();
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        return ajax;
    }

    /**
     * 获取路由信息
     * 
     * @return 路由信息
     */
    @GetMapping("getRouters")
    public AjaxResult getRouters()
    {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        // 用户信息
        SysUser user = loginUser.getUser();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(user.getUserId());
        return AjaxResult.success(menuService.buildMenus(menus));
    }
}
