package org.tsb.bilibili.util.thread;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 功能更强的自定义线程池:
 * // 参1:核心线程数，除非allowCoreThreadTimeOut被设置为true，否则它闲着也不会死
 * // 参2:最大线程数,活动线程数量超过它，后续任务就会排队  ;
 * // 参3:超时时长，作用于非核心线程（allowCoreThreadTimeOut被设置为true时也会同时作用于核心线程），闲置超时便被回收 ;
 * // 参4:枚举类型，设置keepAliveTime的单位;
 * // 参5:缓冲任务队列，线程池的execute方法会将Runnable对象存储起来  ;
 * // 参6:线程工厂接口，只有一个new Thread(Runnable r)方法，可为线程池创建新线程  ;
 *  public ThreadPoolExecutor(  int corePoolSize,
 *                              int maximumPoolSize,
 *                              long keepAliveTime,
 *                              TimeUnit unit,
 *                              BlockingQueue<Runnable> workQueue,
 *                              ThreadFactory threadFactory)
 */
public class ThreadTaskUtil {
    /**
     * 线程任务的实例
     */
    private static ThreadTaskUtil instance;

    /**
     * 网络线程最大数量
     */
    private final int netThreadCount = 5;

    /**
     * 数据库线程最大数量
     */
    private final int dbThreadCount = 3;

    /**
     * 其他类型的耗时线程数量
     */
    private final int otherThreadCount = 10;

    /**
     * 数据库线程池
     */
    private ThreadPoolExecutor dbThreadPool;

    /**
     * 网络线程池
     */
    private ThreadPoolExecutor netThreadPool;

    /**
     * 其他耗时操作线程池
     */
    private ThreadPoolExecutor otherThreadPool;

    /**
     * 数据库线程队列
     */
    private PriorityBlockingQueue dbThreadQueue;

    /**
     * 网络线程队列
     */
    private PriorityBlockingQueue netThreadQueue;

    /**
     * 其他线程队列
     */
    private PriorityBlockingQueue otherThreadQueue;

    /**
     * 任务比较
     */
    private Comparator<PrioriTask> taskCompare;

    private ThreadTaskUtil() {
        final long keepAliveTime = 60L;
        taskCompare = new TaskCompare();
        dbThreadQueue = new PriorityBlockingQueue<PrioriTask>(dbThreadCount, taskCompare);
        netThreadQueue = new PriorityBlockingQueue<PrioriTask>(netThreadCount, taskCompare);
        otherThreadQueue = new PriorityBlockingQueue<PrioriTask>(dbThreadCount, taskCompare);

        dbThreadPool = new ThreadPoolExecutor(dbThreadCount, dbThreadCount, 0L, TimeUnit.MILLISECONDS, dbThreadQueue);
        netThreadPool = new ThreadPoolExecutor(netThreadCount, netThreadCount, 0L, TimeUnit.MILLISECONDS, netThreadQueue);
        otherThreadPool = new ThreadPoolExecutor(otherThreadCount, Integer.MAX_VALUE, keepAliveTime, TimeUnit.SECONDS, otherThreadQueue);
    }

    /**
     * 获取线程管理实例
     *
     * @return 线程管理实例
     */
    public static ThreadTaskUtil getInstance() {
        if (instance == null) {
            instance = new ThreadTaskUtil();
        }
        return instance;
    }

    /**
     * 获取网络线程池
     *
     * @return
     */
    public ThreadPoolExecutor getNetThreadPool() {
        return netThreadPool;
    }

    /**
     * 执行数据库线程
     *
     * @param task     需要执行的任务
     * @param priority 优先级 {@link ThreadPeriod}
     */
    public void executorDBThread(Runnable task, int priority) {
        if (!dbThreadPool.isShutdown()) {
            dbThreadPool.execute(new PrioriTask(priority, task));
        }
    }

    /**
     * 执行网络线程
     *
     * @param task     需要执行的任务
     * @param priority {@link ThreadPeriod} 优先级
     */
    public void executorNetThread(Runnable task, int priority) {

        if (!netThreadPool.isShutdown()) {
            netThreadPool.execute(new PrioriTask(priority, task));
        }
    }

    /**
     * 执行除数据库之外的其他耗时任务
     *
     * @param task     需要执行的任务
     * @param priority {@link ThreadPeriod} 优先级
     */
    public void executorOtherThread(Runnable task, int priority) {
        if (!otherThreadPool.isShutdown()) {
            otherThreadPool.execute(new PrioriTask(priority, task));
        }
    }

    /**
     * 结束掉所有线程,并且回收（正在进行的有可能结束不掉）
     */
    public void shutDownAll() {
        netThreadPool.shutdownNow();
        dbThreadPool.shutdownNow();
        otherThreadPool.shutdownNow();
        instance = null;
    }

    /**
     * 优先级任务
     *
     * @author RES-KUNZHU
     */
    public class PrioriTask implements Runnable {
        private int priori;

        private Runnable task;

        /**
         * Cnstructor Method。
         *
         * @param priority 优先级
         * @param runnable 线程
         */
        public PrioriTask(int priority, Runnable runnable) {
            priori = priority;
            task = runnable;
        }

        public int getPriori() {
            return priori;
        }

        public void setPriori(int priori) {
            this.priori = priori;
        }

        public Runnable getTask() {
            return task;
        }

        public void setTask(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            if (task != null) {
                task.run();
            }
        }

    }

    /**
     * 任务比较器
     *
     * @author RES-KUNZHU
     */
    public class TaskCompare implements Comparator<PrioriTask> {

        @Override
        public int compare(PrioriTask lhs, PrioriTask rhs) {
            return rhs.getPriori() - lhs.getPriori();
        }
    }

    /**
     * 线程优先级
     *
     * @author RES-KUNZHU
     */
    public static class ThreadPeriod {
        /**
         * 线程优先级 低
         */
        public static final int PERIOD_LOW = 1;

        /**
         * 线程优先级 中
         */
        public static final int PERIOD_MIDDLE = 5;

        /**
         * 线程优先级 高
         */
        public static final int PERIOD_HIGHT = 10;
    }
}
