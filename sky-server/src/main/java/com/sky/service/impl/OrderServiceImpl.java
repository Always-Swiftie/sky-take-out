package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrderHistoryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.service.ShoppingCartService;
import com.sky.service.UserService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderInfoVO;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.sky.entity.Orders.CANCELLED;

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
}
