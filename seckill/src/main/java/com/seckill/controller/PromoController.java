package com.seckill.controller;

import com.seckill.response.CommonReturnType;
import com.seckill.service.PromoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
@RestController
@RequestMapping("/promo")
public class PromoController {

    @Autowired
    private PromoService promoService;

    @GetMapping("/publish")
    public CommonReturnType publish(@RequestParam("promoId") Integer promoId) {
        promoService.publish(promoId);
        return CommonReturnType.create("Promo published");
    }

}
