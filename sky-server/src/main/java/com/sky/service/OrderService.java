package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.*;
import org.springframework.core.annotation.Order;

public interface OrderService {


    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 根据订单id查询订单详情信息
     */
    OrderInfoVO getById (Long id);

    /**
     * 查询当前用户的历史订单信息
     * @param orderHistoryDTO
     * @return
     */
    PageResult getHistoryOrders(OrderHistoryDTO orderHistoryDTO);

    /**
     * 取消订单
     * @param id
     */
    void cancel(Long id);

    /**
     * 商家取消订单
     */
    void cancelOrders(OrdersCancelDTO ordersCancelDTO);

    /**
     * 再来一单(根据订单id)
     * @param id
     */
    void repetition(Long id);

    /**
     * 商家端订单条件分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 商家接单
     * @param id
     */
    void confirm(Long id);

    /**
     * 商家拒单
     * @param ordersRejectionDTO
     */
    void reject(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 商家派送订单
     * @param id
     */
    void delivery(Long id);

    /**
     * 商家完成订单
     * @param id
     */
    void complete(Long id);

    /**
     * 各个状态的订单数量统计
     * @return
     */
    OrderStatisticsVO getOrderStatistics();
}
