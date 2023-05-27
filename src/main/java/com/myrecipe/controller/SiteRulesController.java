package com.myrecipe.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SiteRulesController {
    @GetMapping("/terms-of-service")
    private String showSiteRulesAndPolicy() {
        return "terms-of-service";
    }
}
