package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Cart;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import util.IdWorker;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private TbOrderItemMapper orderItemMapper;

	@Autowired
	private TbPayLogMapper payLogMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 根据购物车列表来增加
	 */
	@Override
	public void add(TbOrder order) {
		BoundHashOperations cartListHash = redisTemplate.boundHashOps("cartList");
		String cartListStr = String.valueOf(cartListHash.get(order.getUserId()));
		List<Cart> cartList = JSON.parseArray(cartListStr, Cart.class);

		//订单Id集合
		List<String> orderIdList = new ArrayList<>();
		//总金额
		double totalMoney = 0;
		for (Cart cart : cartList) {
			TbOrder tbOrder = new TbOrder();
			long orderId = idWorker.nextId();
			tbOrder.setOrderId(orderId);
			//支付类型
			tbOrder.setPaymentType(order.getPaymentType());
			//订单状态	1：未付款
			tbOrder.setStatus("1");
			//下单时间
			tbOrder.setCreateTime(new Date());
			//更新时间
			tbOrder.setUpdateTime(new Date());
			//当前用户
			tbOrder.setUserId(order.getUserId());
			//收货人地址
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());
			//收货人电话
			tbOrder.setReceiverMobile(order.getReceiverMobile());
			//收货人
			tbOrder.setReceiver(order.getReceiver());
			//订单来源
			tbOrder.setSourceType(order.getSourceType());
			//商家Id
			tbOrder.setSellerId(cart.getSellerId());

			//BigDecimal money = new BigDecimal("0");
			double money = 0D;
			//循环购物车明细记录
			List<TbOrderItem> orderItemList = cart.getOrderItemList();
			for (TbOrderItem orderItem : orderItemList) {
				orderItem.setId(idWorker.nextId());
				orderItem.setOrderId(orderId);
				orderItem.setSellerId(cart.getSellerId());
				orderItemMapper.insert(orderItem);

				money += orderItem.getTotalFee().doubleValue();
			}
			//合计金额
			tbOrder.setPayment(new BigDecimal(String.valueOf(money)));

			orderMapper.insert(tbOrder);

			orderIdList.add(orderId+"");
			totalMoney += money;
		}

		//添加支付日志
		if ("1".equals(order.getPaymentType())) {
			TbPayLog payLog = new TbPayLog();
			//支付订单号
			payLog.setOutTradeNo(idWorker.nextId()+"");
			payLog.setCreateTime(new Date());
			payLog.setUserId(order.getUserId());
			payLog.setOrderList(orderIdList.toString().replace("[","").replace("]",""));
			//金额(单位:分)
			payLog.setTotalFee((long)totalMoney*100);
			//交易状态
			payLog.setTradeState("0");
			//支付类型 1：微信
			payLog.setPayType("1");

			payLogMapper.insert(payLog);

			redisTemplate.boundHashOps("payLog").put(order.getUserId(),JSON.toJSONString(payLog));
		}

		//清除redis购物车
		cartListHash.delete(order.getUserId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 根据userId从redis中获取支付日志信息
	 *
	 * @param userId
	 * @return
	 */
	@Override
	public TbPayLog searchPayLogFromRedis(String userId) {
		BoundHashOperations boundHashOps = redisTemplate.boundHashOps("payLog");
		String value = String.valueOf(boundHashOps.get(userId));
		TbPayLog tbPayLog = null;
		if (value != null && !"".equals(value) && !"null".equals(value)){
			tbPayLog = JSON.parseObject(value, TbPayLog.class);
		}
		return tbPayLog;
	}

	/**
	 * 支付成功修改状态
	 *
	 * @param out_trade_no
	 * @param transaction_id
	 */
	@Override
	public void updateOrderStatus(String out_trade_no, String transaction_id) {
		//1、修改支付日志的状态及相关字段
		TbPayLog tbPayLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		tbPayLog.setPayTime(new Date());
		//交易状态	1:交易成功
		tbPayLog.setTradeState("1");
		//微信交易流水号
		tbPayLog.setTransactionId(transaction_id);
		payLogMapper.updateByPrimaryKey(tbPayLog);
		//2、修改订单表的状态
		String[] orderIds = tbPayLog.getOrderList().split(",");
		for (String orderId : orderIds) {
			TbOrder order = orderMapper.selectByPrimaryKey(Long.valueOf(orderId));
			//订单状态  2:已付款
			order.setStatus("2");
			order.setPaymentTime(new Date());
			orderMapper.updateByPrimaryKey(order);
		}

		//3、清除缓存中的payLog
		BoundHashOperations payLog = redisTemplate.boundHashOps("payLog");
		payLog.delete(tbPayLog.getUserId());
	}

}
