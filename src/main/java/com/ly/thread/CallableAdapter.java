package com.ly.thread;

import java.util.concurrent.Callable;

/**
 * 自定义Callable类，为了更好地在事件中获取WorkItem对象
 *
 *
 * @param <T>
 */
public class CallableAdapter<T> implements Callable<T> {
	final WorkItem task;
    final T result;
    
    CallableAdapter(WorkItem task, T result) {
        this.task = task;
        this.result = result;
    }
    
    @Override
	public T call() {
        task.run();
        return result;
    }
}
