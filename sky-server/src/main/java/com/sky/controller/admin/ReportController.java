package com.sky.controller.admin;

import com.sky.service.OrderService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/admin/report")
@Api("admin - report related api")
@Slf4j
public class ReportController {

    @Autowired
    private OrderService orderService;


}
