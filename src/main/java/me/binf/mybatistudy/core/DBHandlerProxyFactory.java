package me.binf.mybatistudy.core;

import me.binf.mybatistudy.annos.MyQuery;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.sql.DataSource;
import java.lang.reflect.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @description:
 * @author: wangbin
 * @create: 2022/1/25 17:57
 **/
public class DBHandlerProxyFactory {

    /**
     * 获取代理对象
     *
     * @param dataSource
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getTargetClass(DataSource dataSource, Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(), // 传入ClassLoader
                new Class[]{clazz}, // 传入要实现的接口
                getQueryInvHandler(dataSource)); // 传入处理调用方法的InvocationHandler
    }

    /**
     * 获取动态代理执行handler
     *
     * @param dataSource
     * @return
     */
    public static InvocationHandler getQueryInvHandler(DataSource dataSource) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                MyQuery myQuery = method.getAnnotation(MyQuery.class);
                if (myQuery != null) {
                    String sql = myQuery.value();
                    Connection connection = dataSource.getConnection();
                    Statement statement = connection.createStatement();
                    int i = 0;
                    while (sql.contains("?")) {
                        sql = sql.replaceFirst("\\?", args[i] + "");
                        i++;
                    }
                    System.out.println("执行sql语句：" + sql);
                    ResultSet resultSet = statement.executeQuery(sql);
                    Type returnType = method.getGenericReturnType();
                    Class<?> returnTypeClass = null;
                    if (returnType instanceof Class) {
                        returnTypeClass = (Class<?>) returnType;
                    } else {
                        ParameterizedTypeImpl r = (ParameterizedTypeImpl) method.getGenericReturnType();
                        returnTypeClass = r.getRawType();
                        if (Collection.class.isAssignableFrom(returnTypeClass)) {
                            Type[] types = r.getActualTypeArguments();
                            returnTypeClass = (Class<?>) types[0];
                        }
                    }
                    //如果是集合获取泛型类型
                    List<?> list = getQueryResult(resultSet, returnTypeClass);
                    resultSet.close();
                    connection.close();
                    return list;
                }
                return new ArrayList<>();
            }
        };
        return handler;
    }


    /**
     * 获取查询结果
     *
     * @param resultSet
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> getQueryResult(ResultSet resultSet, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        try {
            while (resultSet.next()) {
                T obj = clazz.newInstance();
                ResultSetMetaData rsmeta = resultSet.getMetaData();
                int count = rsmeta.getColumnCount();
                for (int i = 0; i < count; i++) {
                    String name = rsmeta.getColumnName(i + 1);
                    Field f = obj.getClass().getDeclaredField(name);
                    f.setAccessible(true);
                    f.set(obj, resultSet.getObject(name));
                }
                list.add(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}
