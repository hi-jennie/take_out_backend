package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}
