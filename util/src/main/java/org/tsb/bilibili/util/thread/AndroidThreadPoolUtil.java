package org.tsb.bilibili.util.thread;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 用法:
 *      AndroidThreadPoolUtil.getInstance().mainThread().execute(new Runnable() {
 *          @Override
 *          public void run() {
 *              //do something
 *          }
 *      });
 */
public class AndroidThreadPoolUtil {
    private final Executor mDiskIO;

    private final Executor mNetworkIO;

    private final Executor mMainThread;
    private final ScheduledThreadPoolExecutor schedule;

    private static AndroidThreadPoolUtil instance;

    private static Object object = new Object();


    /**
     * UI线程
     */
    public Executor mainThread() {
        return mMainThread;
    }

    public static AndroidThreadPoolUtil getInstance() {
        if (instance == null) {
            synchronized (object) {
                if (instance == null) {
                    instance = new AndroidThreadPoolUtil();
                }
            }
        }
        return instance;
    }

    private AndroidThreadPoolUtil() {

        this.mDiskIO = Executors.newSingleThreadExecutor(new MyThreadFactory("single"));

        this.mNetworkIO = Executors.newFixedThreadPool(3, new MyThreadFactory("fixed"));

        this.mMainThread = new MainThreadExecutor();

        this.schedule = new ScheduledThreadPoolExecutor(
                5,
                new MyThreadFactory("sc"),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    static class MyThreadFactory implements ThreadFactory {

        private final String name;
        private int count = 0;

        MyThreadFactory(String name) {
            this.name = name;
        }

        @Override
        public Thread newThread(@NonNull Runnable r) {
            count++;
            return new Thread(r, name + "-" + count + "-Thread");
        }
    }

    /**
     * 磁盘IO线程池
     */
    public Executor diskIO() {
        return mDiskIO;
    }

    /**
     * 给定延时后执行异步任务或者周期性执行任务
     *
     * // 达到给定的延时时间后，执行任务。这里传入的是实现Runnable接口的任务，
     * // 因此通过ScheduledFuture.get()获取结果为null
     * public ScheduledFuture<?> schedule(Runnable command,
     *                                        long delay, TimeUnit unit);
     * // 达到给定的延时时间后，执行任务。这里传入的是实现Callable接口的任务，
     * // 因此，返回的是任务的最终计算结果
     *  public <V> ScheduledFuture<V> schedule(Callable<V> callable,
     *                                            long delay, TimeUnit unit);
     *
     * // 是以上一个任务开始的时间计时，period时间过去后，
     * // 检测上一个任务是否执行完毕，如果上一个任务执行完毕，
     * // 则当前任务立即执行，如果上一个任务没有执行完毕，则需要等上一个任务执行完毕后立即执行
     * public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
     *                                                   long initialDelay,
     *                                                   long period,
     *                                                   TimeUnit unit);
     * // 当达到延时时间initialDelay后，任务开始执行。上一个任务执行结束后到下一次
     * // 任务执行，中间延时时间间隔为delay。以这种方式，周期性执行任务。
     * public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
     *                                                      long initialDelay,
     *                                                      long delay,
     *                                                      TimeUnit unit);
     */
    public ScheduledThreadPoolExecutor schedule() {
        return schedule;
    }

    /**
     * 网络IO线程池
     */
    public Executor networkIO() {
        return mNetworkIO;
    }


    /**
     * 执行器
     */
    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        /**
         * 将线程任务附加到main线程运行
         */
        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
