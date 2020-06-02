package cn.coder.struts.event;

public interface StrutsEventListener {
	
	Class<?> support();

	void onEvent(Object event);

}
