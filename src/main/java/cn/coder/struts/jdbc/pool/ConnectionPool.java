package cn.coder.struts.jdbc.pool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.coder.struts.util.JdbcUtils;
import cn.coder.struts.util.StringUtils;

public class ConnectionPool {

	static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
	static final Object lockObj = new Object();
	private BlockingQueue<Connection> idle;
	private int maxActive;
	private String username;
	private String password;
	private String url;

	private final AtomicLong waitCount = new AtomicLong(0);
	private final AtomicLong usedCount = new AtomicLong(0);
	private final AtomicLong createdCount = new AtomicLong(0);

	public ConnectionPool(Properties properties) {
		maxActive = getOrDefault(properties, "maxActive", 1024);
		username = properties.getProperty("username");
		password = properties.getProperty("password");
		url = properties.getProperty("url");
		idle = new LinkedBlockingQueue<>(maxActive);
		init(properties.getProperty("driverClassName"), getOrDefault(properties, "initialSize", 2));
	}

	private void init(String driverClassName, int size) {
		try {
			Class.forName(driverClassName);
		} catch (ClassNotFoundException e) {
			logger.error("Register driver faild", e);
			return;
		}
		for (int i = 0; i < size; i++) {
			try {
				idle.put(this.borrowConnection(false));
			} catch (InterruptedException e) {
				logger.error("Init connection faild", e);
			}
		}
	}

	/**
	 * 获取数据库连接 <br/>
	 * 1、如果不等待，直接返回新连接 <br/>
	 * 2、如果等待，则从队列中取出第一个，如果不存在，判断连接数是否小于允许的最大连接数 <br/>
	 * &nbsp;(1)如果小于允许数，则创建一个新连接返回 <br/>
	 * &nbsp;(2)如果大于允许数，则无限等待
	 * 
	 * @param wait
	 *            是否等待
	 * @return 数据库连接
	 */
	private Connection borrowConnection(boolean wait) {
		logger.debug("bow0 create:" + createdCount.get() + " use:" + usedCount.get() + " wait:" + waitCount.get());
		if (!wait) {
			return createConnection();
		}
		// 从队列获取，队列为空返回null
		Connection conn = idle.poll();
		if (conn == null) {
			if (createdCount.get() < maxActive) {
				logger.debug("Borrow a new connection");
				usedCount.incrementAndGet();
				return createConnection();
			}
			waitCount.incrementAndGet();
			synchronized (lockObj) {
				try {
					lockObj.wait();
				} catch (InterruptedException e) {
					logger.error("wait faild");
				}
			}
			waitCount.decrementAndGet();
			return borrowConnection(true);
		}
		if (JdbcUtils.isValid(conn)) {
			usedCount.incrementAndGet();
			logger.debug("bow1 create:" + createdCount.get() + " use:" + usedCount.get() + " wait:" + waitCount.get());
			return conn;
		}
		JdbcUtils.closeConnection(conn);
		return borrowConnection(true);
	}

	private Connection createConnection() {
		try {
			createdCount.incrementAndGet();
			return DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			createdCount.decrementAndGet();
			logger.error("Create connection faile", e);
			return null;
		}
	}

	public Connection getConnection() {
		return borrowConnection(true);
	}

	public void returnConnection(Connection con) {
		logger.debug("rtn0 create:" + createdCount.get() + " use:" + usedCount.get() + " wait:" + waitCount.get());
		if (idle.offer(con)) {
			usedCount.decrementAndGet();
			synchronized (lockObj) {
				lockObj.notify();
			}
		}
		logger.debug("rtn1 create:" + createdCount.get() + " use:" + usedCount.get() + " wait:" + waitCount.get());
	}

	public void clear() {
		for (Connection con : idle) {
			JdbcUtils.closeConnection(con);
		}
		idle.clear();
		JdbcUtils.deregisterDriver();
		idle = null;
	}

	private static int getOrDefault(Properties properties, String key, int defaultValue) {
		String value = properties.getProperty(key);
		if (StringUtils.isEmpty(value))
			return defaultValue;
		return Integer.parseInt(value);
	}
}
