package com.ly.thread;

import com.ly.thread.WorkItem.RunStatusOfWorkItem;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 自定义线程池
 *
 */
public class MyScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {
	/**
	 * 将BlockingQueue<WorkItem>翻译成BlockingQueue<Runnable>
	 * 	解决泛型子类无法用父类的问题
	 * @param workQueue
	 * @return
	 */
	public static BlockingQueue<Runnable> translateQueueType(BlockingQueue<WorkItem> workQueue){
		if (workQueue == null)
			throw new NullPointerException();
		BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
		queue.addAll(workQueue);
		return queue;
	}
	/**
	 * 构造方法
	 * @param corePoolSize 核心线程池大小
	 * @param maximumPoolSize 线程池最大容量
	 * @param keepAliveTime 线程保持时间
	 * @param unit 时间单位
	 * @param workQueue 线程队列
	 */
	MyScheduledThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
								  BlockingQueue<WorkItem> workQueue) {
		super(corePoolSize);
	}


	/**
	 * 同程自定义线程池核心方法：执行一个线程任务task
	 * 	将一个线程task放入队列中，并使用Future监听线程task的执行情况，同时出发线程task的事件以及属性值的计算
	 * @param task 一个线程任务
	 */
	public void doExecute(WorkItem task){
		/*** 验证 *********/
		if (task == null)
			throw new NullPointerException();
		if (RunStatusOfWorkItem.STATUS_READY != task.runningStatus) {
			throw new RuntimeException("task的状态不是STATUS_READY，拒绝运行");
		}
		/*** 准备数据 *********/
		long timeout = task.getTimeout();
		final boolean isTimeoutFuncSet = task.isTimeoutFuncSet();//表示是否需要设置成超时模式
		TimeUnit timeUnit = task.getTimeUnit();
		int retryTimes = task.getRetryTimes();
		/*if (retryTimes <= 0) {
			retryTimes = 1;
		}*/
		/*** 执行 *********/
		if (isTimeoutFuncSet && timeUnit == null)
			throw new NullPointerException("timeout设置为：" + timeout + "，TimeUnit不可为null，请重新设置");
		//添加到执行队列
		Future<?> f = super.submit(new CallableAdapter<Void>(task, null));
		doExecuteWorkItem(retryTimes, f, (int retry, Future<?> future) -> {
			Throwable thrown = null;
			//第一次执行
			try {
				try {
					if (isTimeoutFuncSet) {
						future.get(timeout, timeUnit);
					} else {
						future.get();
					}
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					if (future != null)
						future.cancel(true);
					thrown = e;
				}
			} catch (RuntimeException x) {
				thrown = x; throw x;
			} catch (Error x) {
				thrown = x; throw x;
			} catch (Throwable x) {
				thrown = x; throw new Error(x);
			}
			//开始重试
			try {
				if (thrown != null) {
					for (int i = 1; i <= retry; i++) {
						try {
							future = this.submit(new CallableAdapter<Void>(task, null));
							//future = TCThreadPoolExecutor.super.submit(task, null);
							//future = TCThreadPoolExecutor.super.submit(new CallableAdapter<Void>(task, null)); //new CallableAdapter<Void>(task, null) //Executors.callable(task, null)
							if (isTimeoutFuncSet) {
								future.get(timeout, timeUnit);
							} else {
								future.get();
							}
							thrown = null;
							break;
						} catch (InterruptedException | ExecutionException | TimeoutException e) {
							if (future != null)
								future.cancel(true);
							thrown = e;
						}
					}
				}
			} catch (RuntimeException x) {
				thrown = x; throw x;
			} catch (Error x) {
				thrown = x; throw x;
			} catch (Throwable x) {
				thrown = x; throw new Error(x);
			} finally {
				task.setOver(thrown);
			}
		});
	}
	/**
	 * 使用异步Future监听WorkItem线程
	 * @param retryTimes
	 * @param executor
	 */
	private void doExecuteWorkItem(int retryTimes, Future<?> future, DoExecute4WorkItem executor){
		Objects.requireNonNull(executor);
		new Thread(() -> {
			executor.doExecute(retryTimes, future);
		}).start();
	}
	
	@Override
	public void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		WorkItem w = getWorkItemFromRunnable(r);
		if (w != null && !w.isBeforeExecuted) {
			w.beforeExecute(t);
			w.setRunning();
			w.isBeforeExecuted = true;
		}
	}
	/**
	 * 由于Callable会被线程池销毁所以由Future代为处理
	 */
	@Override
	public void afterExecute(Runnable r, Throwable e) {
		super.afterExecute(r, e);
	}
	/**
	 * 根据Runnable获取对应的WorkItem对象
	 * @param r
	 * @return
	 */
	protected WorkItem getWorkItemFromRunnable(Runnable r) {
		WorkItem w = null;
		if (r instanceof FutureTask) {
			FutureTask<?> fTask = (FutureTask<?>) r;
			Field fields[] = fTask.getClass().getDeclaredFields();
			final String propStr = "callable";
			for (Field fd : fields) {
				if (propStr.equals(fd.getName())) {
					try {
						fd.setAccessible(true);
						Object value = fd.get(fTask);
						if (value instanceof CallableAdapter) {
							CallableAdapter<?> callable = (CallableAdapter<?>) value;
							w = getWorkItemFromRunnable(callable.task);
						}
					} catch (Exception e) {}
					break;
				}
			}
		} else if (r instanceof WorkItem) {
			w = (WorkItem) r;
		}
		return w;
	}
}
