package com.sky.mapper;


import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /**
     * 批量插入
     * @param orderDetails
     */
    void insertBatch(List<OrderDetail> orderDetails);

    /**
     * 根据订单id(逻辑外键)批量获取orderDetail列表
     * @param orderId
     * @return
     */
    List<OrderDetail> getByOrderId(Long orderId);
}
