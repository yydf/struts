package cn.coder.struts.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadEx {

	private static final Logger logger = LoggerFactory.getLogger(ThreadEx.class);
	private static ThreadPoolExecutor executor;

	public static void init() {
		if (executor == null) {
			int core = Runtime.getRuntime().availableProcessors();
			LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(core * 1024);
			executor = new ThreadPoolExecutor(core, core * 1024, 5L, TimeUnit.SECONDS, queue);
		}
		logger.debug("Thread executor inited");
	}

	public static void run(Runnable r) {
		logger.debug("Runnable begin");
		executor.execute(r);
		logger.debug("Runnable end");
	}
}
