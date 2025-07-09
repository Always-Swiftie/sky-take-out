package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrderHistoryDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderInfoVO;
import com.sky.vo.OrderQueryVO;
import com.sky.vo.OrderStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);


    /**
     * 根据订单号和用户id查询订单
     * @param orderNumber
     * @param userId
     */
    @Select("select * from orders where number = #{orderNumber} and user_id= #{userId}")
    Orders getByNumberAndUserId(String orderNumber, Long userId);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 通过订单id获取订单
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    Page<OrderInfoVO> PageQuery(OrderHistoryDTO orderHistoryDTO);

    /**
     * 商家端订单条件分页查询
     * @param ordersPageQueryDTO
     * @return
     * OrderVO是Order的子类
     */
    Page<OrderQueryVO> orderPageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 获取各个状态订单的数量统计
     * @return
     */
    OrderStatisticsVO getStatistics();

    /**
     * 根据状态和下单时间查询订单
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from sky_take_out.orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);

    /**
     * 动态统计营业额数据
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 动态统计订单数据
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    /**
     * 查询商品销量排名
     * @param begin
     * @param end
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);
}
