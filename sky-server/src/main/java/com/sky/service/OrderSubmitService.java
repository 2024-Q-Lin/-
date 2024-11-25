package com.sky.service;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

/**
 * 订单提交
 */
public interface OrderSubmitService {
    /**
     * 订单提交
     * @param ordersSubmitDTO
     * @return
     */
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
     * 分页查询历史订单
     * @param page,pageSize,status
     * @return
     */
    PageResult page(int page, int pageSize, Integer status);

    /**
     * 查看订单详情
     * @param id
     * @return
     */
    OrderVO showOrderDetail(Long id);

    /**
     * 取消订单
     * @param id
     */
    void cancel(Long id) throws Exception;


//    /**
//     * 再来一单
//     * @param id
//     * @return
//     */
//    List<ShoppingCartDTO> repetition(Long id);


    /**
     * 再来一单
     * @param id
     */
    void repetition(Long id);
}
