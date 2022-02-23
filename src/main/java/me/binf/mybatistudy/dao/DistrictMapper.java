package me.binf.mybatistudy.dao;

import me.binf.mybatistudy.entity.District;

import java.util.List;

/**
 * @description:
 * @author: wangbin
 * @create: 2022/1/27 18:15
 **/
public interface DistrictMapper {


    District getById(Integer id);

    List<District> getListByParentId(Integer parentId);
}
