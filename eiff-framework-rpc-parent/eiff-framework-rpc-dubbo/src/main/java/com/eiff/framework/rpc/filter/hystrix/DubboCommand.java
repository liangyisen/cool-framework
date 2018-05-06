package com.eiff.framework.rpc.filter.hystrix;

import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy;

public class DubboCommand extends HystrixCommand<Result> {

//	private static HdLogger LOGGER = HdLogger.getLogger(DubboCommand.class);
	
	private Invocation invocation;
	private Invoker<?> invoker;
	private Result result = null;

	public DubboCommand(final Invoker<?> invoker, final Invocation invocation, final int maxCon) {
		super(Setter
				.withGroupKey(
						HystrixCommandGroupKey.Factory
								.asKey(invoker.getInterface()
										.getName()))
				.andCommandKey(
						HystrixCommandKey.Factory
								.asKey(invocation.getMethodName()
										+ maxCon))
				.andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
						.withExecutionIsolationStrategy(ExecutionIsolationStrategy.SEMAPHORE)
						.withExecutionTimeoutEnabled(false).withExecutionTimeoutInMilliseconds(3000)
						.withCircuitBreakerEnabled(false).withCircuitBreakerErrorThresholdPercentage(20)
						.withCircuitBreakerForceClosed(false).withCircuitBreakerForceOpen(false)
						.withCircuitBreakerRequestVolumeThreshold(10).withCircuitBreakerSleepWindowInMilliseconds(10)
						.withFallbackEnabled(true).withExecutionIsolationSemaphoreMaxConcurrentRequests(maxCon)));
		this.invocation = invocation;
		this.invoker = invoker;
	}

	@Override
	public Result run() {
		result = invoker.invoke(invocation);
		return result;
	}

	@Override
	protected Result getFallback() {
		Throwable throwable = getFailedExecutionException();

		if (throwable == null) {
			throw new RuntimeException();
		}
		else {
			throw new RuntimeException(throwable);
		}
	}
}
