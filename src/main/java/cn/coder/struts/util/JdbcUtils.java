package cn.coder.struts.util;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcUtils {
	private static final Logger logger = LoggerFactory.getLogger(JdbcUtils.class);

	public static void bindArgs(PreparedStatement stmt, Object[] objs) throws SQLException {
		if (objs != null && objs.length > 0) {
			for (int i = 0; i < objs.length; i++) {
				if (objs[i] instanceof List) {
					// Object[] arr = new Object[((List<?>) objs[i]).size()];
					// ((List<?>) objs[i]).toArray(arr);
					// stmt.setArray(i + 1,
					// stmt.getConnection().createArrayOf("VARCHAR", arr));
				} else {
					stmt.setObject(i + 1, objs[i]);
				}
			}
		}
	}

	public static Object toBean(Class<?> target, HashMap<String, Field> mappings, ResultSet rs) throws SQLException {
		Object t = null;
		try {
			t = target.newInstance();
			Set<String> labels = mappings.keySet();
			Object obj;
			for (String label : labels) {
				obj = rs.getObject(label);
				if (obj != null) {
					FieldUtils.setValue(mappings.get(label), t, obj);
				}
			}
		} catch (InstantiationException | IllegalAccessException e) {
			throw new SQLException("The object '" + target.getName() + "' cannot be initialized.");
		}
		return t;
	}

	public static <T> HashMap<String, Field> buildFiledMappings(Class<T> target, ResultSetMetaData metaData)
			throws SQLException {
		Set<Field> fields = FieldUtils.getDeclaredFields(target);
		HashMap<String, Field> mappings = new HashMap<>(128);
		if (fields == null || fields.isEmpty()) {
			return mappings;
		}
		try {
			String fieldName, label, column;
			for (Field field : fields) {
				fieldName = field.getName();
				for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
					label = metaData.getColumnLabel(i);
					column = metaData.getColumnName(i);
					if (fieldName.equals(label) || fieldName.equals(column)
							|| fieldName.equals(FieldUtils.convert(label))
							|| fieldName.equals(FieldUtils.convert(column))) {
						mappings.put(label, field);
						break;
					}
				}
			}
		} catch (SQLException e) {
			throw new SQLException("获取对象和数据库的映射失败", e);
		}
		return mappings;
	}

	public static String printArgs(Object[] args) {
		if (args == null || args.length == 0)
			return "";
		StringBuffer sb = new StringBuffer(" args:[");
		for (Object obj : args) {
			if (obj != null) {
				sb.append(obj.toString());
				sb.append(",");
			}
		}
		sb = sb.delete(sb.length() - 1, sb.length());
		sb.append("]");
		return sb.toString();
	}

	public static void closeStatement(PreparedStatement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				logger.error("Close statement faild", e);
			}
		}
	}

	public static void registerDriver(String driverClassName) {
		try {
			Class.forName(driverClassName);
		} catch (ClassNotFoundException e) {
			logger.error("Register driver faild", e);
		}
	}

	public static void deregisterDriver() {
		try {
			Class<?> clazz = Class.forName("com.mysql.jdbc.AbandonedConnectionCleanupThread");
			if (clazz != null) {
				clazz.getMethod("shutdown").invoke(clazz);
			}
			Enumeration<Driver> drivers = DriverManager.getDrivers();
			while (drivers.hasMoreElements()) {
				Driver driver = (Driver) drivers.nextElement();
				DriverManager.deregisterDriver(driver);
			}
		} catch (Exception ex) {
			logger.error("Deregister driver faild", ex);
		}
	}

	public static boolean isValid(Connection conn) {
		try {
			return conn.isValid(0);
		} catch (SQLException e) {
			logger.error("Is valid error", e);
			return false;
		}
	}

	public static void closeConnection(Connection conn) {
		try {
			if (!conn.isClosed()) {
				conn.clearWarnings();
				conn.close();
			}
		} catch (SQLException e) {
			logger.error("Close connection faild", e);
		}
	}

	public static void clearWarnings(Connection con) {
		try {
			con.clearWarnings();
		} catch (SQLException e) {
			logger.error("Clear warnings faild", e);
		}
	}
}
