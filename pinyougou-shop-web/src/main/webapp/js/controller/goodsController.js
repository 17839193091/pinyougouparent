 //控制层 
app.controller('goodsController' ,function($scope,$controller,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}

    //增加
    $scope.add=function(){
	    $scope.entity.goodsDesc.introduction = editor.html();

        goodsService.add($scope.entity).success(
            function(response){
                if(response.success){
                    alert("新增成功");
                    $scope.entity = {};
                    //清空富文本编辑器
                    editor.html("");
                }else{
                    alert(response.message);
                }
            }
        );
    }
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

    $scope.image_entity = {};
	//上传图片
	$scope.uploadFile = function () {
		uploadService.uploadFile().success(
		    function (response) {
                if (response.success){
                    $scope.image_entity.url = response.message;
                } else {
                    alert(response.message);
                }
            }
        )
    }
    //组合实体类
    $scope.entity = {goods:{},goodsDesc : {itemImages:[]}};
    //将当前上传的图片实体存入图片列表
    $scope.add_image_entity = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }

    //移除图片
    $scope.remove_image_entity = function (index) {

        var url = $scope.entity.goodsDesc.itemImages[index].url;
        console.log(url.substr(22));
        uploadService.deleFile(url.substr(22)).success(
            function (response) {
                if (response.success && response.message === "0"){
                    alert("删除成功");
                } else {
                    alert("删除失败");
                }
            }
        )
        $scope.entity.goodsDesc.itemImages.splice(index,1);

    }

    //查询一级商品分类列表
    $scope.selectItemCat1List = function () {
        itemCatService.findByParentId("0").success(
            function (response) {
                $scope.itemCat1List = response;
            }
        )
    }

    //查询二级分类列表
    $scope.$watch('entity.goods.category1Id',function (newValue,oldValue) {
        $scope.entity.goods.typeTemplateId = null;
        $scope.itemCat3List = null;
            itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat2List = response;
            }
        )
    });

    //查询三级分类列表
    $scope.$watch('entity.goods.category2Id',function (newValue,oldValue) {
        $scope.entity.goods.typeTemplateId = null;
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat3List = response;
            }
        )
    });

    $scope.typeTemplate = {brandIds:[]};

    //读取模板Id
    $scope.$watch('entity.goods.category3Id',function (newValue,oldValue) {
        $scope.typeTemplate.brandIds = null;
        itemCatService.findOne(newValue).success(
            function (response) {
                $scope.entity.goods.typeTemplateId = response.typeId;
            }
        )
    });

    //读取模板Id后读取品牌列表
    $scope.$watch('entity.goods.typeTemplateId',function (newValue,oldValue) {
        typeTemplateService.findOne(newValue).success(
            function (response) {
                $scope.typeTemplate = response;
                //品牌列表类型转换
                $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);
            }
        )
    });
});	
