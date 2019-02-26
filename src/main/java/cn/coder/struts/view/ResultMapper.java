package cn.coder.struts.view;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ResultMapper<T> {
	T doStatement(PreparedStatement stmt) throws SQLException;

	boolean returnGeneratedKeys();

	String getSql();

	Object[] getArgs();

	void clean();
}
