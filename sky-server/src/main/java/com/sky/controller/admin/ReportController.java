package com.sky.controller.admin;


import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.service.UserService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * 数据统计相关接口
 */
@RestController
@RequestMapping("/admin/report")
@Slf4j
public class ReportController {


    @Autowired
    private ReportService reportService;
    @Autowired
    private UserService userService;


    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate begin,
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate end){
            log.info("营业额统计:begin:{},end:{}", begin, end);
            return Result.success(reportService.getTurnoverStatistics(begin, end));
    }

    /**
     * 用户统计
     * @return
     */
    @GetMapping("/userStatistics")
    public Result<UserReportVO> userStatistics(
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate begin,
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate end){
        log.info("用户统计:begin:{},end:{}", begin, end);
        UserReportVO userReport = userService.getUserStatistics(begin,end);
        return Result.success(userReport);
    }

    /**
     * 订单数据统计
     * @return
     */
    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> ordersStatistics(
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate begin,
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate end){
        log.info("订单数据统计:begin:{},end:{}", begin, end);
        OrderReportVO orderReport = reportService.getOrderStatistics(begin,end);
        return Result.success(orderReport);
    }

    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> top10(
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate begin,
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate end){
        log.info("top10菜品销售统计:begin:{},end:{}", begin, end);
        return Result.success(reportService.getSalesTop10(begin,end));
    }

    /**
     * 导出运营数据报表
     * @param response
     * @throws Exception
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        reportService.exportBusinessData(response);
    }
}
