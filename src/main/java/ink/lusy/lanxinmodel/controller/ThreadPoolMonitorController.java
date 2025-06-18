package ink.lusy.lanxinmodel.controller;

import ink.lusy.lanxinmodel.config.LanXinStreamingModel;
import ink.lusy.lanxinmodel.config.LanXinThreadPoolFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池监控控制器 [仅供测试]
 * 
 * @author lusy
 * @date 2025-06-17
 */
@Slf4j
@RestController
@RequestMapping("/api/lanxin/monitor")
@RequiredArgsConstructor
public class ThreadPoolMonitorController {

    private final LanXinStreamingModel streamingModel;

    /**
     * 获取线程池状态
     */
    @GetMapping("/thread-pool")
    public Map<String, Object> getThreadPoolStatus() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 通过反射获取线程池实例（仅用于监控）
            Field executorField = LanXinStreamingModel.class.getDeclaredField("executorService");
            executorField.setAccessible(true);
            ThreadPoolExecutor executor = (ThreadPoolExecutor) executorField.get(streamingModel);
            
            if (executor != null) {
                result.put("status", "active");
                result.put("corePoolSize", executor.getCorePoolSize());
                result.put("maximumPoolSize", executor.getMaximumPoolSize());
                result.put("poolSize", executor.getPoolSize());
                result.put("activeCount", executor.getActiveCount());
                result.put("queueSize", executor.getQueue().size());
                result.put("queueRemainingCapacity", executor.getQueue().remainingCapacity());
                result.put("completedTaskCount", executor.getCompletedTaskCount());
                result.put("taskCount", executor.getTaskCount());
                result.put("largestPoolSize", executor.getLargestPoolSize());
                result.put("isShutdown", executor.isShutdown());
                result.put("isTerminated", executor.isTerminated());
                result.put("isTerminating", executor.isTerminating());
                
                // 计算使用率
                double poolUsage = (double) executor.getActiveCount() / executor.getMaximumPoolSize() * 100;
                double queueUsage = (double) executor.getQueue().size() / 
                    (executor.getQueue().size() + executor.getQueue().remainingCapacity()) * 100;
                
                result.put("poolUsagePercent", Math.round(poolUsage * 100.0) / 100.0);
                result.put("queueUsagePercent", Math.round(queueUsage * 100.0) / 100.0);
                
                // 状态描述
                result.put("statusDescription", LanXinThreadPoolFactory.getThreadPoolStatus(executor));
                
            } else {
                result.put("status", "not_initialized");
                result.put("message", "线程池未初始化");
            }
            
        } catch (Exception e) {
            log.error("获取线程池状态失败", e);
            result.put("status", "error");
            result.put("message", "获取线程池状态失败: " + e.getMessage());
        }
        
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 获取线程池健康状态
     */
    @GetMapping("/thread-pool/health")
    public Map<String, Object> getThreadPoolHealth() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Field executorField = LanXinStreamingModel.class.getDeclaredField("executorService");
            executorField.setAccessible(true);
            ThreadPoolExecutor executor = (ThreadPoolExecutor) executorField.get(streamingModel);
            
            if (executor != null && !executor.isShutdown()) {
                // 检查健康状态
                double poolUsage = (double) executor.getActiveCount() / executor.getMaximumPoolSize();
                double queueUsage = (double) executor.getQueue().size() / 
                    (executor.getQueue().size() + executor.getQueue().remainingCapacity());
                
                String healthStatus;
                String message;
                
                if (poolUsage > 0.9 || queueUsage > 0.9) {
                    healthStatus = "unhealthy";
                    message = "线程池使用率过高";
                } else if (poolUsage > 0.7 || queueUsage > 0.7) {
                    healthStatus = "warning";
                    message = "线程池使用率较高";
                } else {
                    healthStatus = "healthy";
                    message = "线程池运行正常";
                }
                
                result.put("status", healthStatus);
                result.put("message", message);
                result.put("poolUsage", Math.round(poolUsage * 100 * 100.0) / 100.0);
                result.put("queueUsage", Math.round(queueUsage * 100 * 100.0) / 100.0);
                
            } else {
                result.put("status", "down");
                result.put("message", "线程池已关闭或未初始化");
            }
            
        } catch (Exception e) {
            log.error("检查线程池健康状态失败", e);
            result.put("status", "error");
            result.put("message", "检查线程池健康状态失败: " + e.getMessage());
        }
        
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 获取线程池配置信息
     */
    @GetMapping("/thread-pool/config")
    public Map<String, Object> getThreadPoolConfig() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Field executorField = LanXinStreamingModel.class.getDeclaredField("executorService");
            executorField.setAccessible(true);
            ThreadPoolExecutor executor = (ThreadPoolExecutor) executorField.get(streamingModel);
            
            if (executor != null) {
                result.put("corePoolSize", executor.getCorePoolSize());
                result.put("maximumPoolSize", executor.getMaximumPoolSize());
                result.put("keepAliveTime", executor.getKeepAliveTime(java.util.concurrent.TimeUnit.SECONDS));
                result.put("queueType", executor.getQueue().getClass().getSimpleName());
                result.put("rejectedExecutionHandler", executor.getRejectedExecutionHandler().getClass().getSimpleName());
                result.put("allowsCoreThreadTimeOut", executor.allowsCoreThreadTimeOut());
            } else {
                result.put("message", "线程池未初始化");
            }
            
        } catch (Exception e) {
            log.error("获取线程池配置失败", e);
            result.put("error", "获取线程池配置失败: " + e.getMessage());
        }
        
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}
