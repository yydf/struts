package cn.coder.struts.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.ElementType;

/**
 * 程序起动后自动执行
 * 
 * @author YYDF
 *
 */
@Retention(RUNTIME)
@Target(value = { ElementType.TYPE, ElementType.METHOD })
public @interface AutoRun {

}
