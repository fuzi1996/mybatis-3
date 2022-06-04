package org.apache.ibatis.parsing;

import org.apache.ibatis.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.*;

public class XNodeTest {
  private String resource = "resources/jdbc.properties";
  @Test
  public void test() throws IOException {
    Properties properties = Resources.getResourceAsProperties(resource);
    //定义数据源的xml片段
    String xml ="<?xml version='1.0' encoding='utf-8'?>"+
      "<bean id='dataSource' class='org.apache.commons.dbcp.BasicDataSource' destroy-method='close' >    " +
      "    <property name='driverClassName' value='${driver}' />" +
      "    <property name='url' value='${url}' />    " +
      "    <property name='username' value='${username}' />    " +
      "    <property name='password' value='${password}' />    " +
      "</bean>";
    //初始化XPathParser
    XPathParser xPathParser = new XPathParser(xml,false,properties);
    //解析表达式，获取XNode对象
    XNode xnode = xPathParser.evalNode("//bean");
    //下面调用对应的函数
    System.out.println(xnode);
    System.out.println(xnode.getValueBasedIdentifier());
    System.out.println(xnode.getStringAttribute("id"));
    System.out.println(xnode.getStringAttribute("class"));
  }

}
