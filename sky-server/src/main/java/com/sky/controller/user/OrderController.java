package com.sky.controller.user;


import com.sky.dto.OrderHistoryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderInfoVO;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderService")
@RequestMapping("/user/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;


    /**
     * 订单提交
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("订单提交:{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);

        //模拟交易成功，修改数据库订单状态
        orderService.paySuccess((ordersPaymentDTO.getOrderNumber()));
        log.info("模拟交易成功 订单号:{}",ordersPaymentDTO.getOrderNumber());
        return Result.success(orderPaymentVO);
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/orderDetail/{id}")
    public Result<OrderInfoVO> getOrderDetail(@PathVariable Long id){
        log.info("查询订单详情的订单id:{}", id);
        OrderInfoVO order = orderService.getById(id);
        return Result.success(order);
    }

    /**
     * 查询当前用户的历史订单(分页查询)
     */
    @GetMapping("/historyOrders")
    public Result<PageResult> getHistoryOrders(OrderHistoryDTO orderHistoryDTO){
        log.info("查询用户历史订单信息:{}", orderHistoryDTO);
        PageResult pageResult = orderService.getHistoryOrders(orderHistoryDTO);
        return Result.success(pageResult);
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel/{id}")
    public Result<String> cancelOrder(@PathVariable Long id){
        log.info("取消订单:{}", id);
        orderService.cancel(id);
        return Result.success();
    }

    /**
     * 再来一单
     */
    @PostMapping("/repetition/{id}")
    public Result<String> repetition(@PathVariable Long id){
        log.info("再来一单:{}", id);
        orderService.repetition(id);
        return Result.success();
    }

    /**
     * 用户催单
     * @param id
     * @return
     */
    @GetMapping("/reminder/{id}")
    public Result<String> reminder(@PathVariable Long id){
        log.info("用户催单:{}", id);
        orderService.reminder(id);
        return Result.success();
    }

}
