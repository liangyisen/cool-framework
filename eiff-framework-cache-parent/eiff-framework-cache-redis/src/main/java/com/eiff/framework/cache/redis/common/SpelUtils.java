package com.eiff.framework.cache.redis.common;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

/**
 * @author tangzhaowei
 */
public class SpelUtils {
	public static Object getValue(Object[] args, String key, Method method, BeanFactory beanFactory) {
		try {
			Validate.notEmpty(args);
			Validate.notBlank(key);

			ParameterNameDiscoverer paraNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
			String[] paraNames = paraNameDiscoverer.getParameterNames(method);

			if (args.length != paraNames.length) {
				throw new IllegalArgumentException("args length must be equal to paraNames length");
			}

			ExpressionParser ep = new SpelExpressionParser();
			StandardEvaluationContext context = new StandardEvaluationContext();

			for (int i = 0; i < paraNames.length; i++) {
				context.setVariable(paraNames[i], args[i]);
			}

			if (beanFactory != null) {
				context.setBeanResolver(new BeanFactoryResolver(beanFactory));
			}
			return ep.parseExpression(key).getValue(context);
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Boolean getBoolean(Object[] args, String key, Method method, BeanFactory beanFactory) {
		Object value = getValue(args, key, method, beanFactory);

		if (value != null && value instanceof Boolean) {
			return (Boolean) value;
		}
		return false;
	}
}
