package com.eiff.framework.cache.redis.spring;

import com.eiff.framework.log.api.HdLogger;
import org.slf4j.Logger;
import com.eiff.framework.cache.redis.spring.CacheInfo.Level;

/**
 * @author tangzhaowei
 */
public class CacheSyncHandler {

    public static final Logger logger = HdLogger.getLogger(CacheSyncHandler.class);

    private TwoLevelCacheTemplate      cacheTemplate;

    public CacheSyncHandler(TwoLevelCacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    private void onSet(String name, String key, Object value) {
        this.cacheTemplate.set(name, key, value, Level.Local);
    }

    private void onDel(String name, String key) {
        this.cacheTemplate.del(name, key, Level.Local);
    }

    private void onRem(String name) {
        this.cacheTemplate.rem(name, Level.Local);
    }

    public void handle(Command cmd) {
        switch (cmd.oper) {
            case Command.OPT_SET:
                onSet(cmd.name, cmd.key, cmd.value);
                break;
            case Command.OPT_DEL:
                onDel(cmd.name, cmd.key);
                break;
            case Command.OPT_REM:
                onRem(cmd.name);
                break;
            default:
                logger.warn("Unknown message type = " + cmd.oper);
        }
    }

}
