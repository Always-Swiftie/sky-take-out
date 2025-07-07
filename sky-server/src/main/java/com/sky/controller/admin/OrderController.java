package com.sky.controller.admin;


import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderInfoVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 商家端订单管理
 */
@RestController("adminOrderService")
@RequestMapping("/admin/order")
@Slf4j
public class OrderController {
    @Autowired
    OrderService orderService;

    /**
     * 根据id获取订单详情
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    public Result<OrderInfoVO> getOrderDetail(@PathVariable Long id){
        log.info("查询订单详情:{}", id);
        OrderInfoVO orderInfoVO = orderService.getById(id);
        return Result.success(orderInfoVO);
    }

    /**
     * 订单条件分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    public Result<PageResult> orderPageQuery(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("订单条件分页查询:{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.pageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 商家接单
     * @param
     * @return
     */
    @PutMapping("/confirm")
    public Result<String> confirm(@RequestBody OrdersConfirmDTO  ordersConfirmDTO){
        Long id = ordersConfirmDTO.getId();
        log.info("商家接单:{}", id);
        orderService.confirm(id);
        return Result.success();
    }

    /**
     * 商家拒单
     * @param ordersRejectionDTO
     * @return
     */
    @PutMapping("/rejection")
    public Result<String> rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        log.info("商家拒单:{}", ordersRejectionDTO);
        orderService.reject(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 商家取消订单
     * @param ordersCancelDTO
     * @return
     */
    @PutMapping("/cancel")
    public Result<String> cancel(@RequestBody OrdersCancelDTO ordersCancelDTO){
        log.info("商家取消订单:{}", ordersCancelDTO);
        orderService.cancelOrders(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 商家派送订单
     * @param id
     * @return
     */
    @PutMapping("/delivery/{id}")
    public Result<String> delivery(@PathVariable Long id){
        log.info("派送订单:{}", id);
        orderService.delivery(id);
        return Result.success();
    }

    /**
     * 商家完成订单
     * @param id
     * @return
     */
    @PutMapping("complete/{id}")
    public Result<String> complete(@PathVariable Long id){
        log.info("订单完成:{}", id);
        orderService.complete(id);
        return Result.success();
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> getOrderStatistics(){
        log.info("获取各个状态的订单数量统计");
        OrderStatisticsVO orderStatisticsVO = orderService.getOrderStatistics();
        return Result.success(orderStatisticsVO);
    }



}
