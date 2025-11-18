package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@Api(tags = "work space related controller")
@RequestMapping("/admin/workspace")
@Slf4j
public class WorkSpaceController {

    @Autowired
    private WorkSpaceService workspaceService;
    @Autowired
    private ReportService reportService;

    /**
     * business data of current day
     *
     * @return
     */
    @GetMapping("/businessData")
    @ApiOperation("business data of current day")
    public Result<BusinessDataVO> businessData() {
        //get the beginning time of today
        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);
        //get the ending time of today
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        BusinessDataVO businessDataVO = workspaceService.getBusinessData(begin, end);
        return Result.success(businessDataVO);
    }

    /**
     * query order management data
     * return order count in different status
     *
     * @return
     */
    @GetMapping("/overviewOrders")
    @ApiOperation("query order management data")
    public Result<OrderOverViewVO> orderOverView() {
        //get the beginning time of today
        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);
        //get the ending time of today
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);
        return Result.success(workspaceService.getOrderOverView(begin, end));
    }

    /**
     * get dish overview
     *
     * @return
     */
    @GetMapping("/overviewDishes")
    @ApiOperation("get dish overview")
    public Result<DishOverViewVO> dishOverView() {
        return Result.success(workspaceService.getDishOverView());
    }

    /**
     * 查询套餐总览
     *
     * @return
     */
    @GetMapping("/overviewSetmeals")
    @ApiOperation("查询套餐总览")
    public Result<SetmealOverViewVO> setmealOverView() {
        return Result.success(workspaceService.getSetmealOverView());
    }


}
