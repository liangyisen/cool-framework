package com.eiff.framework.rpc.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

import com.eiff.framework.rpc.controller.DubboRestfulController;
import com.eiff.framework.rpc.controller.condition.LoadDubboRestController;

public class DubboApplicationAutoConfiguration {

	@Bean
	@Conditional(LoadDubboRestController.class)
	public DubboRestfulController registDubboConf() {
		return new DubboRestfulController();
	}
}
