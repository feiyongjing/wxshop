package com.feiyongjing.wxshop.controller;


import com.feiyongjing.wxshop.api.DataStatus;
import com.feiyongjing.wxshop.api.data.OrderInfo;
import com.feiyongjing.wxshop.api.exception.HttpException;
import com.feiyongjing.wxshop.api.generate.Order;
import com.feiyongjing.wxshop.entity.OrderResponse;
import com.feiyongjing.wxshop.api.data.PageResponse;
import com.feiyongjing.wxshop.entity.Response;
import com.feiyongjing.wxshop.service.OrderService;
import com.feiyongjing.wxshop.service.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
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

    @PostMapping("/order")
    public Response<OrderResponse> createOrder(@RequestBody OrderInfo orderInfo) {
        orderService.deductStock(orderInfo);
        return Response.of(orderService.createOrder(orderInfo, UserContext.getCurrentUser().getId()));
    }

    @DeleteMapping("/order/{id}")
    public Response<OrderResponse> deleteOrder(@PathVariable("id") int orderId) {
        return Response.of(orderService.deleteOrder(orderId, UserContext.getCurrentUser().getId()));
    }

    @PatchMapping("/order/{id}")
    public Response<OrderResponse> updateOrder(@PathVariable("id") long orderId, @RequestBody Order order) {
        if (order.getExpressCompany() != null) {
            return Response.of(orderService.updateExpressInformation(orderId, order, UserContext.getCurrentUser().getId()));
        } else {
            return Response.of(orderService.updateOrderStatus(orderId, order, UserContext.getCurrentUser().getId()));
        }
    }

    @GetMapping("/order")
    public PageResponse<OrderResponse> getOrder(@RequestParam("pageNum") int pageNum,
                                                @RequestParam("pageSize") int pageSize,
                                                @RequestParam(value = "status", required = false) String status) {
        if (DataStatus.fromStatus(status) == null) {
            throw HttpException.badRequest("非法status: " + status);
        }
        return orderService.getOrder(pageNum, pageSize, DataStatus.fromStatus(status), UserContext.getCurrentUser().getId());
    }

}
