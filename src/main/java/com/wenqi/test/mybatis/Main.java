package com.wenqi.test.mybatis;

import com.alibaba.fastjson.JSON;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.BasicConfigurator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * @author liangwenqi
 * @date 2023/4/14
 */
public class Main {
  public static void main(String[] args) {
    // 加了这一句log4j才生效
    BasicConfigurator.configure();
    //PropertyConfigurator.configure("src/main/resources/mybatis/log4j.properties");

    String resource = "mybatis/mybatis-config.xml";
    InputStream inputStream = null;
    try {
      inputStream = Resources.getResourceAsStream(resource);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    SqlSessionFactory sqlSessionFactory = null;
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    SqlSession sqlSession = null;
    try {
      sqlSession = sqlSessionFactory.openSession();

      // 业务方法
      test01(sqlSession);

      sqlSession.commit();

    } catch (Exception e) {
      // TODO Auto-generated catch block
      sqlSession.rollback();
      e.printStackTrace();
    } finally {
      sqlSession.close();
    }
  }

  /**
   * 模拟in参数过大会诱发oom场景
   */
  private static void test02(SqlSession sqlSession) {
    RoleMapper roleMapper = sqlSession.getMapper(RoleMapper.class);
    List<Role> roles = roleMapper.selectRoleById(Arrays.asList(1L, 2L, 3L, 4L));
    System.out.println(JSON.toJSONString(roles));
  }

  private static void test01(SqlSession sqlSession) {
    RoleMapper roleMapper = sqlSession.getMapper(RoleMapper.class);
    Role role = roleMapper.getRole(1L);
    System.out.println(role.getId() + ":" + role.getRoleName() + ":" + role.getNote());
  }
}
