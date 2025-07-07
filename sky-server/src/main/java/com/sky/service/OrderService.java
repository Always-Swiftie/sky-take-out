package com.sky.service;

import com.sky.dto.OrderHistoryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderInfoVO;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
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
     * 再来一单(根据订单id)
     * @param id
     */
    void repetition(Long id);
}
