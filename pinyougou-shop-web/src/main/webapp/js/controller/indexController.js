app.controller('indexController',function ($scope,$controller,loginService) {
    $controller('baseController',{$scope:$scope});//继承

    //显示当前用户名
    $scope.showLoginName = function () {
        console.log("获取用户名")
        loginService.loginName().success(
            function (response) {
                $scope.loginName = response.loginName;
            }
        )
    }
});