package com.ly.thread;

import java.util.concurrent.TimeUnit;

/**
 * 线程任务
 *
 *
 */
public class WorkItem extends Thread {
	/**
	 * 运行状态
	 *
	 *
	 */
	public static enum RunStatusOfWorkItem{
		STATUS_READY,
		STATUS_RUNNING,
		STATUS_ERROR,
		STATUS_SUCCESS
	}
	/******* 业务属性 *********************/
	/** 超时时间 **/
	private long timeout = -1L;
	/** 时间单位**/
	private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
	/** 重试次数**/
	private int retryTimes = 0;
	/** 运行状态**/
	RunStatusOfWorkItem runningStatus = RunStatusOfWorkItem.STATUS_READY;
	/** 耗时时长（毫秒）**/
	long tokenTime;
	/** 运行异常（若重试多次，这个是最后一次运行的异常，如果最后一次运行成功则为null）**/
	Throwable thrown;
	/******* 非业务属性 *********************/
	/** BeforeExecute是否执行过了**/
	boolean isBeforeExecuted = false;
	/** 临时保存的开始时间的毫秒数**/
	private long tempStart;
	
	public WorkItem(){}
	
	public WorkItem(int retryTimes){
		this.retryTimes = retryTimes;
	}
	
	public WorkItem(int retryTimes, long timeout){
		this(retryTimes);
		this.timeout = timeout;
	}
	
	public WorkItem(int retryTimes, long timeout, TimeUnit timeUnit){
		this(retryTimes, timeout);
		this.timeUnit = timeUnit;
	}
	
	public void beforeExecute(Thread t){}
	
	public void afterExecute(Throwable e){}
	/**
	 * 设置开始运行
	 */
	final void setRunning(){
		this.runningStatus = RunStatusOfWorkItem.STATUS_RUNNING;
		tempStart = System.currentTimeMillis();
	}
	/**
	 * 设置结束运行
	 */
	final void setOver(){
		this.tokenTime = System.currentTimeMillis() - tempStart;
		if (this.thrown == null) {
			this.runningStatus = RunStatusOfWorkItem.STATUS_SUCCESS;
		} else {
			this.runningStatus = RunStatusOfWorkItem.STATUS_ERROR;
		}
		try {
			this.afterExecute(this.thrown);
		} finally {
			this.tempStart = 0L;
		}
	}
	/**
	 * 设置结束运行
	 */
	final void setOver(Throwable e){
		this.thrown = e;
		setOver();
	}
	
	/**
	 * 是否设置Timeout功能
	 * @return
	 */
	public boolean isTimeoutFuncSet(){
		return this.timeout > 0L;
	}
	/**
	 * 是否准备好运行
	 * @return
	 */
	public boolean isReady(){
		return this.runningStatus == RunStatusOfWorkItem.STATUS_READY;
	}
	/**
	 * 是否正在运行中
	 * @return
	 */
	public boolean isRunning(){
		return this.runningStatus == RunStatusOfWorkItem.STATUS_RUNNING;
	}
	/**
	 * 是否运行成功
	 * @return
	 */
	public boolean isSuccess(){
		return this.runningStatus == RunStatusOfWorkItem.STATUS_SUCCESS;
	}
	/**
	 * 是否运行错误
	 * @return
	 */
	public boolean isError(){
		return this.runningStatus == RunStatusOfWorkItem.STATUS_ERROR;
	}
	/**
	 * 是否运行完毕
	 * @return
	 */
	public boolean isOver(){
		return this.isSuccess() || this.isError();
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	
	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	public int getRetryTimes() {
		return retryTimes;
	}

	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

	public RunStatusOfWorkItem getRunningStatus() {
		return runningStatus;
	}

	public long getTokenTime() {
		return tokenTime;
	}

	public Throwable getThrown() {
		return thrown;
	}
	
}
