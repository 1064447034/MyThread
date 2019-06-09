package com.ly.thread;

import java.util.concurrent.FutureTask;

/**
 * 自定义Future实现类，继承自FutureTask主要存储被封装的WorkItem，避免callable被销毁之后，WorkItem找不着的问题
 *
 * @param <V>
 */
public class MyFutureTask<V> extends FutureTask<V> {
	/** 被执行线程 **/
	protected WorkItem task;
	/**
	 * 构造器
	 * @param callable 自定义Callable
	 */
	public MyFutureTask(CallableAdapter<V> callable) {
		super(callable);
		this.task = callable.task;
	}
	/**
	 * 构造器
	 * @param runnable 被执行线程
	 * @param result 执行结果
	 */
    public MyFutureTask(WorkItem runnable, V result) {
        super(runnable, result);
        this.task = runnable;
    }
}
