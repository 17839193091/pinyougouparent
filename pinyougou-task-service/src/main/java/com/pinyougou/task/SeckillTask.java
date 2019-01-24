package com.pinyougou.task;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2019-01-23 16:05
 */

@Component
public class SeckillTask {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    private Logger logger = LoggerFactory.getLogger(SeckillTask.class);
    @Scheduled(cron = "0 * * * * ?")
    public void refreshSeckillGoods() {
        logger.info("定时任务：秒杀商品增量更新调度执行-"+new Date());
        //查询缓存中的秒杀商品ID集合
        BoundHashOperations hashOps = redisTemplate.boundHashOps("seckillGoods");
        Set<String> keys = hashOps.keys();
        List<Long> goodsIds = new ArrayList<>();
        for (String key : keys) {
            goodsIds.add(Long.valueOf(key));
        }

        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        //必须审核通过的商品
        criteria.andStatusEqualTo("1");
        //库存数大于0
        criteria.andStockCountGreaterThan(0);
        //时间段
        criteria.andStartTimeLessThanOrEqualTo(new Date());	//开始日期小于等于当前日期
        criteria.andEndTimeGreaterThanOrEqualTo(new Date());//截止日期大于等于当前日期

        if (goodsIds.size() != 0) {
            criteria.andItemIdNotIn(goodsIds); //排除缓存中已经存在的商品Id集合
        }

        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);

        //将列表数据装入缓存
        for (TbSeckillGoods tbSeckillGoods : seckillGoodsList) {
            hashOps.put(tbSeckillGoods.getId().toString(), JSON.toJSONString(tbSeckillGoods));
            System.out.println("增量更新秒杀商品Id:"+tbSeckillGoods.getId());
        }
    }

    /**
     * 从缓存中移除过期的秒杀商品
     */
    @Scheduled(cron = "* * * * * ?")
    public void removeSeckillGoods() {
        logger.info("定时任务：移除缓存过期秒杀商品数据调度执行-"+new Date());
        //查询缓存中的秒杀商品ID集合
        BoundHashOperations hashOps = redisTemplate.boundHashOps("seckillGoods");
        List<String> seckillGoodsStr = hashOps.values();

        for (String str : seckillGoodsStr) {
            TbSeckillGoods tbSeckillGoods = JSON.parseObject(str, TbSeckillGoods.class);
            if (tbSeckillGoods.getEndTime().getTime() < new Date().getTime()) {
                //先同步到数据库中
                seckillGoodsMapper.updateByPrimaryKey(tbSeckillGoods);
                //从缓存中删除过期数据
                hashOps.delete(tbSeckillGoods.getId().toString());
                logger.info("秒杀商品已过期:"+tbSeckillGoods.getId());
            }
        }

    }
}
