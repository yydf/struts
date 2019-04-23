package cn.coder.struts.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 定时任务工具类
 * @author YYDF
 * 2019-04-23
 */
public class TaskUtils {

	private static final Logger logger = LoggerFactory.getLogger(TaskUtils.class);
	private static final ScheduledExecutorService manager;
	private static TaskUtils schedule;

	static {
		manager = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
	}

	public synchronized static TaskUtils getInstance() {
		if (schedule == null) {
			schedule = new TaskUtils();
		}
		return schedule;
	}

	public void startTask(Runnable runnable, int delay, TimeUnit unit) {
		logger.debug("Start task " + runnable.toString());
		manager.schedule(runnable, delay, unit);
	}

}
