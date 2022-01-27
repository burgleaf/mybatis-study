# mybatis源码分析（一）：自己动手写一个简单的mybaits框架


## 框架解决了什么问题

目前主流的Java Web项目都采用SSM(spring springmvc mybatis)框架，其中mybatis框架源码是最简单的，想入手源码学习的同学很推荐从mybatis开始。本系列文章是我对mybatis源码学习的一些梳理总结，可以帮助你更高效得理解mybatis。
在学习一个框架源码之前你首先要问自己几个问题。
1.这个框架解决了什么问题？
2.为了解决这个问题，你是如何设计的？框架是如何设计的？
3.框架是怎么样给你提供扩展性的？

通常第一个问题都很简单，可以直接在mybatis官网找到答案。mybatis官网是这么解释的：

>MyBatis 是一款优秀的持久层框架，它支持自定义 SQL、存储过程以及高级映射。MyBatis 免除了几乎所有的 JDBC 代码以及设置参数和获取结果集的工作。MyBatis 可以通过简单的 XML 或注解来配置和映射原始类型、接口和 Java POJO（Plain Old Java Objects，普通老式 Java 对象）为数据库中的记录。

我的理解是mybatis是一个持久层框架，它解决了使用JDBC冗余复杂代码操作数据库的问题，并且可以通过XML或者注解的方式来实现ORM（对象关系映射）技术，使我们在项目中操作数据库更为简单和耦合性更好。

##自己实现一个简单的mybatis框架

在学习源码之前先自己简单实现一遍类似的功能是很有必要的，可以帮助你更好的理解框架是解决问题的。假如要你实现一个简单的mybatis框架你会怎么做？
先不用去看mybatis的细节，我们知道使用mybatis的时候在注解或者xml中定义一个sql然后绑定到一个接口的方法上就能执行这条sql，并给你封装好结果返回。实现这个功能并不难，我们可以先尝试实现一遍。
我们也定义一个接口，不需要写实现类就可以执行接口定义的方法，且这个方法帮我们去执行注解里的sql语句：

```java
public interface DistrictDao {

    @MyQuery(" select * from district where id = ? ")
    List<District> getDistrictById(Integer id);

}
```

要实现这个功能用JDK的动态代理就可以了，我们可以建一个工厂类，用这个工厂类可以生成任意接口的代理对象，代理对象帮我们执行方法上定义的SQL并封装成结果。
简单的实现了一下可以执行任意查询语句的代理工厂类：

```java
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
     * @param dataSource
     * @return
     */
    public static InvocationHandler getQueryInvHandler(DataSource dataSource) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                MyQuery myQuery = method.getAnnotation(MyQuery.class);
                if (myQuery != null) {
                    //1.拿到目标方法上面的sql
                    String sql = myQuery.value();
                    //2.拿到数据库连接并执行sql
                    Connection connection = dataSource.getConnection();
                    Statement statement = connection.createStatement();
                    int i = 0;
                    while (sql.contains("?")) {
                        sql = sql.replaceFirst("\\?", args[i] + "");
                        i++;
                    }
                    System.out.println("执行sql语句：" + sql);
                    ResultSet resultSet = statement.executeQuery(sql);
                    //3.获取目标方法的返回类型
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
                    //4.封装返回结果
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
```

这里就不阐述动态代理了，动态代理handler执行思路是：
1.拿到目标方法上面的sql
2.拿到数据库连接并使用JDBC的API来执行sql
3.获取目标方法的返回类型
4.封装返回结果

通过这四步就可以不用再写冗余的JDBC代码了，只用写接口就可以查询数据并返回封装好的结果了。执行代码是这样的：
```java
@SpringBootTest
class MybatisStudyApplicationTests {

    @Autowired
    private DataSource dataSource;


    @Test
    void testProxy(){
        DistrictDao districtDao = DBHandlerProxyFactory.getTargetClass(dataSource,DistrictDao.class);
        List<District> districtList =  districtDao.getDistrictById(1);
        System.out.println(districtList);
    }
}
```
OK，到这里自己尝试设计一个简单“MyBatis”功能就完成了，接下来我们需要分析一下我们自己写的这个简单的框架有那些问题和缺陷。我们带着问题再去看Mybatis源码就会明白作者的设计意图，再看看Mybatis作者是如何解决这些问题的，这样学习源码会更有收获一些。

## 自己实现mybatis框架的缺陷

1.操作数据库只支持查询的操作而且代码复用性不高，如果要支持增删改查的操作该如何设计

2.封装结果集只支持集合类型，而且返回的实例字段只能和表字段名称一致才能映射，如何把结果集封装设计的更灵活一些

3.参数转换如何实现，例如传入的是Java的日期类型，应该怎么样转换成数据库支持的格式

4.预编译SQL以及数据库的事务如何实现

5.如何加入缓存策略