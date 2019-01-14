package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.impl.CartService;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2019-01-12 22:20
 */

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Reference
    private CartService cartService;

    private static final Logger LOGGER = LoggerFactory.getLogger(CartController.class);

    @RequestMapping("/addGoodsToCartList")
    //@CrossOrigin(origins = {"http://localhost:9105"},allowCredentials = "true")     //springMVC 4.2之后的跨域请求注解  allowCredentials 默认为true可以不写
    public Result addGoodsToCartList(Long itemId, Integer num) {

        //允许跨域请求
        response.addHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        //允许操作cookie(服务端允许后，客户端请求体中也必须同时携带对应的信息 {'withCredentials':true} )
        response.addHeader("Access-Control-Allow-Credentials", "true");

        //获取当前登录人
        String name = SecurityContextHolder.getContext().getAuthentication().getName();


        try {
            List<Cart> cartList = findCartList();
            //调用服务方法操作购物车
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);

            if ("anonymousUser".equals(name)) {
                //未登录   将新的购物车存入cookie
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "utf-8");
            } else {
                //已登录   将新的购物车存入redis
                cartService.saveCartListToRedis(name, cartList);
            }
            return new Result(true, "存入购物车成功");
        } catch (Exception e) {
            LOGGER.error("存入购物车失败", e);
            return new Result(false, "存入购物车失败");
        }

    }

    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {

        //获取当前登录人
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        LOGGER.info("当前登录人:" + name);
        String cartListStr = CookieUtil.getCookieValue(request, "cartList", "utf-8");
        if (cartListStr == null || "".equals(cartListStr)) {
            cartListStr = "[]";
        }
        if ("anonymousUser".equals(name)) {
            LOGGER.info("从cookie中提取购物车");
            //未登录   从cookie中提取购物车
            return JSON.parseArray(cartListStr, Cart.class);
        } else {
            //已登录
            LOGGER.info("从redis中提取购物车");
            //redis中的购物车数据
            List<Cart> cartListFromRedis = cartService.findCartListFromRedis(name);
            //cookie中的购物车数据
            List<Cart> cartListFromCookie = JSON.parseArray(cartListStr, Cart.class);
            if (cartListFromCookie.size() > 0) {
                //合并购物车
                List<Cart> mergeCartList = cartService.mergeCartList(cartListFromCookie, cartListFromRedis);
                //清除本地的购物车数据
                CookieUtil.deleteCookie(request, response, "cartList");
                //保存合并后的购物车到redis
                cartService.saveCartListToRedis(name, mergeCartList);
                return mergeCartList;
            }
            return cartListFromRedis;
        }

    }
}
