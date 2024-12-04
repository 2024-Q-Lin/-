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
import com.sky.service.OrderSubmitService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderSubmitServiceImpl implements OrderSubmitService {

    private static final Logger log = LoggerFactory.getLogger(OrderSubmitServiceImpl.class);
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;

    /**
     * 提交订单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        // 处理异常情况（收货地址为空、超过配送范围、购物车为空）
        // 收货地址为空
        AddressBook addressBook = addressMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //购物车为空
        ShoppingCart cart = new ShoppingCart();
        cart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(cart);
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //新建一个order对象，完善数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setUserId(BaseContext.getCurrentId());
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setOrderTime(LocalDateTime.now());
        //完善完成后，向订单表插入数据
        orderMapper.insert(orders);

        //完善订单明细数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart shoppingCart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        //向明细表插入n条数据
        orderDetailMapper.insertBatch(orderDetailList);

        //删除购物车数据
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());

        //查询返回值的内容
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
    }

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

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

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

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

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
     * 分页查询历史订单
     *
     * @param page,pageSize,status
     * @return
     */
    @Override
    public PageResult page(int page, int pageSize, Integer status) {
        PageHelper.startPage(page, pageSize);

        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);
        // 条件查询订单
        Page<Orders> pageResult = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list = new ArrayList<>();

        //查询订单明细，把查询结果封装到OrderVO中
        if (pageResult != null && pageResult.size() > 0) {
            for (Orders orders : pageResult) {
                // 根据订单id查询订单明细
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
                //创建一个OrderVO对象
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                list.add(orderVO);
            }
        }
        return new PageResult(pageResult.getTotal(), list);
    }


    /**
     * 查看订单详情
     *
     * @param id
     * @return
     */
    @Override
    public OrderVO showOrderDetail(Long id) {
        // 根据订单id查询订单
        Orders orders = orderMapper.getById(id);

        // 新建一个OrderVO对象
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);

        // 根据订单id查询订单明细
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }

    /**
     * 取消订单
     *
     * @param id
     */
    @Override
    public void cancel(Long id) throws Exception {
        //根据订单id查询订单
        Orders ordersDB = orderMapper.getById(id);

        //校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //判断订单状态是否可以取消订单
        if (ordersDB.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // todo 作用存疑
        // 推测是因为简化对象，不需要更改的属性不用重复替换，直接跳过
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

        // 如果订单处于待接单状态下取消，需要进行退款
        if (ordersDB.getStatus().equals(OrderVO.TO_BE_CONFIRMED)) {
            //调用微信支付退款接口
            weChatPayUtil.refund(
                    ordersDB.getNumber(),//订单号
                    ordersDB.getNumber(),//退款订单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额

            //支付状态修改为退款
            orders.setPayStatus(Orders.REFUND);
        }

        //更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }


//    /**
//     * 再来一单
//     * @param id
//     * @return
//     */
//    @Override
//    public List<ShoppingCartDTO> repetition(Long id) {
//        Orders orders = orderMapper.getById(id);
//        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
//
//        //创建一个集合，用于存放购物车数据
//        List<ShoppingCartDTO> shoppingCartDTOList = new ArrayList<>();
//
//        if (orderDetailList != null && orderDetailList.size() > 0) {
//            for (OrderDetail orderDetail : orderDetailList) {
//                // 构造购物车对象
//                ShoppingCartDTO shoppingCartDTO = new ShoppingCartDTO();
//                BeanUtils.copyProperties(orderDetail, shoppingCartDTO);
//                shoppingCartDTOList.add(shoppingCartDTO);
//            }
//        }
//        return shoppingCartDTOList;
//    }

    /**
     * 再来一单
     *
     * @param id
     */
    @Override
    public void repetition(Long id) {
        // 查询当前用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单id查询当前订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        // 将订单详情对象转换为购物车对象
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            // 将原订单详情里面的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(x, shoppingCart, "id");// 排除id，避免id重复
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());

        // 将购物车对象批量添加到数据库
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 条件搜索订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        // 部分订单状态，需要额外返回订单菜品信息，将Orders转化为OrderVO
        List<OrderVO> orderVOList = getOrderVOList(page);

        return new PageResult(page.getTotal(), orderVOList);
    }

    /**
     * 统计各个状态订单总数
     *
     * @return
     */
    @Override
    public OrderStatisticsVO getStatistics() {
        // 查询所有订单
        List<Orders> ordersList = orderMapper.getAllOrders();

        //待接单数量2
        Integer toBeConfirmed = 0;
        //(已接单)待派送数量3
        Integer confirmed = 0;
        //派送中数量4
        Integer deliveryInProgress = 0;

        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                Integer status = orders.getStatus();
                if (status == 2) toBeConfirmed++;
                if (status == 3) confirmed++;
                if (status == 4) deliveryInProgress++;
            }
        }

        return new OrderStatisticsVO(toBeConfirmed, confirmed, deliveryInProgress);
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersConfirmDTO,orders);
        orders.setStatus(Orders.CONFIRMED);
        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        // 根据订单id查询订单
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());
        // 订单只有存在且状态为2（待接单）才可以拒单
        if (ordersDB == null || !ordersDB.getStatus().equals(OrderVO.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //支付状态,如果已经支付即退款
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus.equals(Orders.PAID)) {
            String refund = weChatPayUtil.refund(
                    ordersDB.getNumber(),//订单号
                    ordersDB.getNumber(),//退款订单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));
            log.info("申请退款：{}",refund);
        }
        //根据订单id更新订单状态、拒单原因、取消时间
        Orders orders = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 管理端取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void adminCancel(OrdersCancelDTO ordersCancelDTO) throws Exception {
        //根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());
        // 如果订单已经支付，则退款
        if (ordersDB != null && ordersDB.getPayStatus().equals(Orders.PAID)) {
            String refund = weChatPayUtil.refund(
                    ordersDB.getNumber(),//订单号
                    ordersDB.getNumber(),//退款订单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));
            log.info("申请退款：{}",refund);
        }
        //根据订单id更新订单状态、取消原因、取消时间
        Orders orders = Orders.builder()
                .id(ordersCancelDTO.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 派送订单
     * @param id
     */
    @Override
    public void delivery(Long id) {
        //查询订单信息
        Orders ordersDB = orderMapper.getById(id);
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void complete(Long id) {
        // 根据订单id查询订单
        Orders orderDB = orderMapper.getById(id);
        if (orderDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!orderDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(orders);
    }


    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        List<OrderVO> orderVOList = new ArrayList<>();

        // 取出Orders对象
        List<Orders> orderList = page.getResult();
        if (orderList != null && orderList.size() > 0) {
            for (Orders orders : orderList) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                // 获取orderDishes
                String orderDishes = getOrderDish(orders);
                // 将orderDishes封装进orderVO中,并添加到orderVOList
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    private String getOrderDish(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        // 将每一条订单菜品信息拼接为字符串，（格式：宫保鸡丁*3；）
        List<String> orderDDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将订单菜品信息拼接在一起
        return String.join("", orderDDishList);
    }
}
