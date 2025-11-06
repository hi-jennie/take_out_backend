package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/user/shoppingCart")
@Api(tags = "shopping cart related operations")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;


    @PostMapping("/add")
    @ApiOperation("add item to shopping cart")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("add item to shopping cart {}", shoppingCartDTO);
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("list shopping cart items")
    public Result<List<ShoppingCart>> list() {
        log.info("list shopping cart items");
        List<ShoppingCart> shoppingCartList = shoppingCartService.list();
        return Result.success(shoppingCartList);
    }

    @DeleteMapping("/clean")
    @ApiOperation("clear shopping cart")
    public Result clean() {
        log.info("clear shopping cart");
        shoppingCartService.clear();
        return Result.success();
    }

    @PostMapping("/sub")
    @ApiOperation("subtract amount by 1")
    public Result subAmount(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("subtract the amount by 1: {}", shoppingCartDTO);
        shoppingCartService.subBy1(shoppingCartDTO);
        return Result.success();
    }
}
