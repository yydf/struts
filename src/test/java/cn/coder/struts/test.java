package cn.coder.struts;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import cn.coder.struts.jdbc.SqlSession;
import cn.coder.struts.jdbc.SqlSessionBase;

public class test {
	public static void main(String[] args) throws IOException, SQLException {
		InputStream input = test.class.getClassLoader().getResourceAsStream("jdbc.properties");
		if (input != null) {
			Properties properties = new Properties();
			properties.load(input);
			SqlSessionBase.createSession(properties);
		}
		final SqlSession session = SqlSessionBase.getSession();
		List<Integer> nums = session.table("weiki").where("", "").asc("").skip(0).limit(2).list(Integer.class);
		long start = System.currentTimeMillis();
		ExecutorService es = Executors.newCachedThreadPool();
		final AtomicLong count = new AtomicLong(0);
		for (int i = 0; i < 100; i++) {

			es.execute(new Runnable() {

				@Override
				public void run() {
					System.out.println(session.selectOne(Integer.class, "select count(1) from weike"));
					count.incrementAndGet();
				}
			});
		}
		es.shutdown();
		while (true) {
			if (es.isTerminated()) {
				break;
			}
		}
		System.out.println(System.currentTimeMillis() - start + " :" + count.get());
	}
}
