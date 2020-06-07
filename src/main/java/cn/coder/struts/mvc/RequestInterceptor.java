package cn.coder.struts.mvc;

public interface RequestInterceptor extends Interceptor {

	boolean matches(String path);

}
