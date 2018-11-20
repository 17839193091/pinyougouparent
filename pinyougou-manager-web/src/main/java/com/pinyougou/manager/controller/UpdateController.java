package com.pinyougou.manager.controller;

import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import util.FastDFSClient;

/**
 * 描述:
 *  文件上传
 * @author hudongfei
 * @create 2018-10-30 12:33
 */

@RestController
public class UpdateController {

    @Value("${FILE_SERVER_URL}")
    private String file_server_url;

    @RequestMapping("/upload")
    public Result uoload(MultipartFile file){
        //获取文件名
        String filename = file.getOriginalFilename();
        //得到扩展名
        String extName = filename.substring(filename.lastIndexOf(".")+1);

        try {
            FastDFSClient client = new FastDFSClient("classpath:config/fdfs_client.conf");
            String fileId = client.uploadFile(file.getBytes(), extName);

            //文件的完整地址
            String url = file_server_url + fileId;
            return new Result(true,url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }
    }

    @RequestMapping("/deleFile")
    public Result deleFile(String fileId){
        try {
            FastDFSClient client = new FastDFSClient("classpath:config/fdfs_client.conf");
            Integer integer = client.delete_file(fileId);
            return new Result(true,String.valueOf(integer));
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }
}
