package cn.coder.struts.jdbc;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import cn.coder.struts.jdbc.esql.EasySql;
import cn.coder.struts.view.PageResult;
import cn.coder.struts.wrapper.EntityWrapper.SQLType;

import static cn.coder.struts.util.ObjectUtils.mergeArray;

public final class SqlSession extends SqlSessionBase {

	public SqlSession(DataSource ds) {
		super(ds);
	}

	public EasySql table(String table) {
		return EasySql.table(this, table);
	}

	public <T> List<T> selectList(final Class<T> target, final String sql, Object... array) {
		return execute(new queryResultMapper<>(target, sql, array));
	}

	public <T> List<T> selectList(final SqlTranction tran, final Class<T> target, final String sql, Object... array)
			throws SQLException {
		return execute(tran, new queryResultMapper<>(target, sql, array));
	}

	public <T> List<T> selectPage(final Class<T> target, final PageResult result, final String fetchSql,
			final String countSql, Object... array) {
		result.setTotal(selectOne(Long.class, countSql, array));
		String fetchSql2 = fetchSql + " LIMIT ?,?";
		return selectList(target, fetchSql2, mergeArray(array, result.getStartRow(), result.getPageSize()));
	}

	public <T> T selectOne(final Class<T> target, final String sql, Object... array) {
		return execute(new singleResultMapper<>(target, sql, array));
	}

	public <T> T selectOne(final SqlTranction tran, final Class<T> target, final String sql, Object... array)
			throws SQLException {
		return execute(tran, new singleResultMapper<>(target, sql, array));
	}

	public boolean insert(final Object data) {
		return execute(new entityResultMapper(data, SQLType.INSERT));
	}

	public boolean insert(final SqlTranction tran, final Object data) throws SQLException {
		return execute(tran, new entityResultMapper(data, SQLType.INSERT));
	}

	public boolean update(final Object data) {
		return execute(new entityResultMapper(data, SQLType.UPDATE));
	}

	public boolean update(final SqlTranction tran, final Object data) throws SQLException {
		return execute(tran, new entityResultMapper(data, SQLType.UPDATE));
	}

	public int execute(final String sql, final Object... array) {
		return execute(new defaultResultMapper(sql, array));
	}

	public int execute(SqlTranction tran, final String sql, final Object... array) throws SQLException {
		return execute(tran, new defaultResultMapper(sql, array));
	}
}
