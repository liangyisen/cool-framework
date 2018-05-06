package com.eiff.framework.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.eiff.framework.test.automock.CacheClaz;
import com.eiff.framework.test.automock.FsClaz;
import com.eiff.framework.test.automock.IdgenClaz;
import com.eiff.framework.test.automock.JobClaz;
import com.eiff.framework.test.automock.MockInfo;
import com.eiff.framework.test.automock.MqClaz;
import com.eiff.framework.test.automock.RpcClaz;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface AutoMockComponent {
	Class<? extends MockInfo>[] values() default { CacheClaz.class, FsClaz.class, IdgenClaz.class, JobClaz.class,
			MqClaz.class, RpcClaz.class };
}
