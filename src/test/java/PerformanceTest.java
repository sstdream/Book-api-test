import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PerformanceTest {

    // 配置参数
    private static final String BASE_URL = "http://localhost:8081/api/books"; // 修改为你的服务地址
    private static final int THREAD_COUNT = 10;      // 并发线程数
    private static final int TOTAL_REQUESTS = 100;   // 总请求数
    private static final boolean PRINT_DETAIL = false; // 是否打印每个请求的耗时

    // 统计变量
    private static final List<Long> responseTimes = new ArrayList<>();
    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failCount = new AtomicInteger(0);
    private static final AtomicLong totalDuration = new AtomicLong(0);
    private static long startTime;
    private static long endTime;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("========== 性能测试开始 ==========");
        System.out.printf("并发线程数: %d, 总请求数: %d%n", THREAD_COUNT, TOTAL_REQUESTS);
        System.out.println("目标 URL: " + BASE_URL);

        // 预热（可选），让 JVM 充分优化
        warmUp();

        // 创建线程池和闭锁
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(TOTAL_REQUESTS);

        // 记录开始时间
        startTime = System.currentTimeMillis();

        // 提交任务
        for (int i = 0; i < TOTAL_REQUESTS; i++) {
            final int requestId = i;
            executor.submit(() -> {
                long start = System.currentTimeMillis();
                try {
                    // 发送 GET 请求（获取所有书籍，也可以改为其他接口）
                    Response response = RestAssured
                            .given()
                            .when()
                            .get(BASE_URL)
                            .then()
                            .extract().response();

                    int statusCode = response.getStatusCode();
                    if (statusCode >= 200 && statusCode < 300) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                        if (PRINT_DETAIL) {
                            System.err.println("请求失败, 状态码: " + statusCode);
                        }
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    if (PRINT_DETAIL) {
                        System.err.println("请求异常: " + e.getMessage());
                    }
                }
                long duration = System.currentTimeMillis() - start;
                synchronized (responseTimes) {
                    responseTimes.add(duration);
                }
                latch.countDown();
            });
        }

        // 等待所有请求完成
        latch.await();
        executor.shutdown();

        // 记录结束时间
        endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // 统计结果
        printStatistics(totalTime);

        System.out.println("========== 性能测试结束 ==========");
    }

    /** 预热：发送少量请求，让 JIT 优化 */
    private static void warmUp() {
        System.out.println("预热中...");
        for (int i = 0; i < 10; i++) {
            try {
                RestAssured.get(BASE_URL).then().statusCode(200);
            } catch (Exception ignored) {
            }
        }
        System.out.println("预热完成");
    }

    /** 输出统计结果 */
    private static void printStatistics(long totalTime) {
        double[] values = responseTimes.stream().mapToLong(Long::longValue).asDoubleStream().toArray();
        DescriptiveStatistics stats = new DescriptiveStatistics(values);

        double avg = stats.getMean();
        double median = stats.getPercentile(50);
        double p95 = stats.getPercentile(95);
        double p99 = stats.getPercentile(99);
        double min = stats.getMin();
        double max = stats.getMax();

        double tps = (TOTAL_REQUESTS * 1000.0) / totalTime;

        System.out.println("\n========== 性能测试报告 ==========");
        System.out.printf("总请求数: %d%n", TOTAL_REQUESTS);
        System.out.printf("成功数: %d%n", successCount.get());
        System.out.printf("失败数: %d%n", failCount.get());
        System.out.printf("总耗时: %.2f 秒%n", totalTime / 1000.0);
        System.out.printf("TPS: %.2f 请求/秒%n", tps);
        System.out.println("\n响应时间统计 (毫秒):");
        System.out.printf("  平均值: %.2f%n", avg);
        System.out.printf("  中位数: %.2f%n", median);
        System.out.printf("  95%%分位: %.2f%n", p95);   // 注意：使用 %%
        System.out.printf("  99%%分位: %.2f%n", p99);   // 注意：使用 %%
        System.out.printf("  最小值: %.0f%n", min);
        System.out.printf("  最大值: %.0f%n", max);
        System.out.println("===================================");
    }
}