package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2018-12-23 22:04
 */
public interface ItemSearchService {

    /**
     * 搜索方法
     * @param searchMap
     * @return
     */
    public Map search(Map searchMap);

    /**
     *  批量导入列表
     */
    public void importList(List list);

    /**
     * 批量删除 (SPU ID)
     * @param goodsIdList
     */
    public void deleteByGoodsIds(List goodsIdList);
}
