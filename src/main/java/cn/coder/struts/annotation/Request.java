package cn.coder.struts.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.ElementType;

@Retention(RUNTIME)
@Target(value = { ElementType.TYPE, ElementType.METHOD })
public @interface Request {

	String value() default "";// 请求链接

	HttpMethod method() default HttpMethod.GET;// 请求方式

	public enum HttpMethod {
		ALL, GET, POST
	}

}
