package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import util.IdWorker;

import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;

	@Autowired
	private TbSeckillGoodsMapper tbSeckillGoodsMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	@Qualifier("idWorker")
	private IdWorker idWorker;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);
	}


	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}
	}


		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();

		if(seckillOrder!=null){
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}

		}

		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 订单Id
	 * @param userId
	 * @param seckillId
	 */
	@Override
	public void submitOrder(Long seckillId,String userId) {

		//查询缓存中的商品
		BoundHashOperations hashOps = redisTemplate.boundHashOps("seckillGoods");
		String tbSeckillGoodsStr = String.valueOf(hashOps.get(seckillId.toString()));
		TbSeckillGoods tbSeckillGoods = coverClass(tbSeckillGoodsStr, TbSeckillGoods.class);
		if (tbSeckillGoods == null) {
			throw new RuntimeException("商品不存在");
		}

		if (tbSeckillGoods.getStockCount() <= 0) {
			throw new RuntimeException("商品已经被抢光");
		}

		//减少库存
		tbSeckillGoods.setStockCount(tbSeckillGoods.getStockCount() -1);
		hashOps.put(seckillId.toString(),JSON.toJSONString(tbSeckillGoods));

		if (tbSeckillGoods.getStockCount() == 0) {
			tbSeckillGoodsMapper.updateByPrimaryKey(tbSeckillGoods);
			hashOps.delete(seckillId.toString());
		}

		//存储秒杀订单(不向数据库存入,只存入缓存)
		TbSeckillOrder seckillOrder=new TbSeckillOrder();
		seckillOrder.setId(idWorker.nextId());
		seckillOrder.setSeckillId(seckillId);
		seckillOrder.setMoney(tbSeckillGoods.getCostPrice());
		seckillOrder.setUserId(userId);
		seckillOrder.setSellerId(tbSeckillGoods.getSellerId());//商家ID
		seckillOrder.setCreateTime(new Date());
		seckillOrder.setStatus("0");//状态

		BoundHashOperations orderHashOps = redisTemplate.boundHashOps("seckillOrder");
		orderHashOps.put(userId,JSON.toJSONString(seckillOrder));
	}

	/**
	 * 从缓存中提取订单
	 *
	 * @return
	 */
	@Override
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
		BoundHashOperations hashOps = redisTemplate.boundHashOps("seckillOrder");
		String orderStr = String.valueOf(hashOps.get(userId));
		return coverClass(orderStr,TbSeckillOrder.class);
	}

	/**
	 * 保存订单到数据库
	 *
	 * @param userId
	 * @param orderId
	 * @param transactionId
	 */
	@Override
	public void saveOrderFromRedisToDB(String userId, Long orderId, String transactionId) {
		//从缓存中获取订单数据
		TbSeckillOrder seckillOrder = searchOrderFromRedisByUserId(userId);

		if (seckillOrder == null) {
			throw new RuntimeException("不存在的订单");
		}

		if (!seckillOrder.getId().equals(orderId)){
			throw new RuntimeException("订单号不符");
		}
		//修改订单实体的属性
		seckillOrder.setPayTime(new Date());
		seckillOrder.setStatus("1");	//已支付状态
		seckillOrder.setTransactionId(transactionId);

		//将订单存入数据库
		seckillOrderMapper.insert(seckillOrder);

		//清除缓存中的订单
		redisTemplate.boundHashOps("seckillOrder").delete(userId);
	}

	/**
	 * 秒杀删除超时订单
	 *
	 * @param userId
	 * @param orderId
	 */
	@Override
	public void deleteOrderFromRedis(String userId, Long orderId) {
		//查询出缓存中的订单
		TbSeckillOrder seckillOrder = searchOrderFromRedisByUserId(userId);
		if (seckillOrder == null) {
			return;
		}

		//删除缓存中的订单
		redisTemplate.boundHashOps("seckillOrder").delete(userId);
		//库存回退
		String seckillGoodsStr = String.valueOf(redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId()));
		TbSeckillGoods seckillGoods = coverClass(seckillGoodsStr, TbSeckillGoods.class);
		if (seckillGoods != null) {
			seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
			redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(),JSON.toJSONString(seckillGoods));
		} else {
			seckillGoods = new TbSeckillGoods();

			seckillGoods.setId(seckillOrder.getSeckillId());
			seckillGoods.setSellerId(seckillOrder.getSellerId());
			//其他属性设置省略。。。。。。。
			seckillGoods.setStockCount(1);
			redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(),JSON.toJSONString(seckillGoods));
		}
	}

	private <T> T coverClass(String content,Class<T> clazz){
		T obj = null;
		if (!"".equals(content) && !"null".equals(content)) {
			obj = JSON.parseObject(content, clazz);
		}
		return obj;
	}


}
