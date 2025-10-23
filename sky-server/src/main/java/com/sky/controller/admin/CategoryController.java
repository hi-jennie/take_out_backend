package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 分类管理
 */
@RestController
@RequestMapping("/admin/category")
@Api(tags = "category management")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * add new category
     * @param categoryDTO
     * @return
     */
    @PostMapping
    @ApiOperation("add new category")
    public Result<String> save(@RequestBody CategoryDTO categoryDTO){
        log.info("add new category：{}", categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success();
    }

    /**
     * find category by page
     * @param categoryPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("find category by page")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("find category by page：{}", categoryPageQueryDTO);
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * delete category by id
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("delete category by id")
    public Result<String> deleteById(Long id){
        log.info("delete category by id：{}", id);
        categoryService.deleteById(id);
        return Result.success();
    }

    /**
     * update category
     * @param categoryDTO
     * @return
     */
    @PutMapping
    @ApiOperation("update category")
    public Result<String> update(@RequestBody CategoryDTO categoryDTO){
        categoryService.update(categoryDTO);
        return Result.success();
    }

    /**
     * enable or disable category
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("enable or disable category")
    public Result<String> startOrStop(@PathVariable("status") Integer status, Long id){
        categoryService.startOrStop(status,id);
        return Result.success();
    }

    /**
     * list categories by type
     * @param type
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("list categories by type")
    public Result<List<Category>> list(Integer type){
        List<Category> list = categoryService.list(type);
        return Result.success(list);
    }
}
