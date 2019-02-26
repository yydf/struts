# struts

Summary
-------
A simple, light Java WEB + ORM framework.

Generated project characteristics
-------------------------
* No-xml
* Easy to use
* Light MVC
* ORM with tomcat jdbc
* JSTL for jsp
* XSS filter

Prerequisites
-------------
- JDK 7
- Tomcat 8

Switching to struts
-----------------------

* Add dependency to java web in POM:

```
<dependency>
    <groupId>com.github.yydf</groupId>
    <artifactId>struts</artifactId>
    <version>1.0.2</version>
</dependency>
```

* Coding:

```
@Request("/banner")
public class BannerController extends ActionSupport
{
	@Resource
	private BannerService bannerService;
	
	@Request("/info")
	public void getBannerInfo(){
		getResponse().sendRedirect("http://libs.4coder.cn/static/jstl")
	}
	
	@Request("/test")
	public String test(){
		String str = getParameter("s");
		return "12e3" + str;
	}
	
	@Request("/test")
	public String test(){
		//获取上传的文件对象
		MultipartFile file = getMultipartFile("name");
		return "ok";
	}
}
```
