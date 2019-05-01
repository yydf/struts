package cn.coder.struts.jdbc;

import java.sql.Connection;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据库事务处理对象
 * 
 * @author YYDF
 *
 */
public class SqlTranction {

	private final Connection conn;
	private static final Logger logger = LoggerFactory.getLogger(SqlTranction.class);

	public SqlTranction(Connection connection) throws SQLException {
		connection.setAutoCommit(false);
		this.conn = connection;
	}

	public Connection Connection() {
		return this.conn;
	}

	public void commit() {
		try {
			this.conn.commit();
			logger.debug("Tranction commited");
		} catch (SQLException e) {
			logger.error("Tranction commit faild", e);
		}
	}

	public void rollback(Exception e) {
		try {
			this.conn.rollback();
			logger.debug("Tranction rollbacked");
			logger.error("Tranction error:", e);
		} catch (SQLException ex) {
			logger.error("Tranction rollback faild", ex);
		}
	}
}
