package com.feiyongjing.wxshop.controller;


import com.feiyongjing.wxshop.api.DataStatus;
import com.feiyongjing.wxshop.api.data.OrderInfo;
import com.feiyongjing.wxshop.api.data.PageResponse;
import com.feiyongjing.wxshop.api.generate.Order;
import com.feiyongjing.wxshop.entity.OrderResponse;
import com.feiyongjing.wxshop.entity.Response;
import com.feiyongjing.wxshop.service.OrderService;
import com.feiyongjing.wxshop.service.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class OrderController {

    private OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @RequestMapping("testRpc")
    private String testRpc() {
//        orderService.placeOrder(1, 2);
        return "";
    }

    /**
     * 添加订单
     * @param orderInfo
     * @return {}
     */
    @PostMapping("/order")
    public Response<OrderResponse> createOrder(@RequestBody OrderInfo orderInfo) {
        orderService.deductStock(orderInfo);
        return Response.of(orderService.createOrder(orderInfo, UserContext.getCurrentUser().getId()));
    }

    /**
     * 删除订单
     * @param orderId
     * @return {}
     */
    @DeleteMapping("/order/{id}")
    public Response<OrderResponse> deleteOrder(@PathVariable("id") int orderId) {
        return Response.of(orderService.deleteOrder(orderId, UserContext.getCurrentUser().getId()));
    }

    /**
     * 修改订单
     * @param orderId
     * @param order
     * @return {}
     */
    @RequestMapping(value = "/order/{id}", method = {RequestMethod.POST, RequestMethod.PATCH})
    public Response<OrderResponse> updateOrder(@PathVariable("id") long orderId, @RequestBody Order order) {
        if (order.getExpressCompany() != null) {
            return Response.of(orderService.updateExpressInformation(orderId, order, UserContext.getCurrentUser().getId()));
        } else {
            return Response.of(orderService.updateOrderStatus(orderId, order, UserContext.getCurrentUser().getId()));
        }
    }

    /**
     * 分页获取订单
     * @param pageNum 页码数
     * @param pageSize 分页大小
     * @param status 订单状态 可以不填写
     * @return {}
     */
    @GetMapping("/order")
    public PageResponse<OrderResponse> getOrder(@RequestParam("pageNum") int pageNum,
                                                @RequestParam("pageSize") int pageSize,
                                                @RequestParam(value = "status", required = false) String status) {

        return orderService.getOrder(pageNum, pageSize, DataStatus.fromStatus(status), UserContext.getCurrentUser().getId());
    }

}
