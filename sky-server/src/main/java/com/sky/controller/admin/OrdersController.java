package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderSubmitService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/order")
@Slf4j
@Api(tags = "订单管理相关接口")
public class OrdersController {

    @Autowired
    private OrderSubmitService orderSubmitService;

    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("订单搜索")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("订单搜索：{}", ordersPageQueryDTO);
        PageResult pageResult = orderSubmitService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 统计各状态订单的总数
     * @return
     */
    @GetMapping("/statistics")
    @ApiOperation("统计各状态订单的总数")
    public Result<OrderStatisticsVO> orderStatistics() {
        log.info("统计各状态订单数量");
        OrderStatisticsVO orderStatisticsVO = orderSubmitService.getStatistics();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> details(@PathVariable Long id) {
        log.info("查询订单详情，订单id：{}",id);
        OrderVO orderVO = orderSubmitService.showOrderDetail(id);
        return Result.success(orderVO);
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     * @return
     */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result<String> confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("接单，订单id：{}",ordersConfirmDTO.getId());
        orderSubmitService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     * @return
     */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result<String> rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        log.info("拒单：{}",ordersRejectionDTO);
        orderSubmitService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     * @return
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result<String> cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception {
        log.info("取消订单，取消信息：{}",ordersCancelDTO);
        orderSubmitService.adminCancel(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 派送订单
     * @param id
     * @return
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("订单派送")
    public Result<String> delivery(@PathVariable Long id) {
        log.info("订单派送，订单id：{}",id);
        orderSubmitService.delivery(id);
        return Result.success();
    }

    /**
     * 完成订单
     * @param id
     * @return
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result<String> complete(@PathVariable Long id) {
        log.info("完成订单，订单id：{}",id);
        orderSubmitService.complete(id);
        return Result.success();
    }
}
