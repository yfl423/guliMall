package com.atguigu.gulimall.order.web;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitResponseVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    /**
     * 生成订单确认页模型，并跳转去确认
     *
     * @param model
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @RequestMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();

        model.addAttribute("orderConfirmData", orderConfirmVo);
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo orderSubmitVo, Model model, RedirectAttributes redirectAttributes) {
        OrderSubmitResponseVo responseVo = orderService.submitOrder(orderSubmitVo);
        if (responseVo.getCode() == 0) {
            // 下单成功，转发支付页
            model.addAttribute("submitOrderResponse", responseVo);
            return "pay";
        } else {
            // 下单不成功，重定向到订单确认页
            String msg = "下单失败，";
            Integer code = responseVo.getCode();
            if (code == BizCodeEnume.ORDER_REPEAT_SUBMIT_EXCEPTION.getCode()) {
                msg = msg + BizCodeEnume.ORDER_REPEAT_SUBMIT_EXCEPTION.getMsg();
            }
            if (code == BizCodeEnume.PRICE_NOT_SAME_EXCEPTION.getCode()) {
                msg = msg + BizCodeEnume.PRICE_NOT_SAME_EXCEPTION.getMsg();
            }
            if (code == BizCodeEnume.ORDER_REPEAT_SUBMIT_EXCEPTION.getCode()) {
                msg = msg + BizCodeEnume.ORDER_REPEAT_SUBMIT_EXCEPTION.getMsg();
            }
            if (code == BizCodeEnume.NO_STOCK_EXCEPTION.getCode()) {
                msg = msg + BizCodeEnume.NO_STOCK_EXCEPTION.getMsg();
            }
            redirectAttributes.addFlashAttribute("msg", msg);
            return "redirect:http://order.gulimall.com/toTrade";
        }

    }
}
