package cn.coder.struts.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadEx {
	static int corePoolSize = Runtime.getRuntime().availableProcessors();

	private static ThreadPoolExecutor domainExecutor = new ThreadPoolExecutor(
			corePoolSize, 
			corePoolSize * 2 + 1, 
			1L,
			TimeUnit.MINUTES, 
			new LinkedBlockingQueue<Runnable>(64));

	public static void execute(Runnable runnable) {
		if (runnable != null) {
			domainExecutor.execute(runnable);
		}
	}
}
