package ink.lusy.lanxinmodel.config;

import ink.lusy.lanxinmodel.properties.LanXinProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LanXin线程池工厂
 * 
 * @author lusy
 * @date 2025-06-17
 */
@Slf4j
public class LanXinThreadPoolFactory {

    /**
     * 创建自定义线程池
     */
    public static ThreadPoolExecutor createThreadPool(LanXinProperties.ThreadPoolConfig config) {
        // 创建有界队列
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(config.getQueueCapacity());
        
        // 创建自定义线程工厂
        ThreadFactory threadFactory = new LanXinThreadFactory(config.getThreadNamePrefix());
        
        // 创建拒绝策略
        RejectedExecutionHandler rejectedHandler = createRejectedExecutionHandler(config.getRejectedExecutionHandler());
        
        // 创建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                config.getCorePoolSize(),
                config.getMaximumPoolSize(),
                config.getKeepAliveTime(),
                TimeUnit.SECONDS,
                workQueue,
                threadFactory,
                rejectedHandler
        );
        
        // 设置核心线程超时
        executor.allowCoreThreadTimeOut(config.isAllowCoreThreadTimeOut());
        
        log.info("LanXin线程池创建成功: coreSize={}, maxSize={}, queueCapacity={}, keepAliveTime={}s", 
                config.getCorePoolSize(), config.getMaximumPoolSize(), 
                config.getQueueCapacity(), config.getKeepAliveTime());
        
        return executor;
    }
    
    /**
     * 创建拒绝策略
     */
    private static RejectedExecutionHandler createRejectedExecutionHandler(String handlerType) {
        return switch (handlerType.toUpperCase()) {
            case "ABORT" -> new ThreadPoolExecutor.AbortPolicy();
            case "CALLER_RUNS" -> new ThreadPoolExecutor.CallerRunsPolicy();
            case "DISCARD" -> new ThreadPoolExecutor.DiscardPolicy();
            case "DISCARD_OLDEST" -> new ThreadPoolExecutor.DiscardOldestPolicy();
            default -> {
                log.warn("未知的拒绝策略: {}, 使用默认策略: CALLER_RUNS", handlerType);
                yield new ThreadPoolExecutor.CallerRunsPolicy();
            }
        };
    }
    
    /**
     * 自定义线程工厂
     */
    private static class LanXinThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        private final ThreadGroup group;
        
        LanXinThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
            // 直接使用当前线程的线程组，避免使用已废弃的SecurityManager
            this.group = Thread.currentThread().getThreadGroup();
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            
            // 设置为非守护线程
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            
            // 设置正常优先级
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            
            // 设置异常处理器
            t.setUncaughtExceptionHandler((thread, ex) -> 
                log.error("LanXin线程池中的线程 {} 发生未捕获异常", thread.getName(), ex));
            
            return t;
        }
    }
    
    /**
     * 优雅关闭线程池
     */
    public static void shutdownGracefully(ExecutorService executor, long timeoutSeconds) {
        if (executor == null || executor.isShutdown()) {
            return;
        }
        
        try {
            log.info("开始关闭LanXin线程池...");
            
            // 停止接收新任务
            executor.shutdown();
            
            // 等待现有任务完成
            if (!executor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                log.warn("线程池在 {}s 内未能正常关闭，强制关闭", timeoutSeconds);
                executor.shutdownNow();
                
                // 再次等待
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.error("线程池强制关闭失败");
                }
            }
            
            log.info("LanXin线程池关闭完成");
            
        } catch (InterruptedException e) {
            log.warn("线程池关闭过程被中断", e);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 获取线程池状态信息
     */
    public static String getThreadPoolStatus(ThreadPoolExecutor executor) {
        if (executor == null) {
            return "线程池未初始化";
        }
        
        return String.format(
                "线程池状态 - 核心线程数: %d, 最大线程数: %d, 当前线程数: %d, " +
                "活跃线程数: %d, 队列大小: %d, 已完成任务数: %d, 总任务数: %d",
                executor.getCorePoolSize(),
                executor.getMaximumPoolSize(),
                executor.getPoolSize(),
                executor.getActiveCount(),
                executor.getQueue().size(),
                executor.getCompletedTaskCount(),
                executor.getTaskCount()
        );
    }
}
