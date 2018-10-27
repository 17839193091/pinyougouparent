package com.pinyougou.manager.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.Map;

/**
 * 描述:
 *  登录相关的控制器
 * @author hudongfei
 * @create 2018-10-23 12:21
 */

@RestController
@RequestMapping("/login")
public class LoginController {

    @RequestMapping("/name")
    public Map name(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String,String> map = new HashMap(16);
        map.put("loginName",name);
        return map;
    }
}
