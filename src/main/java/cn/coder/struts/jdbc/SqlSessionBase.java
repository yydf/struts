package cn.coder.struts.jdbc;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.util.FieldUtils;
import cn.coder.struts.util.JdbcUtils;
import cn.coder.struts.util.MD5;
import cn.coder.struts.view.ResultMapper;
import cn.coder.struts.wrapper.EntityWrapper;
import cn.coder.struts.wrapper.EntityWrapper.SQLType;

public class SqlSessionBase {

	static final Logger logger = LoggerFactory.getLogger(SqlSessionBase.class);
	private DataSource ds;
	private static SqlSession sessionImpl;
	private final HashMap<String, HashMap<String, Field>> resultMappings = new HashMap<>();
	private final HashMap<String, EntityWrapper> entityWrappers = new HashMap<>();

	public SqlSessionBase(DataSource ds2) {
		this.ds = ds2;
	}

	public SqlTranction beginTranction() throws Exception {
		logger.debug("Tranction begin");
		return new SqlTranction(ds.getConnection());
	}

	public void close(SqlTranction tran) {
		try {
			Connection con = tran.Connection();
			con.setAutoCommit(true);
			((cn.coder.struts.jdbc.pool.DataSource)ds).releaseConnection(con);
			logger.debug("Tranction closed");
		} catch (SQLException e) {
			logger.error("Tranction close faile", e);
		}
	}

	protected <T> T execute(ResultMapper<T> mapper) {
		try {
			return execute(mapper, ds.getConnection(), true);
		} catch (SQLException e) {
			logger.error("execute faild", e);
			return null;
		}
	}

	protected <T> T execute(SqlTranction tran, ResultMapper<T> mapper) throws SQLException {
		return execute(mapper, tran.Connection(), false);
	}

	protected <T> T execute(ResultMapper<T> mapper, Connection con, boolean close) throws SQLException {
		logger.debug("Run:[" + mapper.getSql() + "]" + JdbcUtils.printArgs(mapper.getArgs()));
		PreparedStatement stmt = null;
		try {
			if (mapper.returnGeneratedKeys())
				stmt = con.prepareStatement(mapper.getSql(), Statement.RETURN_GENERATED_KEYS);
			else
				stmt = con.prepareStatement(mapper.getSql());
			applySettings(stmt);
			T result = mapper.doStatement(stmt);
			handleWarnings(stmt);
			return result;
		} catch (SQLException ex) {
			logger.error("Error:[" + mapper.getSql() + "]", ex);
			throw new SQLException("Execute statement callback faild:" + mapper.getSql(), ex);
		} finally {
			mapper.clean();
			JdbcUtils.closeStatement(stmt);
			if (close) {
				((cn.coder.struts.jdbc.pool.DataSource)ds).releaseConnection(con);
			}
		}
	}

	private synchronized <T> HashMap<String, Field> buildFiledMappings(Class<T> target, String sql,
			ResultSetMetaData metaData) throws SQLException {
		String key = MD5.encodeByMD5(target.getName() + "_" + sql);
		if (resultMappings.containsKey(key))
			return resultMappings.get(key);
		HashMap<String, Field> mapping = JdbcUtils.buildFiledMappings(target, metaData);
		resultMappings.put(key, mapping);
		return mapping;
	}

	private synchronized EntityWrapper buildEntityWrapper(Class<?> clazz) {
		String clazzName = clazz.getName();
		if (entityWrappers.containsKey(clazzName))
			return entityWrappers.get(clazzName);
		EntityWrapper wrapper = new EntityWrapper(clazz);
		entityWrappers.put(clazzName, wrapper);
		return wrapper;
	}

	private void applySettings(PreparedStatement stmt) throws SQLException {
		stmt.setQueryTimeout(5);
	}

	private void handleWarnings(PreparedStatement stmt) throws SQLException {
		// Do Nothing
	}

	private abstract class baseResultMapper<T> implements ResultMapper<T> {

		protected String sql;
		protected Object[] objs;
		protected Class<?> target;
		protected boolean withKey;
		protected EntityWrapper wrapper;

		public abstract T doStatement(PreparedStatement stmt) throws SQLException;

		@Override
		public boolean returnGeneratedKeys() {
			return withKey;
		}

		@Override
		public String getSql() {
			return this.sql;
		}

		@Override
		public Object[] getArgs() {
			return this.objs;
		}

		@Override
		public void clean() {
			this.sql = null;
			this.objs = null;
			this.target = null;
			this.withKey = false;
			if (this.wrapper != null) {
				this.wrapper.clear();
				this.wrapper = null;
			}
		}

	}

	protected final class defaultResultMapper extends baseResultMapper<Integer> implements ResultMapper<Integer> {

		public defaultResultMapper(String sql, Object[] array) {
			this.sql = sql;
			this.objs = array;
		}

		@Override
		public Integer doStatement(PreparedStatement stmt) throws SQLException {
			JdbcUtils.bindArgs(stmt, objs);
			return stmt.executeUpdate();
		}
	}

	protected final class queryResultMapper<T> extends baseResultMapper<List<T>> implements ResultMapper<List<T>> {

		public queryResultMapper(Class<T> _target, String sql, Object[] array) {
			this.sql = sql;
			this.objs = array;
			this.target = _target;
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<T> doStatement(PreparedStatement stmt) throws SQLException {
			JdbcUtils.bindArgs(stmt, objs);
			List<T> dataList = new ArrayList<>();
			ResultSet rs = stmt.executeQuery();
			HashMap<String, Field> mappings = null;
			Object t;
			while (rs.next()) {
				if (FieldUtils.isPrimitive(target))
					t = FieldUtils.toValue(target, rs.getObject(1));
				else {
					if (mappings == null)
						mappings = buildFiledMappings(target, sql, rs.getMetaData());
					t = JdbcUtils.toBean(target, mappings, rs);
				}
				dataList.add((T) t);
			}
			rs.close();
			return dataList;
		}
	}

	protected final class entityResultMapper extends baseResultMapper<Boolean> implements ResultMapper<Boolean> {

		public entityResultMapper(Object data, SQLType type) {
			this.wrapper = buildEntityWrapper(data.getClass()).setObject(data, type);
			this.sql = wrapper.getSql();
			this.objs = wrapper.getData();
			this.withKey = wrapper.returnGeneratedKey();
		}

		@Override
		public Boolean doStatement(PreparedStatement stmt) throws SQLException {
			JdbcUtils.bindArgs(stmt, objs);
			int num = stmt.executeUpdate();
			if (withKey) {
				ResultSet result = stmt.getGeneratedKeys();
				while (result.next()) {
					wrapper.setGeneratedKey(result.getInt(1));
					break;
				}
			}
			return num > 0;
		}

	}

	protected final class singleResultMapper<T> extends baseResultMapper<T> implements ResultMapper<T> {

		public singleResultMapper(Class<T> _target, String sql, Object[] array) {
			this.objs = array;
			this.target = _target;
			this.sql = sql;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T doStatement(PreparedStatement stmt) throws SQLException {
			JdbcUtils.bindArgs(stmt, objs);
			ResultSet rs = stmt.executeQuery();
			Object t = null;
			while (rs.next()) {
				if (FieldUtils.isPrimitive(target))
					t = FieldUtils.toValue(target, rs.getObject(1));
				else {
					HashMap<String, Field> mappings = buildFiledMappings(target, sql, rs.getMetaData());
					t = JdbcUtils.toBean(target, mappings, rs);
				}
				break;
			}
			rs.close();
			return (T) t;
		}
	}

	public void clear() {
		((cn.coder.struts.jdbc.pool.DataSource)ds).close();
		this.ds = null;
		this.entityWrappers.clear();
		this.resultMappings.clear();
	}

	public static SqlSession getSession() {
		return sessionImpl;
	}

	public static void createSession(DataSource ds) {
		if (sessionImpl == null) {
			sessionImpl = new SqlSession(ds);
		}
		logger.debug("Sql session created");
	}

	public static void createSession(Properties properties) throws SQLException {
		cn.coder.struts.jdbc.pool.DataSource ds = new cn.coder.struts.jdbc.pool.DataSource();
		ds.createPool(properties);
		createSession(ds);
	}

	public static void destory() {
		sessionImpl.clear();
		sessionImpl = null;
	}

}
