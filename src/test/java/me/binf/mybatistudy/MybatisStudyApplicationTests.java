package me.binf.mybatistudy;

import me.binf.mybatistudy.core.DBHandlerProxyFactory;
import me.binf.mybatistudy.dao.DistrictDao;
import me.binf.mybatistudy.entity.District;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.util.List;

@SpringBootTest
class MybatisStudyApplicationTests {

    @Autowired
    private DataSource dataSource;


    @Test
    void testProxy() {
        DistrictDao districtDao = DBHandlerProxyFactory.getTargetClass(dataSource, DistrictDao.class);
        List<District> districtList = districtDao.getDistrictById(1);
        System.out.println(districtList);
    }


}
