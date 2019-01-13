package com.pinyougou.cart.service.impl;

import com.pinyougou.pojogroup.Cart;

import java.util.List;

/**
 * 描述:
 *      购物车服务接口
 * @author hudongfei
 * @create 2019-01-12 21:12
 */
public interface CartService {
    /**
     * 添加商品到购物车列表
     * @param list
     * @param itemId
     * @param num
     * @return
     */
    List<Cart> addGoodsToCartList(List<Cart> list, Long itemId, Integer num);

    /**
     * 从Redis中获取购物车
     * @param username
     * @return
     */
    List<Cart> findCartListFromRedis(String username);

    /**
     * 将购物车存入Redis
     * @param username
     * @param cartList
     */
    void saveCartListToRedis(String username,List<Cart> cartList);
}
