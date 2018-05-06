package com.eiff.framework.rpc.filter.hystrix;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;

@Activate(group = { com.alibaba.dubbo.common.Constants.CONSUMER })
public class HystrixConsumerFilter implements Filter {

//	private static HdLogger LOGGER = HdLogger.getLogger(DubboProviderFilter.class);

	@Override
	public Result invoke(Invoker<?> invoker, final Invocation invocation) throws RpcException {
		return invoker.invoke(invocation);
	}
}