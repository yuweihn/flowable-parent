package com.wei.web.controller.system;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wei.common.constant.HttpStatus;
import com.wei.common.core.domain.TreeSelect;
import com.yuweix.kuafu.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.wei.common.annotation.Log;
import com.wei.common.constant.Constants;
import com.wei.common.constant.UserConstants;
import com.wei.common.core.controller.BaseController;
import com.wei.common.core.domain.entity.SysMenu;
import com.wei.common.core.domain.model.LoginUser;
import com.wei.common.enums.BusinessType;
import com.wei.common.utils.SecurityUtils;
import com.wei.common.utils.ServletUtils;
import com.wei.common.utils.StringUtils;
import com.wei.framework.web.service.TokenService;
import com.wei.system.service.ISysMenuService;

import static org.springframework.web.bind.annotation.RequestMethod.*;


/**
 * 菜单信息
 * 
 * @author ruoyi
 */
@RestController
public class SysMenuController extends BaseController {
    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private TokenService tokenService;


    /**
     * 获取菜单列表
     */
    @PreAuthorize("@ss.hasPermi('system:menu:list')")
    @RequestMapping(value = "/system/menu/list", method = GET)
    public Response<Integer, List<SysMenu>> list(SysMenu menu) {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Long userId = loginUser.getUser().getUserId();
        List<SysMenu> menus = menuService.selectMenuList(menu, userId);
        return new Response<>(HttpStatus.SUCCESS, "ok", menus);
    }

    /**
     * 根据菜单编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:menu:query')")
    @RequestMapping(value = "/system/menu/{menuId}", method = GET)
    public Response<Integer, SysMenu> getInfo(@PathVariable Long menuId) {
        SysMenu menu = menuService.selectMenuById(menuId);
        return new Response<>(HttpStatus.SUCCESS, "ok", menu);
    }

    /**
     * 获取菜单下拉树列表
     */
    @RequestMapping(value = "/system/menu/treeselect", method = GET)
    public Response<Integer, List<TreeSelect>> treeselect(SysMenu menu) {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        Long userId = loginUser.getUser().getUserId();
        List<SysMenu> menus = menuService.selectMenuList(menu, userId);
        List<TreeSelect> treeSelectList = menuService.buildMenuTreeSelect(menus);
        return new Response<>(HttpStatus.SUCCESS, "ok", treeSelectList);
    }

    /**
     * 加载对应角色菜单列表树
     */
    @RequestMapping(value = "/system/menu/roleMenuTreeselect/{roleId}", method = GET)
    public Response<Integer, Map<String, Object>> roleMenuTreeselect(@PathVariable("roleId") Long roleId) {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        List<SysMenu> menus = menuService.selectMenuList(loginUser.getUser().getUserId());
        Map<String, Object> map = new HashMap<>();
        map.put("checkedKeys", menuService.selectMenuListByRoleId(roleId));
        map.put("menus", menuService.buildMenuTreeSelect(menus));
        return new Response<>(HttpStatus.SUCCESS, "ok", map);
    }

    /**
     * 新增菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:add')")
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @RequestMapping(value = "/system/menu", method = POST)
    public Response<Integer, Integer> add(@Validated @RequestBody SysMenu menu) {
        if (UserConstants.NOT_UNIQUE.equals(menuService.checkMenuNameUnique(menu))) {
            return new Response<>(HttpStatus.ERROR, "新增菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        } else if (UserConstants.YES_FRAME.equals(menu.getIsFrame())
                && !StringUtils.startsWithAny(menu.getPath(), Constants.HTTP, Constants.HTTPS)) {
            return new Response<>(HttpStatus.ERROR, "新增菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
        }
        menu.setCreateBy(SecurityUtils.getUsername());
        return new Response<>(HttpStatus.SUCCESS, "ok", menuService.insertMenu(menu));
    }

    /**
     * 修改菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:edit')")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @RequestMapping(value = "/system/menu", method = PUT)
    public Response<Integer, Integer> edit(@Validated @RequestBody SysMenu menu) {
        if (UserConstants.NOT_UNIQUE.equals(menuService.checkMenuNameUnique(menu))) {
            return new Response<>(HttpStatus.ERROR, "修改菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        } else if (UserConstants.YES_FRAME.equals(menu.getIsFrame())
                && !StringUtils.startsWithAny(menu.getPath(), Constants.HTTP, Constants.HTTPS)) {
            return new Response<>(HttpStatus.ERROR, "修改菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
        } else if (menu.getMenuId().equals(menu.getParentId())) {
            return new Response<>(HttpStatus.ERROR, "修改菜单'" + menu.getMenuName() + "'失败，上级菜单不能选择自己");
        }
        menu.setUpdateBy(SecurityUtils.getUsername());
        return new Response<>(HttpStatus.SUCCESS, "ok", menuService.updateMenu(menu));
    }

    /**
     * 删除菜单
     */
    @PreAuthorize("@ss.hasPermi('system:menu:remove')")
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @RequestMapping(value = "/system/menu/{menuId}", method = DELETE)
    public Response<Integer, Integer> remove(@PathVariable("menuId") Long menuId) {
        if (menuService.hasChildByMenuId(menuId)) {
            return new Response<>(HttpStatus.ERROR, "存在子菜单,不允许删除");
        }
        if (menuService.checkMenuExistRole(menuId)) {
            return new Response<>(HttpStatus.ERROR, "菜单已分配,不允许删除");
        }
        return new Response<>(HttpStatus.SUCCESS, "ok", menuService.deleteMenuById(menuId));
    }
}