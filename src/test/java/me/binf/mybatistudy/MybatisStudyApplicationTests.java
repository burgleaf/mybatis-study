package me.binf.mybatistudy;

import me.binf.mybatistudy.core.DBHandlerProxyFactory;
import me.binf.mybatistudy.dao.DistrictDao;
import me.binf.mybatistudy.dao.DistrictMapper;
import me.binf.mybatistudy.entity.District;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
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


    @Test
    public void testMyBatis() throws IOException {
        //1、得到 SqlSessionFactory
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
        //2、得到 sqlSession ,代表和数据库一次回话
        SqlSession sqlSession = sqlSessionFactory.openSession();
        //3、得到真正操作数据库的Dao
        DistrictMapper mapper = sqlSession.getMapper(DistrictMapper.class);
        District district = mapper.getById(1);
        System.out.println(district);
        sqlSession.close();
    }

    @Test
    public void testMyBatisForList() throws IOException {
        //1、得到 SqlSessionFactory
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactory();
        //2、得到 sqlSession ,代表和数据库一次回话
        SqlSession sqlSession = sqlSessionFactory.openSession();
        //3、得到真正操作数据库的Dao
        DistrictMapper mapper = sqlSession.getMapper(DistrictMapper.class);
        List<District> district = mapper.getListByParentId(0);
        System.out.println(district);
        sqlSession.close();
    }

    /**
     * 1、根据全局配置文件获取 SqlSessionFactory
     * @return
     * @throws  = {DefaultSqlSessionFactory@6446}
     */
    public SqlSessionFactory getSqlSessionFactory() throws IOException {
        String resource = "mybatis/mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        //1、拿到全局配置
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        //准备环境信息
        Environment environment = new Environment("development", transactionFactory, dataSource);
        //2、使用我们的数据源
        sqlSessionFactory.getConfiguration().setEnvironment(environment);
        //使用mybatis-config.xml + Spring的数据源
        return sqlSessionFactory;
    }

}
