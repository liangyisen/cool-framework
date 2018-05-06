package com.eiff.framework.cache.redis.spring;

import java.io.Serializable;

/**
 * @author tangzhaowei
 */
public class Command implements Serializable {

    private static final long serialVersionUID  = 7126530485423286910L;

    public final static byte OPT_SET = 0x01;
    public final static byte OPT_DEL = 0x02;
    public final static byte OPT_REM = 0x03;
    /**
     * 失效后更新，即从多级缓存中拿出数据重新设置
     */
    public final static byte OPT_EXPIRE_UPDATE = 0x10;
    /**
     * 失效后删除
     */
    public final static byte OPT_EXPIRE_DELETE = 0x11;

    public byte oper;
    public String name;
    public String key;
    public Object value;
    public String src;

    public Command() {
    }

    public Command(byte oper, String name, String key, Object value) {
        this.oper = oper;
        this.name = name;
        this.key = key;
        this.value = value;
        this.src = CacheInfo.MODULE;
    }

    public static Command set(String cacheName, String key, Object value) {
        return new Command(OPT_SET, cacheName, key, value);
    }

    public static Command del(String cacheName, String key) {
        return new Command(OPT_DEL, cacheName, key, null);
    }

    public static Command rem(String cacheName) {
        return new Command(OPT_REM, cacheName, null, null);
    }

    @Override
    public String toString() {
        String opt;
        switch (this.oper) {
            case Command.OPT_SET:
                opt = "set";
                break;
            case Command.OPT_DEL:
                opt = "del";
                break;
            case Command.OPT_REM:
                opt = "rem";
                break;
            default:
                opt = "unknown";
        }
        return "Command [oper=" + opt + ", name=" + name + ", key=" + key + ", value=" + value + "]";
    }

}