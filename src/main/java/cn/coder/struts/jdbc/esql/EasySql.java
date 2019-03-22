package cn.coder.struts.jdbc.esql;

import java.util.List;

import cn.coder.struts.jdbc.SqlSession;

public class EasySql {

	private final SqlSession session;

	private EasySql(SqlSession session, String table) {
		this.session = session;
	}

	public static EasySql table(final SqlSession session, final String table) {
		return new EasySql(session, table);
	}

	public EasySql where(Object... args) {
		return this;
	}

	public EasySql asc(String column) {
		return this;
	}

	public EasySql skip(int num) {
		return this;
	}

	public EasySql limit(int num) {
		return this;
	}

	public <T> T get(Class<T> clazz) {
		return session.selectOne(clazz, "");
	}

	public <T> List<T> list(Class<T> clazz) {
		return session.selectList(clazz, "");
	}

}
