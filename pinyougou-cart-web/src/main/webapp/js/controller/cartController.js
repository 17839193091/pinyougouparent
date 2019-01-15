app.controller('cartController', function ($scope, cartService) {

    $scope.getLoginUser = function() {
        cartService.getLoginUser().success(
            function (response) {
                $scope.username = response.username;
            }
        )
    }

    //查询购物车列表
    $scope.findCartList = function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList = response;
                $scope.totalValue = cartService.sum($scope.cartList);
            }
        );
    }

    //数量加减
    $scope.addGoodsToCartList = function (itemId, num) {
        cartService.addGoodsToCartList(itemId, num).success(
            function (response) {
                if (response.success) {//如果成功
                    $scope.findCartList();//刷新列表
                } else {
                    alert(response.message);
                }
            }
        );
    }

    //获取当前登录账号的地址
    $scope.findAddressList = function () {
        cartService.findAddressList().success(
            function (response) {
                $scope.addressList = response;

                for (let i = 0; i < $scope.addressList.length; i++) {
                    if ($scope.addressList[i].isDefault == '1') {
                        $scope.address = $scope.addressList[i];
                        break;
                    }
                }
            }
        )
    }

    //选择地址
    $scope.selectAddress = function (address) {
        $scope.address = address;
    }

    //判断某地址对象是不是当前选择的地址
    $scope.isSelectedAddress = function (address) {
        return address == $scope.address;
    }

    $scope.order = {paymentType:'1'};  //订单对象

    //选择支付类型
    $scope.selectPayType = function (type) {
        $scope.order.paymentType = type;
    }

    //保存订单
    $scope.submitOrder = function () {
        $scope.order.receiverAreaName = $scope.address.address;
        $scope.order.receiverMobile = $scope.address.mobile;
        $scope.order.receiver = $scope.address.contact;

        cartService.submitOrder($scope.order).success(
            function (response) {
                if (response.success) {
                    if ($scope.order.paymentType == '1') {
                        //如果是微信支付，跳转到支付页面
                        location.href = 'pay.html';
                    } else {
                        //如果是货到付款，跳转到提示页面
                        location.href = 'paysuccess.html';
                    }
                } else {
                    alert(response.message);
                }
            }
        )
    }
});