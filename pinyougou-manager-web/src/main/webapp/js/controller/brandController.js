app.controller('brandController',function($scope,$controller,brandService){

    //伪继承
    $controller('baseController',{$scope:$scope})

    //查询品牌列表
    $scope.findAll=function(){
        brandService.findAll().success(
            function(response){
                $scope.list=response;
            }
        );
    }

    //分页
    $scope.findPage=function(page,size){
        brandService.findPage(page,size).success(
            function(response){
                $scope.list=response.rows;//显示当前页数据
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }

    //新增
    $scope.save = function () {
        var object = null;
        if ($scope.entity.id != null){
            object = brandService.update($scope.entity);
        } else{
            object = brandService.add($scope.entity);
        }
        object.success(
            function (response) {
                console.log(response)
                if(response.success){
                    $scope.reloadList();    //刷新
                } else {
                    alert(response.message)
                }
            }
        )
    }

    //查询实体
    $scope.findOne = function (id) {
        brandService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        )
    }

    $scope.dele = function () {
        if ($scope.selectIds.length === 0){
            alert("请选择要删除的项");
            return;
        }
        brandService.dele($scope.selectIds).success(
            function (response) {
                if(response.success){
                    $scope.reloadList();    //刷新
                } else {
                    alert(response.message)
                }
            }
        )
    }

    $scope.searchEntity = {};
    $scope.search = function (page,size) {
        brandService.search($scope.searchEntity,page,size).success(
            function(response){
                $scope.list=response.rows;//显示当前页数据
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }
})