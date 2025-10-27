package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "Dish management module Api")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @PostMapping
    @ApiOperation("add new dish")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("add new dish");
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("dish page query dish")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("dish page query:{}", dishPageQueryDTO);
        PageResult page = dishService.queryPage(dishPageQueryDTO);
        log.info(page.toString());
        return Result.success(page);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("update dish status")
    public Result updateStatus(@PathVariable Integer status, @RequestParam("id") Long id) {
        log.info("update dish status to {} for id:{}", status, id);
        dishService.updateDishStatus(status, id);
        return Result.success();
    }

    @DeleteMapping()
    @ApiOperation("delete dishes in batch by ids")
    public Result deleteBatch(@RequestParam("ids") List<Long> ids) {
        log.info("delete dishes in batch by ids:{}", ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * in frontend, get the dish first used to display the original data in form and then update it
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("get dish by id")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("get dish by id:{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * update dish with flavor
     *
     * @param dishDTO
     * @return
     */
    @PutMapping()
    @ApiOperation("update dish with flavor")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("update dish with flavor:{}", dishDTO);
        dishService.updateDishWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * Get dishes by categoryId
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("Get dished by categoryId")
    public Result<List<Dish>> list(Long categoryId) {
        List<Dish> list = dishService.getDishes(categoryId);
        return Result.success(list);
    }

}