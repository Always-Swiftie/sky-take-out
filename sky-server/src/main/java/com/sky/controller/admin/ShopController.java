package com.sky.controller.admin;


import com.sky.result.Result;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
public class ShopController {

    public static final String key = "SHOP_STATUS";
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 设置店铺营业状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable String status){
        log.info("设置店铺的营业状态...:{}",status);
        redisTemplate.opsForValue().set(key,status);
        return Result.success();
    }

    /**
     * 获取店铺营业状态
     * @return
     */
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        String shopStatus =(String) redisTemplate.opsForValue().get("SHOP_STATUS");
        Integer status = Integer.parseInt(shopStatus);
        return Result.success(status);
    }

}
