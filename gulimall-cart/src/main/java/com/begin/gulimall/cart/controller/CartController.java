package com.begin.gulimall.cart.controller;

import com.begin.gulimall.cart.interceptor.CartInterceptor;
import com.begin.gulimall.cart.service.CartService;
import com.begin.gulimall.cart.vo.Cart;
import com.begin.gulimall.cart.vo.CartItem;
import com.begin.gulimall.cart.vo.UserInfoTo;
import com.begin.gulimall.common.constant.AuthServerConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {

    @Resource
    CartService cartService;

    /**
     * 获取当前用户所有选中的购物项
     * @return
     */
    @GetMapping("/currentUserCartItems")
    @ResponseBody
    public List<CartItem> getCurrentUserCartItems() {
        return  cartService.getUserCartItems();
    }



    @GetMapping("deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com:30010/cart.html";
    }

    /**
     * 更改购物车数量
     *
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num) {
        cartService.changeItemCount(skuId, num);

        return "redirect:http://cart.gulimall.com:30010/cart.html";
    }

    /**
     * 记录选中状态
     *
     * @param skuId
     * @param check 1、表示选中 0、表示未选中
     * @return
     */
    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("check") Integer check) {

        cartService.checkItem(skuId, check);

        return "redirect:http://cart.gulimall.com:30010/cart.html";
    }

    /**
     * 浏览器有一个cookie:user-key标识用户身份，一个月后过期
     * 如果第一次使用jd的购物车功能，都会给一个临时的用户身份
     * 浏览器以后保存，每次访问都会带上这个cookie;
     * <p>
     * 登录session有
     * 未登录按照cookie里面带user-key来做
     * 第一次：如果没有临时用户，后台创建一个临时用户
     *
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {

        //1、快速得到用户信息
//        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
//        System.out.println(userInfoTo);

        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);

        return "cartList";
    }

    /**
     * 添加商品到购物车
     * RedirectAttributes
     * ra.addFlashAttribute:将数据放在session里面，可以在页面取出，但是只能取一次
     * ra.addAttribute:将数据放在url后面
     *
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes ra) throws ExecutionException, InterruptedException {

        cartService.addToCart(skuId, num);
        ra.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimall.com:30010/addToCartSuccessPage.html";
    }

    /**
     * 跳转到成功页
     *
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("/addToCartSuccessPage.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model) {
        //重定向到成功页面，再次查询购物车即可
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("item", cartItem);
        return "success";
    }
}
