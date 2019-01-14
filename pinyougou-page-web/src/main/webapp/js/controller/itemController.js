 //控制层 
 app.controller('itemController', function ($scope,$http) {
 	//数量加减
 	$scope.addNum = function (x) {
 		$scope.num += x;
 		if ($scope.num < 1) {
 			$scope.num = 1;
 		}
 	}

 	//存储用户选择的规格
 	$scope.specificationItems = {};

 	//用户选择规格调用的方法
 	$scope.selectSpecification = function (key, value) {
 		$scope.specificationItems[key] = value;
 		searchSku();
 		// console.log($scope.specificationItems);
 	}

 	//判断某规格是否被选中
 	$scope.isSelected = function (key, value) {
 		return $scope.specificationItems[key] == value;
 	}

 	//当前选择的SKU
 	$scope.sku = {};

 	//加载默认的SKU
 	$scope.loadSku = function () {
 		$scope.sku = skuList[0];
 		$scope.specificationItems = JSON.parse(JSON.stringify($scope.sku.spec));
 	}

 	//匹配两个对象是或否相等
 	matchObject = function (map1, map2) {
 		if (Object.keys(map1).length != Object.keys(map2).length) {
 			return false;
 		}

 		for (let key in map1) {
 			if (map1[key] != map2[key]) {
 				return false;
 			}
 		}

 		// for(let key in map2){
 		// 	if(map2[key] != map1[key]){
 		// 		return false;
 		// 	}
 		// }
 		return true;
 	}

 	//根据规格查找SKU
 	searchSku = function () {
 		for (let i = 0; i < skuList.length; i++) {
 			if (matchObject(skuList[i].spec, $scope.specificationItems)) {
 				$scope.sku = skuList[i];
 				return;
 			}
 		}

 		$scope.sku = {
 			'id': 0,
 			'title': '-----------',
 			'price': 0
 		}
 	}


 	//添加商品到购物车
 	$scope.addToCart = function () {
		//alert('skuId:'+$scope.sku.id);

		$http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='+$scope.sku.id+'&num='+$scope.num,{'withCredentials':true}).success(
			function (response) {
				if (response.success){
					location.href='http://localhost:9107/cart.html';
				} else {
					alert(response.message);
				}
			}
		)
 	}
 });