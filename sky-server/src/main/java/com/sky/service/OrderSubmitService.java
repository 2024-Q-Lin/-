package com.sky.service;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.vo.OrderSubmitVO;

/**
 * 订单提交
 */
public interface OrderSubmitService {
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);
}
