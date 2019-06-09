package com.ly.thread;

import java.util.concurrent.Future;

/**
 * 线程池中，单独执行一次WorkItem线程
 *
 *
 */
public interface DoExecute4WorkItem {
	/**
	 * 执行
	 * @return true：成功	false：失败
	 */
	void doExecute(int retryTimes, Future<?> future);
}
