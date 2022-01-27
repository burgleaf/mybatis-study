package me.binf.mybatistudy.entity;

import lombok.Data;

/**
 * @description:
 * @author: wangbin
 * @create: 2022/1/24 17:01
 **/
@Data
public class District {

    private Integer id;

    private String name;

    private Integer level;

    private Integer parent_id;

    private Integer is_special_city;



}
