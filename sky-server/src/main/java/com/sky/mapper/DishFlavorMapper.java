package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 批量插入菜品口味数据
     *
     * @param flavors
     */
    void saveBatch(List<DishFlavor> flavors);

    /**
     * 根据批量菜品id删除对应的口味
     * @param ids
     */
    void deleteByDishIds(List<Long> ids);

    /**
     * 根据菜品id查询对应的口味数据
     * @param id
     */
    @Select("select * from dish_flavor where dish_id = #{id}")
    List<DishFlavor> getByDishId(Long id);

    /**
     * 根据菜品id删除对应的口味数据
     * @param id
     */
    @Delete("delete from dish_flavor where dish_id = #{id}")
    void deleteByDishId(Long id);
}
