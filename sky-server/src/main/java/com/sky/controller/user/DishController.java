package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId 分类id
     * @return 返回查询的菜品列表数据
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> getDishByCid(Long categoryId) {
        log.info("根据分类id查询菜品：{}", categoryId);

        String key = "dish_" + categoryId;

        // 判断缓存中是否存在菜品数据
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if (list != null && list.size() > 0) {
            return Result.success(list);
        }
        // 缓存中不存在，查询数据库
        List<DishVO> dishVOList = dishService.listWithFlavor(categoryId);
        // 将查询结果存入缓存
        redisTemplate.opsForValue().set(key, dishVOList);

        return Result.success(dishVOList);
    }
}
