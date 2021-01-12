package com.musinsa.watcher.config.cache.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;

@Slf4j
public class HystrixPutCommand extends HystrixCommand {

  private final Cache globalCache;
  private final Cache localCache;
  private final Object key;
  private final Object value;

  public HystrixPutCommand(Cache localCache, Cache globalCache, Object key, Object value) {
    super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("testGroupKey"))
        .andCommandKey(HystrixCommandKey.Factory.asKey("cache-get"))
        .andCommandPropertiesDefaults(
            HystrixCommandProperties.defaultSetter()
                .withExecutionTimeoutInMilliseconds(1000)
                .withCircuitBreakerErrorThresholdPercentage(50)
                .withCircuitBreakerRequestVolumeThreshold(5)
                .withMetricsRollingStatisticalWindowInMilliseconds(20000)));
    this.globalCache = globalCache;
    this.localCache = localCache;
    this.key = key;
    this.value = value;
  }

  @Override
  protected Object run() {
    localCache.put(key, value);
    globalCache.put(key, value);
    log.info("글로벌 put");
    log.info("로컬 put");
    return null;
  }

  @Override
  protected Object getFallback() {
    log.warn("put fallback called, circuit is {}", super.circuitBreaker.isOpen());
    localCache.put(key, value);
    log.info("로컬 put");
    return null;
  }
}
