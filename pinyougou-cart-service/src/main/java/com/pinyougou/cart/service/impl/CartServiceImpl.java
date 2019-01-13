package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述:
 * 购物车服务接口
 *
 * @author hudongfei
 * @create 2019-01-12 21:12
 */
@Service(timeout = 10000)
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(CartServiceImpl.class);

    /**
     * 添加商品到购物车列表
     *
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        if (cartList == null) {
            cartList = new ArrayList<>();
        }

        //根据skuId查询商品明细对象
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        //根据sku对象得到商家Id
        if (!"1".equals(item.getStatus())){
            throw new RuntimeException("商品状态不合法");
        }
        String sellerId = item.getSellerId();
        //根据商家Id在购物车列表中查询购物车对象
        Cart cart = searchCartBySellerId(cartList, sellerId);

        if (cart == null) {
            //如果购物车列表中不存在该商家的购物车
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            List<TbOrderItem> orderItemList = new ArrayList<>();
            TbOrderItem orderItem = createOrderItem(num, item);
            orderItemList.add(orderItem);

            cart.setOrderItemList(orderItemList);

            //将新的购物车对象放入购物车列表中
            cartList.add(cart);
        } else {
            //如果购物车列表中存在该商家的购物车
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if (orderItem == null) {
                //如果不存在，创建新的购物车明细对象，并添加到该购物车明细列表中
                orderItem = createOrderItem(num, item);
                cart.getOrderItemList().add(orderItem);
            } else {
                //如果存在，在原有的数量上添加数量，并更新金额
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(orderItem.getPrice().multiply(new BigDecimal(String.valueOf(orderItem.getNum()))));

                //当明细的数量<=0,移除此明细项
                if (orderItem.getNum() <= 0) {
                    cart.getOrderItemList().remove(orderItem);
                }

                //当购物车的明细数量为0，在购物车列表中移除此购物车
                if (cart.getOrderItemList().size() == 0) {
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    /**
     * 从Redis中获取购物车
     *
     * @param username
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        LOGGER.info("从Redis中获取购物车");
        List<Cart> carts = new ArrayList<>();
        BoundHashOperations cartList = redisTemplate.boundHashOps("cartList");
        String cartListStr = String.valueOf(cartList.get(username));
        if (!"null".equals(cartListStr)) {
            carts = JSON.parseArray(cartListStr,Cart.class);
        }
        LOGGER.info("购物车:"+carts);
        return carts;
    }

    /**
     * 将购物车存入Redis
     *
     * @param username
     * @param cartList
     */
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        LOGGER.info("向Redis中存入购物车");
        BoundHashOperations cartListRedisHash = redisTemplate.boundHashOps("cartList");
        cartListRedisHash.put(username,JSON.toJSONString(cartList));
    }

    /**
     * 根据skuID在购物车明细列表中查询购物车明细对象
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId){
        if (orderItemList == null) {
            return null;
        }
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().longValue() == itemId.longValue()) {
                return orderItem;
            }
        }
        return null;
    }

    //创建新的购物车明细对象
    private TbOrderItem createOrderItem(Integer num, TbItem item) {
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(item.getPrice().multiply(new BigDecimal(String.valueOf(num))));
        return orderItem;
    }

    /**
     * 根据商家Id在购物车列表中查询购物车是否存在
     * @param list
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> list,String sellerId){
        if (list == null) {
            return null;
        }

        for (Cart cart : list) {
            if (cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }
}
