package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {

    /**
     * 获取营业额统计信息
     * @param beginDate
     * @param endDate
     * @return
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate beginDate, LocalDate endDate);

    /**
     * 统计订单数据
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);

    /**
     * 查询指定时间区间内的销量排名top10
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);

    /**
     * 导出近30天的运营数据报表
     * @param response
     */
    void exportBusinessData(HttpServletResponse response);
}
