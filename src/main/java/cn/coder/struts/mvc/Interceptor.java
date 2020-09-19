package cn.coder.struts.mvc;

public interface Interceptor {

	boolean before(Object[] args) throws Exception;

	void after(Object result) throws Exception;

	void exceptionCaught(Exception exception) throws Throwable;

}
