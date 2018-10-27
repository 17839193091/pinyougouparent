package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2018-10-20 10:45
 */
public interface BrandService {
    /**
     * 查找所有的品牌数据
     * @return
     */
    public List<TbBrand> findAll();

    /**
     * 分页查找品牌数据
     * @param pageNum   当前页面
     * @param pageSize  每页记录数
     * @return
     */
    public PageResult findPage(int pageNum,int pageSize);

    /**
     * 新增品牌数据
     * @param brand
     */
    public void add(TbBrand brand);

    /**
     * 根据Id查询实体
     * @param id
     * @return
     */
    public TbBrand findOne(Long id);

    /**
     * 修改
     * @param brand
     */
    public void update(TbBrand brand);

    /**
     * 删除
     * @param ids
     */
    public void delete(Long[] ids);

    /**
     * 分页查找品牌数据
     * @param pageNum   当前页面
     * @param pageSize  每页记录数
     * @return
     */
    public PageResult findPage(TbBrand brand,int pageNum,int pageSize);

    /**
     * 返回下拉列表数据
     * @return
     */
    public List<Map> selectOptionList();
}
