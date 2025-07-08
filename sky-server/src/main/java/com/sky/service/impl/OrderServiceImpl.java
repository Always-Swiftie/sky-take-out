package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.*;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sky.entity.Orders.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private WebSocketServer  webSocketServer;


    /**
     * 订单提交
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //业务异常处理--地址簿、购物车异常
        //购物车异常判断
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if( shoppingCartList == null || shoppingCartList.isEmpty() ) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //地址簿异常判断
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if( addressBook == null ) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //**业务处理**
        //向订单表添加1条记录
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);

        orderMapper.insert(orders);
        //向订单明细表插入多条数据--遍历购物车
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for(ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        //清理当前用户购物车数据
        shoppingCartMapper.deleteByUserId(userId);
        //封装返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
        return orderSubmitVO;
    }

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
        //生成空的JSON,跳过微信支付流程
        JSONObject jsonObject = new JSONObject();

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单号查询当前用户的订单
        Orders ordersDB = orderMapper.getByNumberAndUserId(outTradeNo, userId);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
        //向商家端推送来单消息
        Map map = new HashMap();
        map.put("type",1);//消息类型,1-来单提醒
        map.put("orderId",ordersDB.getId());//订单id
        map.put("content","订单号:"+outTradeNo);
        //调用webSocketServer
        webSocketServer.sendToAllClient(JSONObject.toJSONString(map));
    }

    /**
     * 根据订单id查询订单详情
     * @param id
     * @return
     */
    @Override
    public OrderInfoVO getById(Long id) {
        //先查询到Order对象,给VO对象属性赋值,再查询OrderDetail对象,得到列表
        Orders order = orderMapper.getById(id);
        OrderInfoVO orderInfoVO = new OrderInfoVO();
        BeanUtils.copyProperties(order, orderInfoVO);
        //再通过orderDetailMapper查询到所有的详情信息集合
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        orderInfoVO.setOrderDetailList(orderDetailList);
        //还需要通过addressBookId获取到地址信息，再赋值给VO对象
        AddressBook addressBook = addressBookMapper.getById(order.getAddressBookId());
        String address = addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail();
        orderInfoVO.setAddress(address);
        //赋值完成
        return orderInfoVO;
    }

    /**
     * 查询当前用户的历史订单信息
     * @param orderHistoryDTO
     * @return
     */
    @Override
    public PageResult getHistoryOrders(OrderHistoryDTO orderHistoryDTO) {
        PageHelper.startPage(orderHistoryDTO.getPage(), orderHistoryDTO.getPageSize());
        //给DTO的userId属性赋值
        Long userId = BaseContext.getCurrentId();
        orderHistoryDTO.setUserId(userId);
        //调用mapper执行分页查询,查询到所有的orders,ordersDetail集合
        Page<OrderInfoVO> page = orderMapper.PageQuery(orderHistoryDTO);//还有orderDetail信息没获取
        for(OrderInfoVO orderInfoVO : page.getResult()) {
            //对于每个orderInfoVO对象，获取它的orderDetail信息
            List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderInfoVO.getId());
            orderInfoVO.setOrderDetailList(orderDetailList);
            //还需要通过addressBookId获取到地址信息，再赋值给VO对象
            AddressBook addressBook = addressBookMapper.getById(orderInfoVO.getAddressBookId());
            String address = addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail();
            orderInfoVO.setAddress(address);
        }
        long total = page.getTotal();
        return new PageResult(total,page);
    }

    /**
     * 取消订单
     * @param id
     */
    @Override
    public void cancel(Long id) {
        //简单的修改当前订单状态为6-已取消即可
        Orders order = orderMapper.getById(id);
        order.setStatus(CANCELLED);
        orderMapper.update(order);
    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    public void repetition(Long id) {
        //再来一单的逻辑是,通过订单id查询到订单的详情信息,重新加入到购物车中(也就是重新填充购物车)
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        for(OrderDetail orderDetail : orderDetailList) {
            ShoppingCartDTO shoppingCartDTO = new ShoppingCartDTO();
            shoppingCartDTO.setSetmealId(orderDetail.getSetmealId());
            shoppingCartDTO.setDishId(orderDetail.getDishId());
            shoppingCartDTO.setDishFlavor(orderDetail.getDishFlavor());
            shoppingCartService.addShoppingCart(shoppingCartDTO);
        }
    }

    /**
     * 商家端订单条件分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        //获取到Order类有的属性,只需要再为每个OrderQueryVO的Dishes属性赋值即可
        Page<OrderQueryVO> page = orderMapper.orderPageQuery(ordersPageQueryDTO);
        page.forEach(orderQueryVO -> {
            //要根据每张订单的订单id,获取到所有的orderDetail对象，把他们的name属性拿出来拼接
            List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderQueryVO.getId());
            StringBuilder dishes = new StringBuilder();
            for(OrderDetail orderDetail : orderDetailList) {
                dishes.append(orderDetail.getName()).append("*").append(orderDetail.getNumber()).append(",");
            }
            dishes.deleteCharAt(dishes.length() - 1);
            //菜品信息组装完成
            orderQueryVO.setOrderDishes(dishes.toString());
            //除了菜品的信息,现在address属性也没有赋值(只有addressBook属性),根据addressBookID去查询地址信息
            AddressBook addressBook = addressBookMapper.getById(orderQueryVO.getAddressBookId());
            orderQueryVO.setAddress(addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDetail());
        });
        long total = page.getTotal();
        return new PageResult(total,page);
    }

    /**
     * 商家接单
     * @param id
     */
    @Override
    public void confirm(Long id) {
        Orders order = orderMapper.getById(id);
        order.setStatus(CONFIRMED);
        orderMapper.update(order);
    }

    /**
     * 商家拒单
     * @param ordersRejectionDTO
     */
    @Override
    public void reject(OrdersRejectionDTO ordersRejectionDTO) {
        Orders order = orderMapper.getById(ordersRejectionDTO.getId());
        order.setStatus(CANCELLED);
        order.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orderMapper.update(order);
    }

    /**
     * 商家取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void cancelOrders(OrdersCancelDTO ordersCancelDTO) {
        Orders order = orderMapper.getById(ordersCancelDTO.getId());
        order.setStatus(CANCELLED);
        order.setCancelReason(ordersCancelDTO.getCancelReason());
        orderMapper.update(order);
    }

    /**
     * 商家派送订单
     * @param id
     */
    @Override
    public void delivery(Long id) {
        Orders order = orderMapper.getById(id);
        order.setStatus(DELIVERY_IN_PROGRESS);
        orderMapper.update(order);
    }

    /**
     * 商家完成订单
     * @param id
     */
    @Override
    public void complete(Long id) {
        Orders order = orderMapper.getById(id);
        order.setStatus(COMPLETED);
        orderMapper.update(order);
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @Override
    public OrderStatisticsVO getOrderStatistics() {
        OrderStatisticsVO orderStatisticsVO = orderMapper.getStatistics();
        return orderStatisticsVO;
    }

    /**
     * 用户催单
     * @param id
     */
    @Override
    public void reminder(Long id) {
        //根据id查到订单
        Orders order = orderMapper.getById(id);
        if(order == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Map map = new HashMap();
        map.put("type",2);
        map.put("orderId",order.getId());
        map.put("content","订单号:" + order.getNumber());
        webSocketServer.sendToAllClient(JSONObject.toJSONString(map));
    }
}
