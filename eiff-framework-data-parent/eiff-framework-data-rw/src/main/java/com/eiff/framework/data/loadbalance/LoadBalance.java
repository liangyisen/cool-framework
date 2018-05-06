package com.eiff.framework.data.loadbalance;

public interface LoadBalance<T> {

	T elect();
}
