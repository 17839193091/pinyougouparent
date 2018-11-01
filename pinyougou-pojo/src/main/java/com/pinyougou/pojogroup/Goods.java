package com.pinyougou.pojogroup;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;

import java.io.Serializable;
import java.util.List;

/**
 * 描述:
 *  商品的组合实体类
 * @author hudongfei
 * @create 2018-10-27 21:57
 */
public class Goods implements Serializable {
    /**
     * 商品基本信息    SPU
     */
    private TbGoods goods;
    /**
     * 商品扩展信息    SPU
     */
    private TbGoodsDesc goodsDesc;
    /**
     * 商品列表        SKU
     */
    private List<TbItem> itemList;

    public List<TbItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<TbItem> itemList) {
        this.itemList = itemList;
    }

    public TbGoods getGoods() {
        return goods;
    }

    public void setGoods(TbGoods goods) {
        this.goods = goods;
    }

    public TbGoodsDesc getGoodsDesc() {
        return goodsDesc;
    }

    public void setGoodsDesc(TbGoodsDesc goodsDesc) {
        this.goodsDesc = goodsDesc;
    }
}
