app.service('seckillGoodsService', function ($http) {
    this.findList = function () {
        return $http.get("seckillGoods/findList.do");
    }

    //查询单独商品信息
    this.findOne = function (id) {
        return $http.get("seckillGoods/findOneFromRedis.do?id="+id);
    }

    //提交订单
    this.submitOrder=function(seckillId){
        return $http.get('seckillOrder/submitOrder.do?seckillId='+seckillId);
    }
});