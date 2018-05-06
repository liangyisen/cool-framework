package com.eiff.framework.rpc.controller.condition;

import org.slf4j.Logger;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.eiff.framework.log.api.HdLogger;


public class LoadDubboRestController implements Condition{
	
	Logger HDLOGGER = HdLogger.getLogger(Condition.class);
	
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		if (System.getProperty("export_restful") != null) {
			try {
				return Boolean.valueOf(System.getProperty("export_restful"));
			} catch (Exception e) {
				HDLOGGER.warn("", e);
			}
		}
		return false;
	}
}
