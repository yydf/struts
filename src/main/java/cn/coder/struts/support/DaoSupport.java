package cn.coder.struts.support;

import java.sql.SQLException;

import cn.coder.struts.jdbc.SqlSession;
import cn.coder.struts.jdbc.SqlSessionBase;
import cn.coder.struts.jdbc.SqlTranction;

public class DaoSupport {
	public SqlSession jdbc() {
		return SqlSessionBase.getSession();
	}
	
	protected boolean tran(Run run) {
		SqlTranction tran = null;
		try {
			tran = jdbc().beginTranction();
			run.exec(tran);
			tran.commit();
			return true;
		} catch (Exception e) {
			tran.rollback(e);
			return false;
		} finally {
			jdbc().close(tran);
		}
	}

	protected interface Run {
		// 执行事务
		void exec(SqlTranction tran) throws SQLException;
	}
}
