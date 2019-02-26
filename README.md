# struts

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
    <version>0.0.1</version>
</dependency>
```

* 编码:

```
@Request("/banner")
public class BannerController extends ActionSupport {
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

```
public class BannerDao extends DaoSupport {

	public List<BannerVo> getAdList() {
		return jdbc().selectList(BannerVo.class, "select * from bz_banner");
	}
}
```
