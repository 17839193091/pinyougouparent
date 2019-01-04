package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;
import entity.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class ContentServiceImpl implements ContentService {
    private static final Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbContentMapper contentMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbContent> findAll() {
        return contentMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbContent content) {
        contentMapper.insert(content);
        //清除原分组缓存
        redisTemplate.boundHashOps("content").delete(String.valueOf(content.getCategoryId()));
    }


    /**
     * 修改
     */
    @Override
    public void update(TbContent content) {
        //查询原来的分组Id
        Long categoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
        //清除原分组缓存
        redisTemplate.boundHashOps("content").delete(String.valueOf(categoryId));

        contentMapper.updateByPrimaryKey(content);
        //清除现分组缓存
        if (categoryId.longValue() != content.getCategoryId().longValue()){
            redisTemplate.boundHashOps("content").delete(String.valueOf(content.getCategoryId()));
        }
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbContent findOne(Long id) {
        return contentMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();
            redisTemplate.boundHashOps("content").delete(String.valueOf(categoryId));
            //删除完就查不出来categoryId了
            contentMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbContent content, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbContentExample example = new TbContentExample();
        Criteria criteria = example.createCriteria();

        if (content != null) {
            if (content.getTitle() != null && content.getTitle().length() > 0) {
                criteria.andTitleLike("%" + content.getTitle() + "%");
            }
            if (content.getUrl() != null && content.getUrl().length() > 0) {
                criteria.andUrlLike("%" + content.getUrl() + "%");
            }
            if (content.getPic() != null && content.getPic().length() > 0) {
                criteria.andPicLike("%" + content.getPic() + "%");
            }
            if (content.getStatus() != null && content.getStatus().length() > 0) {
                criteria.andStatusLike("%" + content.getStatus() + "%");
            }

        }

        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据广告分类Id查询广告列表
     *
     * @param categoryId
     * @return
     */
    @Override
    public List<TbContent> findByCategoryId(Long categoryId) {
        List<TbContent> content = null;
        BoundHashOperations contentHashOps = redisTemplate.boundHashOps("content");
        if (contentHashOps.get(String.valueOf(categoryId)) != null){
            content = JSON.parseArray(String.valueOf(redisTemplate.boundHashOps("content").get(String.valueOf(categoryId))),TbContent.class);
        }
        logger.info("从缓存中获取数据:"+content);
        if (content == null) {
            TbContentExample example = new TbContentExample();
            Criteria criteria = example.createCriteria();
            //指定条件: 分类Id
            criteria.andCategoryIdEqualTo(categoryId);
            //指定条件：有效
            criteria.andStatusEqualTo("1");
            //指定条件：排序
            example.setOrderByClause("sort_order");
            content = contentMapper.selectByExample(example);
            redisTemplate.boundHashOps("content").put(String.valueOf(categoryId), JSON.toJSONString(content));
            logger.info("从数据库中获取数据:"+content);
        }
        return content;
    }

}
