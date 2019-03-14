package cn.coder.struts.jdbc.pool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.util.JdbcUtils;

public class ConnectionPool {

	private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
	private static final Object lockObj = new Object();
	private final LinkedBlockingQueue<Connection> idle = new LinkedBlockingQueue<>(1024);
	private final int initialSize;
	private final String username;
	private final String password;
	private final String url;

	public ConnectionPool(Properties properties) throws SQLException {
		initialSize = Integer.parseInt(properties.getProperty("initialSize", "2"));
		username = properties.getProperty("username");
		password = properties.getProperty("password");
		url = properties.getProperty("url");
		init(properties.getProperty("driverClassName"));
	}

	private void init(String driverClassName) throws SQLException {
		JdbcUtils.registerDriver(driverClassName);
		Connection conn;
		for (int i = 0; i < initialSize; i++) {
			conn = createConnection();
			idle.add(conn);
			logger.debug("Inited {}", conn.hashCode());
		}
	}

	public Connection getConnection() throws SQLException {
		try {
			return borrowConnection();
		} catch (SQLException | InterruptedException e) {
			logger.error("Borrow connection faild", e);
			throw new SQLException("Borrow connection faild");
		}
	}

	private Connection borrowConnection() throws SQLException, InterruptedException {
		// 从队列获取，队列为空返回null
		Connection conn = idle.poll();
		if (conn != null) {
			if (JdbcUtils.isValid(conn)) {
				logger.debug("Borrowed {}", conn.hashCode());
				return conn;
			}
			JdbcUtils.closeConnection(conn);
			return createConnection();
		}
		logger.debug("Waiting...");
		synchronized (lockObj) {
			lockObj.wait();
		}
		return borrowConnection();
	}

	private synchronized Connection createConnection() throws SQLException {
		return DriverManager.getConnection(url, username, password);
	}

	public void releaseConnection(Connection con) {
		if (idle.offer(con)) {
			JdbcUtils.clearWarnings(con);
			logger.debug("Released {}", con.hashCode());
			synchronized (lockObj) {
				lockObj.notify();
			}
		} else {
			JdbcUtils.closeConnection(con);
			logger.warn("Released {} faild and force close", con.hashCode());
		}
	}

	public void clear() {
		logger.debug("Pool start clear");
		for (Connection con : idle) {
			JdbcUtils.closeConnection(con);
		}
		idle.clear();
		JdbcUtils.deregisterDriver();
	}

}
