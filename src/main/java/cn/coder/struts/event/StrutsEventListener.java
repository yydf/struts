package cn.coder.struts.event;

public interface StrutsEventListener {
	
	boolean listen(Class<?> clazz);

	void onEvent(Object event);

}
