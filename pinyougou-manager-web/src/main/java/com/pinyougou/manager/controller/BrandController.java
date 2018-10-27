package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 描述:
 *
 * @author hudongfei
 * @create 2018-10-20 11:17
 */

@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;

    @RequestMapping("/findAll")
    public List<TbBrand> findAll(){
        return brandService.findAll();
    }

    @RequestMapping("/findPage")
    public PageResult findPage(int page,int size){
        return brandService.findPage(page,size);
    }

    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand brand){
        try {
            brandService.add(brand);
            return new Result(true,"新增品牌信息成功");
        } catch (Exception e){
            e.printStackTrace();
            return new Result(false,"增加失败"+e.getMessage());
        }

    }

    @RequestMapping("/findOne")
    public TbBrand findOne(Long id){
        return  brandService.findOne(id);
    }

    @RequestMapping("/update")
    public Result update(@RequestBody TbBrand brand){
        try {
            brandService.update(brand);
            return new Result(true,"修改成功");
        } catch (Exception e){
            e.printStackTrace();
            return new Result(false,"修改失败"+e.getMessage());
        }
    }

    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            brandService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e){
            e.printStackTrace();
            return new Result(false,"删除失败"+e.getMessage());
        }
    }

    @RequestMapping("/search")
    public PageResult search(@RequestBody TbBrand brand,int page,int size){
        return brandService.findPage(brand, page, size);
    }

    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return brandService.selectOptionList();
    }
}
