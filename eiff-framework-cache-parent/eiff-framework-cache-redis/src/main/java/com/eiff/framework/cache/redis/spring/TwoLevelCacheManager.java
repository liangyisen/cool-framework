package com.eiff.framework.cache.redis.spring;

import org.springframework.cache.Cache;
import org.springframework.cache.transaction.AbstractTransactionSupportingCacheManager;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author tangzhaowei
 */
public class TwoLevelCacheManager extends AbstractTransactionSupportingCacheManager {

    private TwoLevelCacheTemplate cacheTemplate;

    public TwoLevelCacheManager() {
    }

    public TwoLevelCacheManager(TwoLevelCacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
    }

    @Override
    protected Collection<Cache> loadCaches() {
        Set<String> names = this.cacheTemplate.names();
        Collection<Cache> caches = new LinkedHashSet<Cache>(names.size());
        for (String name : names) {
            caches.add(new TwoLevelCache(name, this.cacheTemplate));
        }
        return caches;
    }

    @Override
    protected Cache getMissingCache(String name) {
        return new TwoLevelCache(name, this.cacheTemplate);
    }

    public TwoLevelCacheTemplate getCacheTemplate() {
        return cacheTemplate;
    }

    public void setCacheTemplate(TwoLevelCacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }
}
