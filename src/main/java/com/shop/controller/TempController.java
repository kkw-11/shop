package com.shop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TempController {

    @GetMapping("/orders")
    public String ordersComingSoon(Model model) {
        model.addAttribute("title", "구매내역 기능은 준비 중입니다 🚧");
        return "coming-soon";
    }
}
