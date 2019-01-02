app.controller('searchController', function ($scope, searchService) {
    //定义搜索对象的结构 category商品分类
    $scope.searchMap = {
        'keywords': '',
        'category': '',
        'brand': '',
        'spec': {},
        'price': '',
        'pageNo': '1',
        'pageSize': 20,
        'sort':'',
        'sortField':''
    };

    $scope.resultMap = {};

    //搜索方法
    $scope.search = function () {
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;
                //构建分页栏
                buildPageLabel();
                //$scope.searchMap.pageNo = '1';
            }
        )
    }

    //构建分页栏
    buildPageLabel = function () {
        //debugger;
        $scope.pageLabel = [];
        let firstPage = 1;  //开始页
        let lastPage = $scope.resultMap.totalPages; //截止页码

        if ($scope.resultMap.totalPages > 5) {
            if ($scope.searchMap.pageNo <= 3) {
                lastPage = 5;
            } else if ($scope.searchMap.pageNo >= lastPage - 2) {
                firstPage = lastPage - 4;
            } else {
                firstPage = parseInt($scope.searchMap.pageNo) - 2;
                lastPage = parseInt($scope.searchMap.pageNo) + 2;
            }
        }

        //console.log("firstPage:"+firstPage+";lastPage:"+lastPage)

        //构建页码
        for (let i = firstPage; i <= lastPage; i++) {
            $scope.pageLabel.push(i);
        }
        //console.log($scope.pageLabel)
    }

    //添加搜索项
    $scope.addItemSearch = function (key, value) {
        if (key === 'category' || key === 'brand' || key === 'price') {
            //用户点击的是分类或者品牌
            $scope.searchMap[key] = value;
        } else {
            //用户点击的是规格
            $scope.searchMap.spec[key] = value;
        }

        //console.table($scope.searchMap);
        $scope.search();
    }

    //撤销搜索项
    $scope.removeSearchItem = function (key) {
        if (key === 'category' || key === 'brand' || key === 'price') {
            //用户点击的是分类或者品牌
            $scope.searchMap[key] = "";
        } else {
            //用户点击的是规格
            delete $scope.searchMap.spec[key];
        }
        $scope.search();
    }

    $scope.queryByPage = function (pageNo) {
        if (pageNo < 1 || pageNo > $scope.resultMap.totalPages) {
            return;
        }
        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    }

    //判断当前页是否为第一页
    $scope.isTopPage = function () {
        if ($scope.searchMap.pageNo -2 <= 1) {
            return true;
        }
        return false;
    }

    //判断当前页是否为最后一页
    $scope.isEndPage = function () {
        //debugger;
        if ('totalPages' in $scope.resultMap) {
            if (parseInt($scope.searchMap.pageNo)+2 >= $scope.resultMap.totalPages) {
                return true;
            }
        }
        return false;
    }

    //排序查询
    $scope.sortSearch = function (sortField,sort) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort = sort;
        $scope.search();
    }
});