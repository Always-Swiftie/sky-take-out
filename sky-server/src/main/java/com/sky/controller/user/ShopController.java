package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
@RestController("userShopController")
@RequestMapping("/user/shop")
@Slf4j
public class ShopController {
    public static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取店铺的营业状态
     * @return
     */
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        String shopStatus = (String) redisTemplate.opsForValue().get(KEY);
        Integer status = Integer.parseInt(shopStatus);
        log.info("获取到店铺的营业状态为：{}",status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }
}
