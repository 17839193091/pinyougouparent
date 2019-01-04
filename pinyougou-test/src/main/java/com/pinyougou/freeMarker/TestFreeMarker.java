package com.pinyougou.freeMarker;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2019-01-03 12:41
 */
public class TestFreeMarker {
    public static void main(String[] args) throws IOException, TemplateException {
        //1 创建一个配置对象
        Configuration configuration = new Configuration(Configuration.getVersion());
        //2 设置模板所在目录
        configuration.setDirectoryForTemplateLoading(new File("E:\\pinyougouparent\\pinyougou-test\\src\\main\\resources"));
        //3 设置字符集
        configuration.setDefaultEncoding("utf-8");
        //4 获取模板对象
        Template template = configuration.getTemplate("test.ftl");
        //5 创建数据模型
        Map map = new HashMap();
        List goodsList = new ArrayList();

        Map<String,Object> goods1 = new HashMap<>();
        goods1.put("name","苹果");
        goods1.put("price","18.3");

        Map<String,Object> goods2 = new HashMap<>();
        goods2.put("name","梨");
        goods2.put("price","18.4");

        Map<String,Object> goods3 = new HashMap<>();
        goods3.put("name","橘子");
        goods3.put("price","18.5");

        goodsList.add(goods1);
        goodsList.add(goods2);
        goodsList.add(goods3);

        map.put("goodsList",goodsList);
        map.put("today",new Date());
        map.put("point",199510520);

        //6 创建输出流对象
        Writer out = new FileWriter("E:\\test.html");
        //7 通过模板对象输出
        template.process(map,out);
        //8 关闭流
        out.close();
    }
}
