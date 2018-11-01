app.service('uploadService',function ($http) {
    this.uploadFile = function () {
        //html5中新增的 FormData
        var formData = new FormData();

        // file : 文件上传框的name
        //  file.files[0] 取第一个文件上传框
        formData.append('file',file.files[0]);
        return $http({
            url : '../upload.do',
            method : 'post',
            data : formData,
            // 默认为json类型 设置 headers : {'Content-Type' : undefined} 之后可以修改为文件上传的类型
            headers : {'Content-Type' : undefined},
            //angular 提供的，对整个表单进行二进制的序列化 transformRequest : angular.identity
            transformRequest : angular.identity
        });
    }
    
    this.deleFile = function (fileId) {
        return $http.get("../deleFile.do?fileId="+fileId);
    }
})