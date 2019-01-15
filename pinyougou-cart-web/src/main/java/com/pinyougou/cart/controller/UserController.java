package com.pinyougou.cart.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2019-01-14 20:46
 */

@RestController
@RequestMapping("/user")
public class UserController {
    @RequestMapping("/getLoginUser")
    public Map<String,String> getLoginUser() {
        Map<String,String> map = new HashMap<>();
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("username",name);
        return map;
    }
}
