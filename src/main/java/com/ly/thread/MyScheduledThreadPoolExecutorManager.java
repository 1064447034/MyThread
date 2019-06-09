package com.ly.thread;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 同程线程池管理器
 *
 *
 */
public class MyScheduledThreadPoolExecutorManager {
	/**
	 * 线程池容器
	 */
	private static volatile Map<String, MyScheduledThreadPoolExecutorManager> threadsExecutors = new Hashtable<String, MyScheduledThreadPoolExecutorManager>();
	/**
	 * 默认线程池大小
	 */
	private static final int DEFAULT_POOL_SIZE = 10;
	/**
	 * 以名称获得一个线程池管理器实例
	 * @param name
	 * @return
	 */
	public static MyScheduledThreadPoolExecutorManager getInstance(String name){
		return getInstance(name, -1, false);
	}
	/**
	 * 以名称，线程池大小，是否重设线程池大小，获得一个线程池管理器实例
	 * @param name 名称
	 * @param poolSize 线程池大小
	 * @param resetSize 是否重设线程池大小
	 * @return
	 */
	public static MyScheduledThreadPoolExecutorManager getInstance(String name, int poolSize, boolean resetSize){
		MyScheduledThreadPoolExecutorManager instance = null;
		if (threadsExecutors.containsKey(name)) {
			instance = threadsExecutors.get(name);
			if (resetSize)
				instance.resetSize(poolSize);
		} else {
			instance = new MyScheduledThreadPoolExecutorManager(name, poolSize);

			threadsExecutors.put(name, instance);
		}
		return instance;
	}

	/**
	 * 名称
	 */
	private String name;
	/**
	 * 线程池实例
	 */
	private MyScheduledThreadPoolExecutor executor;

	/**
	 * 空构造
	 */
	private MyScheduledThreadPoolExecutorManager(){}
	/**
	 * 名称构造器
	 * @param name 名称
	 */
	private MyScheduledThreadPoolExecutorManager(String name){
		this.name = name;
		generateExecutor(DEFAULT_POOL_SIZE);
	}
	/**
	 * 名称，线程池大小 构造器
	 * @param name 名称
	 * @param poolSize 线程池大小
	 */
	private MyScheduledThreadPoolExecutorManager(String name, int poolSize){
		this.name = name;
		generateExecutor(poolSize);
	}
	
	/**
	 * 生成一个线程池
	 * @param size 线程池大小
	 */
	private void generateExecutor(int size){
		generateExecutor(size, TimeUnit.MILLISECONDS);
	}
	/**
	 * 生成一个线程池
	 * @param size 线程池大小
	 * @param timeUnit 时间单位
	 */
	private void generateExecutor(int size, TimeUnit timeUnit){
		if (size <= 0) {
			size = DEFAULT_POOL_SIZE;
		}
		executor = new MyScheduledThreadPoolExecutor(size, size, 0L, timeUnit, new LinkedBlockingQueue<WorkItem>());
	}
	/**
	 * 重设线程池大小
	 * @param size 线程池大小
	 */
	private void resetSize(int size){
		if (size <= 0) {
			size = DEFAULT_POOL_SIZE;
		}
		executor.setCorePoolSize(size);
		executor.setMaximumPoolSize(size);
	}
	
	/**
	 * 执行单个任务
	 * @param task 任务
	 */
	public void doExecute(WorkItem task,Long time){
		executor.schedule(task,time,TimeUnit.SECONDS);
	}
	/**
	 * 执行不定个数任务
	 * @param task 任务（集）
	 */
	public void doExecute(Long time,WorkItem... task){
		if (task == null)
			throw new IllegalArgumentException("参数为null");
		for (WorkItem w : task) {
			this.doExecute(w,time);
		}
	}
	/**
	 * 执行多个任务
	 * @param tasks 任务集
	 */
	public void doExecute(Long time,Collection<WorkItem> tasks){
		if (tasks == null)
			throw new IllegalArgumentException("参数为null");
		for (WorkItem w : tasks) {
			this.doExecute(time,w);
		}
	}
	/**
	 * 关闭线程池，线程池关闭后线程池容器将销毁该管理器实例
	 */
	public void shutdown(){
		try {
			executor.shutdown();
			while (!executor.isTerminated()) {
				/*try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}*/
			}
		} finally {
			threadsExecutors.remove(this.name);
		}
	}
	/**
	 * 强制关闭线程池，线程池关闭后线程池容器将销毁该管理器实例
	 */
	public void shutdownNow(){
		try {
			executor.shutdownNow();
			while (true) {
				if (executor.isTerminated() || executor.isShutdown())
					break;
				/*try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}*/
			}
		} finally {
			threadsExecutors.remove(this.name);
		}
	}

	/**
	 * 当前线程池中实际在运行的线程数目
	 * @return
	 */
	public int getActiveThreadCount() {
		return executor.getActiveCount();
	}
	
	public String getName() {
		return name;
	}
	
	public static void main(String[] args) {
		List<WorkItem> wis = new ArrayList<WorkItem>();
		for (int i = 0; i < 10000; i++) {
			WorkItem wi1 = new WorkItem(0){
				@Override
				public void beforeExecute(Thread t) {
					System.out.println(this.getName() + " before测试1");
				}
				
				@Override
				public void afterExecute(Throwable e) {
					System.out.println(this.getName() + " after测试1");
					System.out.println(this.getName() + "-" + this.runningStatus + "," + this.tokenTime);
				}
				
				@Override
				public void run() {
					System.out.println(this.getName() + "测试1");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						System.out.println(this.getName() + "被吵醒了~");
					}
					System.out.println(this.getName() + "执行成功~");
				}
			};
			wis.add(wi1);
		}
		/*for (int i = 0; i < 100; i++) {
			WorkItem wi2 = new WorkItem(0, 1000){
				int count = 1;
				
				@Override
				public void beforeExecute(Thread t) {
					System.out.println(this.getName() + " before测试2");
				}

				@Override
				public void afterExecute(Throwable e) {
					System.out.println(this.getName() + " after测试2");
					System.out.println(this.getName() + "-" + this.runningStatus + "," + this.tokenTime);
				}
				
				@Override
				public void run() {
					System.out.println(this.getName() + "第" + count + "次执行-测试2");
					if (count++ <= 2) {
						System.out.println("执行异常~");
						throw new RuntimeException("测试2");
					}
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						System.out.println(this.getName() + "被吵醒了~");
						throw new RuntimeException(e);
					}
					System.out.println(this.getName() + "执行成功~");
				}
			};
			wis.add(wi2);
		}*/
		long now = System.currentTimeMillis();
		MyScheduledThreadPoolExecutorManager manager = MyScheduledThreadPoolExecutorManager.getInstance("pool3test");
//		manager.doExecute(wis);
		manager.shutdown();
		boolean flag = true;
		for (WorkItem w : wis) {
			if (w.isError()) {
				flag = false;
				break;
			}
		}
		if (flag) {
			System.out.println("程序执行成功，耗时" + (System.currentTimeMillis() - now) + "毫秒");
		} else {
			System.out.println("程序执行失败，耗时" + (System.currentTimeMillis() - now) + "毫秒");
		}
	}

	public Map<String,Object> getThreadCount(){
		return new HashMap<String,Object>(){{
			put("TaskCount",executor.getTaskCount());
			put("CompletedTaskCount",executor.getCompletedTaskCount());
			put("LargestPoolSize",executor.getLargestPoolSize());
			put("ActiveCount",executor.getActiveCount());
			put("PoolSize",executor.getPoolSize());
		}};
	}

	public static MyScheduledThreadPoolExecutorManager getThreadInstance(String name){
		MyScheduledThreadPoolExecutorManager instance = null;
		if (threadsExecutors.containsKey(name)) {
			instance = threadsExecutors.get(name);
		}
		return instance;
	}


}
