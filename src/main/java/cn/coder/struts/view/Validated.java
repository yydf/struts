package cn.coder.struts.view;

public class Validated {

	private Object data;

	public <T> void setData(T newInstance) {
		this.data = newInstance;
	}

	@SuppressWarnings("unchecked")
	public <T> T getData() {
		return (T) this.data;
	}

	public boolean check() {
		// TODO Auto-generated method stub
		return true;
	}

	public String error() {
		// TODO Auto-generated method stub
		return null;
	}

}
