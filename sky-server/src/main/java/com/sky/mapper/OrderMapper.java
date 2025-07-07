package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.dto.OrderHistoryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
}
