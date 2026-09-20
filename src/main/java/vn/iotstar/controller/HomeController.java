package vn.iotstar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    

    @GetMapping("/admin/products")
    public String adminProducts() {
        return "admin-products";
    }

    @GetMapping("/admin/categories")
    public String adminCategories() {
        return "admin-categories";
    }
    @GetMapping("/")
    public String index() {
        return "home";
    }
}