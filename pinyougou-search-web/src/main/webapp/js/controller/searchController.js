app.controller('searchController',function($scope,searchService){

    //搜索方法
    $scope.search = function () {
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;
            }
        )
    }
});