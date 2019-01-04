package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2018-12-23 22:08
 */

/**
 * timeout 超时时间 毫秒  如果服务端和消费端同时配置了timeout 则以消费端为准
 */
@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 搜索方法
     *
     * @param searchMap
     * @return
     */
    @Override
    public Map search(Map searchMap) {
        //空格处理
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords",keywords.replace(" ",""));

        Map map = new HashMap();
        /*Query query = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        ScoredPage<TbItem> tbItems = solrTemplate.queryForPage(query, TbItem.class);
        map.put("rows",tbItems.getContent());*/

        //1、查询列表
        map.putAll(searchList(searchMap));

        //2、分组查询商品分类列表
        List categoryList = searchCategoryList(searchMap);
        map.put("categoryList", categoryList);

        //3、查询品牌和规格列表
        String category = (String) searchMap.get("category");
        if (!"".equals(category)) {
            Map s = searchBrandAndSpecList(category);
            map.putAll(s);
        } else {
            if (categoryList.size() > 0) {
                Map s = searchBrandAndSpecList(String.valueOf(categoryList.get(0)));
                map.putAll(s);
            }
        }
        return map;
    }

    /**
     * 批量导入列表
     *
     * @param list
     */
    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    /**
     * 批量删除
     *
     * @param goodsIdList
     */
    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        Query solrDataQuery = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        solrDataQuery.addCriteria(criteria);
        solrTemplate.delete(solrDataQuery);
        solrTemplate.commit();
    }

    /**
     * 分组查询（查询商品分类列表）
     *
     * @return
     */
    private List searchCategoryList(Map searchMap) {
        List<String> list = new ArrayList<String>();

        Query query = new SimpleQuery("*:*");
        //关键字查询 相当于where
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //设置分组选项    相当于 group by
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category");
        query.setGroupOptions(groupOptions);

        //获取分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);

        //获取分组结果对象
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");

        //获取分组入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();

        //获取分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();

        //将分组的结果添加到返回值中
        for (GroupEntry<TbItem> tbItemGroupEntry : content) {
            list.add(tbItemGroupEntry.getGroupValue());
        }

        return list;
    }

    /**
     * 查询列表方法
     *
     * @param searchMap
     * @return
     */
    private Map searchList(Map searchMap) {
        Map map = new HashMap();

        /*
         * 高亮选项初始化
         */
        HighlightQuery highlightQuery = new SimpleHighlightQuery();

        initHighLightQueryOption(highlightQuery);

        // 1.1 关键字查询
        if (!"".equals(searchMap.get("keywords"))) {
            Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
            highlightQuery.addCriteria(criteria);
        }

        //1.2 按照商品分类进行筛选
        if (!"".equals(searchMap.get("category"))) {
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria criteria1 = new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(criteria1);
            highlightQuery.addFilterQuery(filterQuery);
        }

        //1.3 按照品牌进行筛选
        if (!"".equals(searchMap.get("brand"))) {
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria criteria1 = new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(criteria1);
            highlightQuery.addFilterQuery(filterQuery);
        }

        //1.4 按规格过滤
        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
            if (searchMap.size() > 0) {
                for (String key : specMap.keySet()) {
                    FilterQuery filterQuery = new SimpleFilterQuery();
                    Criteria criteria1 = new Criteria("item_spec_" + key).is(specMap.get(key));
                    filterQuery.addCriteria(criteria1);
                    highlightQuery.addFilterQuery(filterQuery);
                }
            }
        }

        //1.5 价格过滤
        if (!"".equals(searchMap.get("price"))) {
            String priceStr = (String) searchMap.get("price");
            String[] price = priceStr.split("-");
            //如果最低价格不等于0
            if (!"0".equals(price[0])) {
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria criteria1 = new Criteria("item_price").greaterThanEqual(price[0]);
                filterQuery.addCriteria(criteria1);
                highlightQuery.addFilterQuery(filterQuery);
            }
            //如果最高价格不等于 *
            if (!"*".equals(price[1])) {
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria criteria1 = new Criteria("item_price").lessThanEqual(price[1]);
                filterQuery.addCriteria(criteria1);
                highlightQuery.addFilterQuery(filterQuery);
            }
        }

        // 1.6 分页
        //获取页码
        Integer pageNo = 1;
        if (!"".equals(String.valueOf(searchMap.get("pageNo")))){
            pageNo = Integer.valueOf(String.valueOf(searchMap.get("pageNo")));
        }
        //获取页大小
        Integer pageSize = 20;
        if (!"".equals(String.valueOf(searchMap.get("pageSize")))){
            pageSize = Integer.valueOf(String.valueOf(searchMap.get("pageSize")));
        }

        //起始索引
        highlightQuery.setOffset((pageNo-1)*pageSize);
        //每页记录数
        highlightQuery.setRows(pageSize);

        //1.7 价格排序
        String sortValue = (String) searchMap.get("sort");
        String sortField = (String) searchMap.get("sortField");

        if (!"".equals(sortValue)){
            if ("ASC".equals(sortValue.toUpperCase())){
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                highlightQuery.addSort(sort);
            }
            if ("DESC".equals(sortValue.toUpperCase())){
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                highlightQuery.addSort(sort);
            }
        }

        //****************************获取高亮结果集***********************************
        //返回一个高亮页对象
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(highlightQuery, TbItem.class);

        //高亮入口集合(每条记录的高亮入口)
        List<HighlightEntry<TbItem>> highlighted = page.getHighlighted();

        for (HighlightEntry<TbItem> tbItemHighlightEntry : highlighted) {
            //获取高亮列表(高亮域的个数)
            List<HighlightEntry.Highlight> highlights = tbItemHighlightEntry.getHighlights();
            /*for (HighlightEntry.Highlight highlight : highlights) {
                //每个域有可能存储多值
                List<String> snipplets = highlight.getSnipplets();
                System.out.println(snipplets);
            }*/

            if (highlights.size() > 0 && highlights.get(0).getSnipplets().size() > 0) {
                TbItem item = tbItemHighlightEntry.getEntity();
                item.setTitle(highlights.get(0).getSnipplets().get(0));
            }
        }

        map.put("rows", page.getContent());

        //总页数
        map.put("totalPages",page.getTotalPages());
        //总记录数
        map.put("total",page.getTotalElements());

        return map;
    }

    private void initHighLightQueryOption(HighlightQuery highlightQuery) {
        HighlightOptions highlightOptions = new HighlightOptions();
        //高亮显示  前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //高亮显示  后缀
        highlightOptions.setSimplePostfix("</em>");

        //设置高亮显示的域
        highlightOptions.addField("item_title");

        highlightQuery.setHighlightOptions(highlightOptions);
    }

    /**
     * 根据商品分类名称查询规格和品牌列表
     *
     * @param category 商品分类名称
     * @return
     */
    private Map searchBrandAndSpecList(String category) {
        Map map = new HashMap();
        //1.根据商品名称得到模板ID
        String templateId = String.valueOf(redisTemplate.boundHashOps("itemCat").get(category));

        if (templateId != null) {
            //2.根据模板Id获取品牌列表
            Object brandListStr = redisTemplate.boundHashOps("brandList").get(templateId);
            List<Map> brandList = JSON.parseArray(String.valueOf(brandListStr), Map.class);
            map.put("brandList", brandList);
            //3.根据模板Id获取规格列表
            Object specListStr = redisTemplate.boundHashOps("specList").get(templateId);
            List<Map> specList = JSON.parseArray(String.valueOf(specListStr), Map.class);
            map.put("specList", specList);
        }

        return map;
    }
}
