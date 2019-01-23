app.controller('seckillGoodsController',function ($scope,$location,$interval,seckillGoodsService) {
    $scope.findList = function () {
        seckillGoodsService.findList().success(
            function (response) {
                $scope.list = response;
            }
        )
    };

    $scope.findOne = function () {
        let id = $location.search()['id'];
        seckillGoodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;

                //倒计时
                let allSecond = Math.floor((new Date($scope.entity.endTime).getTime() - new Date().getTime())/1000);

                time = $interval(function () {
                    allSecond -= 1;
                    $scope.second = convertTimeString(allSecond);
                    if ($scope.second <= 0) {
                        $interval.cancel(time);
                    }
                },1000);
            }
        )
    }

    convertTimeString = function (allSecond) {
        let days = Math.floor(allSecond/(60*60*24));
        let hours = Math.floor((allSecond - days*60*60*24)/(60*60));
        let minutes = Math.floor((allSecond - days*60*60*24 - hours*60*60)/60);
        let second = Math.floor(allSecond - days*60*60*24 - hours*60*60 - minutes*60);

        let timeString = "";
        if (days > 0) {
            timeString += days+"天 ";
        }
        return timeString + hours+":"+ minutes + ":" + second;
    }

    //提交订单
    $scope.submitOrder=function(){
        seckillGoodsService.submitOrder($scope.entity.id).success(
            function(response){
                if(response.success){
                    alert("下单成功，请在1分钟内完成支付");
                    location.href="pay.html";
                }else{
                    alert(response.message);
                }
            }
        );
    }
});