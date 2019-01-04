package com.pinyougou.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2019-01-03 21:13
 */
@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Value("${pageDir}")
    private String pageDir;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;

    private Logger logger = LoggerFactory.getLogger(ItemPageServiceImpl.class);
    /**
     * 生成商品详情页
     *
     * @param goodsId
     * @return
     */
    @Override
    public Boolean getItemHtml(Long goodsId) {
        Configuration configuration = freeMarkerConfigurer.getConfiguration();
        try {
            Template template = configuration.getTemplate("item.ftl");
            //创建数据模型
            Map dataModel = new HashMap<>();

            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(goodsId);
            //1、商品主表数据
            dataModel.put("goods",tbGoods);
            TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            //2、商品扩展表数据
            dataModel.put("goodsDesc",tbGoodsDesc);
            //3、读取商品分类
            String itemCat1 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory1Id()).getName();
            String itemCat2 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory2Id()).getName();
            String itemCat3 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id()).getName();

            dataModel.put("itemCat1",itemCat1);
            dataModel.put("itemCat2",itemCat2);
            dataModel.put("itemCat3",itemCat3);

            //4、读取sku列表数据
            TbItemExample tbItemExample = new TbItemExample();
            TbItemExample.Criteria criteria = tbItemExample.createCriteria();
            //SPU ID
            criteria.andGoodsIdEqualTo(goodsId);
            //设置商品状态为 1 - 正常
            criteria.andStatusEqualTo("1");
            //按是否默认字段进行降序排序，目的是返回的结果第一条为默认的SKU
            tbItemExample.setOrderByClause("is_default desc");

            List<TbItem> itemList = itemMapper.selectByExample(tbItemExample);

            dataModel.put("itemList",itemList);

            Writer out = new FileWriter(pageDir+goodsId+".html");
            template.process(dataModel,out);
            out.close();
            return true;
        } catch (IOException | TemplateException e) {
            logger.error("页面静态化出错",e);
            return false;
        }
    }
}
