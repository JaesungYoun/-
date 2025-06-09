package com.ssg.ssg.domain.order.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderTestPageController {

    @GetMapping("/test")
    public String orderTestPage() {
        return "test";
    }
}
