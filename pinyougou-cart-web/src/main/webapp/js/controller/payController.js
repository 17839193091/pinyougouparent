app.controller('payController',function ($scope,$location,payService) {
   $scope.createNative = function () {
       payService.createNative().success(
           function (response) {
               //显示订单号和金额
               $scope.money = (response.total_fee/100).toFixed(2);
               $scope.out_trade_no = response.out_trade_no;

               /*var qr = new QRious({
                   element:document.getElementById("qrious"),
                   size:250,
                   value:response.code_url,
                   level:'H'
               });*/

               var qrcode = new QRCode("qrcode", {
                   text: response.code_url,
                   width: 250,
                   height: 250,
                   colorDark : "#000000",
                   colorLight : "#ffffff",
                   correctLevel : QRCode.CorrectLevel.H
               });
               queryPayStatus();
           }
       )
   }

   queryPayStatus = function () {
       payService.queryPayStatus($scope.out_trade_no).success(
           function (response) {
               if (response.success){
                   location.href = "paysuccess.html#?money=" + $scope.money;
               } else {
                   if (response.message == "支付超时") {
                       //重新生成二维码信息
                       $scope.createNative();
                   } else {
                       location.href = "payfail.html";
                   }
               }
           }
       )
   }

   $scope.getMoney = function () {
        return $location.search()['money'];
   }
});