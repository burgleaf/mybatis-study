package me.binf.mybatistudy.dao;

import me.binf.mybatistudy.annos.MyQuery;
import me.binf.mybatistudy.entity.District;
import java.util.List;

/**
 * @description:
 * @author: wangbin
 * @create: 2022/1/24 16:57
 **/
public interface DistrictDao {

    @MyQuery(" select * from district where id = ? ")
    List<District> getDistrictById(Integer id);


}
