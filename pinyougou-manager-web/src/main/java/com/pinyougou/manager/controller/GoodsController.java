package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.PageResult;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.List;

/**
 * controller
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;

    /*@Reference(timeout = 100000)
    private ItemSearchService itemSearchService;*/

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    @Qualifier("queueSolrDestination")
    private Destination destination;

    @Autowired
    @Qualifier("queueSolrDeleteDestination")
    private Destination destinationDel;

    //@Reference(timeout = 400000)
    //private ItemPageService itemPageService;

    @Autowired
    @Qualifier("topicPageDestination")
    private Destination topicDestination;

    @Autowired
    @Qualifier("topicPageDeleteDestination")
    private Destination topicDeleteDestination;

    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbGoods> findAll() {
        return goodsService.findAll();
    }


    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int rows) {
        return goodsService.findPage(page, rows);
    }

    /**
     * 修改
     *
     * @param goods
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Goods goods) {
        try {
            goodsService.update(goods);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 获取实体
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Goods findOne(Long id) {
        return goodsService.findOne(id);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(final Long[] ids) {
        try {
            goodsService.delete(ids);

            //itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
            jmsTemplate.send(destinationDel, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });

            //删除每个服务器上的商品详情页
            jmsTemplate.send(topicDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });

            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 查询+分页
     *
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
        return goodsService.findPage(goods, page, rows);
    }

    /**
     * 批量修改商品状态
     *
     * @param ids
     * @param status
     */
    @RequestMapping("/updateStatus")
    public Result updateStatus(final Long[] ids, String status) {
        try {
            goodsService.updateStatus(ids, status);
            //如果商品审核通过
            if ("1".equals(status) && ids.length > 0) {
                //得到需要导入的SKU列表
                List<TbItem> itemLstByGoodsIdListAndStatus = goodsService.findItemLstByGoodsIdListAndStatus(ids, status);
                if (itemLstByGoodsIdListAndStatus.size() > 0) {
                    //导入到solr
                    //itemSearchService.importList(itemLstByGoodsIdListAndStatus);
                    final String itemLstByGoodsIdListAndStatusStr = JSON.toJSONString(itemLstByGoodsIdListAndStatus);
                    MessageCreator messageCreator = new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(itemLstByGoodsIdListAndStatusStr);
                        }
                    };
                    jmsTemplate.send(destination, messageCreator);

                    //生成商品详细页
                    /*for (Long goodsId : ids){
                        itemPageService.getItemHtml(goodsId);
                    }*/
                    jmsTemplate.send(topicDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createObjectMessage(ids);
                        }
                    });
                }
            }

            return new Result(true, "成功");
        } catch (Exception e) {
            return new Result(false, "失败");
        }

    }

}
