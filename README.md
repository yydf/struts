# struts
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/cn.4coder/struts/badge.svg)](https://maven-badges.herokuapp.com/maven-central/cn.4coder/struts/)
[![GitHub release](https://img.shields.io/github/release/yydf/struts.svg)](https://github.com/yydf/struts/releases)
[![license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://raw.githubusercontent.com/yydf/struts/master/LICENSE)
![Jar Size](https://img.shields.io/badge/jar--size-72k-blue.svg)

特性
-------------------------
* 零配置
* 高并发JDBC

环境
-------------
- JDK 7
- Tomcat 8

如何使用
-----------------------
* 添加dependency到POM文件:

```
<dependency>
    <groupId>cn.4coder</groupId>
    <artifactId>struts</artifactId>
    <version>0.0.2</version>
</dependency>
```
* resources目录下添加jdbc.properties文件(用不到，可以不添加):
```
driverClassName =com.mysql.jdbc.Driver
url=jdbc:mysql://localhost:3306/test
username=test
password=123456
initialSize=4
```
* [编码实现](https://gitee.com/yydf/easystruts-xjcy/wikis/pages)
* [DEMO示例](https://gitee.com/yydf/easystruts-xjcy/blob/master/demo.zip)

