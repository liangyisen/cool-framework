package com.eiff.framework.rpc.invoker;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Cluster;
import com.alibaba.dubbo.rpc.cluster.Directory;

public class BaseFailoverCluster implements Cluster {

	@SuppressWarnings("unused")
	private Cluster cluster;

	public BaseFailoverCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	@Override
	public <T> Invoker<T> join(Directory<T> directory) throws RpcException {
		return new BaseFailoverClusterInvoker<>(directory);
	}
}
