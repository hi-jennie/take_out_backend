package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkSpaceService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkSpaceService workSpaceService;

    /**
     * get the turnover statistics
     *
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // get the dateList first;
        List<LocalDate> dateList = generateDateList(begin, end);

        List<Double> turnoverList = new ArrayList<>();
        //  get turnoverStatistics
        // select sun(amount) from orders where order_time < __ and order_time > __ and status == completed
        for (LocalDate localDate : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            Map map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.countByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * get user statistics
     *
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // get the dateList first;
        List<LocalDate> dateList = generateDateList(begin, end);

        // each single Integer is the total user amount and new user amount of the specific day
        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();

        // newUserList select count(id) from user where create_time > __ and create_time < __
        // totalUserList select count(id) from user where create_time < __
        for (LocalDate localDate : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            Map map = new HashMap<>();
            map.put("end", endTime);
            Integer totalUserAmount = userMapper.countByMap(map);

            map.put("begin", beginTime);
            Integer newUserAmount = userMapper.countByMap(map);
            totalUserList.add(totalUserAmount);
            newUserList.add(newUserAmount);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }


    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        // generate a dateList
        List<LocalDate> dateList = generateDateList(begin, end);

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        Integer totalOrderCount = 0;
        Integer validOrderCount = 0;
        Double orderCompletionRate = 0.0;

        for (LocalDate localDate : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            Integer totalOrder = getOrderAmount(beginTime, endTime, null);
            Integer validOrder = getOrderAmount(beginTime, endTime, Orders.COMPLETED);
            orderCountList.add(totalOrder);
            validOrderCountList.add(validOrder);

            totalOrderCount += totalOrder;
            validOrderCount += validOrder;

        }

        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * Get the top 10 best-selling products
     *
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderMapper.getTop10(beginTime, endTime);
        List<String> nameList = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();

    }

    /**
     * using apache poi to generate business report excel file and export it
     *
     * @param response
     */
    public void exportBusinessData(HttpServletResponse response) throws FileNotFoundException {
        // prepare data, see what kind of data we need to export
        // time period: last 30 days/ turnover/ orderCompletedRate/ newUsers/ validOrders/ unit price
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        BusinessDataVO businessData = workSpaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));

        // start to write excel file using apache poi
        // input template
        FileInputStream in = new FileInputStream(new File("/Users/jennie/take_out_backend/sky-server/src/main/resources/businessReportTemplate.xlsx"));
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            // write the time period
            XSSFSheet sheet = workbook.getSheetAt(0);
            sheet.getRow(1).getCell(1).setCellValue(begin + " - " + end);
            // write the overview data
            sheet.getRow(3).getCell(2).setCellValue(businessData.getTurnover());
            sheet.getRow(3).getCell(4).setCellValue(businessData.getOrderCompletionRate());
            sheet.getRow(3).getCell(6).setCellValue(businessData.getNewUsers());
            sheet.getRow(4).getCell(2).setCellValue(businessData.getValidOrderCount());
            sheet.getRow(4).getCell(4).setCellValue(businessData.getUnitPrice());

            // get the detailed data of each day
            for (int i = 1; i <= 30; i++) {
                LocalDateTime beginTime = LocalDateTime.of(LocalDate.now().minusDays(i), LocalTime.MIN);
                LocalDateTime endTime = LocalDateTime.of(LocalDate.now().minusDays(i), LocalTime.MAX);
                businessData = workSpaceService.getBusinessData(beginTime, endTime);

                XSSFRow row = sheet.getRow(6 + i);

                // write data of each day
                row.getCell(1).setCellValue(LocalDate.now().minusDays(i).toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());

            }
            // using output stream to export excel file
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);

            in.close();
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private List<LocalDate> generateDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }

    private Integer getOrderAmount(LocalDateTime beginTime, LocalDateTime endTime, Integer status) {
        Map map = new HashMap<>();
        map.put("begin", beginTime);
        map.put("end", endTime);
        map.put("status", status);

        Integer orderAmount = orderMapper.getOrderAmountByMap(map);
        return orderAmount;
    }

}
