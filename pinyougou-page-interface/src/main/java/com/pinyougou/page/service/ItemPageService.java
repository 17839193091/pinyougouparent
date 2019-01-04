package com.pinyougou.page.service;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2019-01-03 20:55
 */
public interface ItemPageService {
    /**
     * 生成商品详情页
     * @param goodsId
     * @return
     */
    public Boolean getItemHtml(Long goodsId);
}
