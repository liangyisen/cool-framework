package com.eiff.framework.cache.redis.cache;

import org.springframework.cache.annotation.Cacheable;

@Cacheable
public class AccountService {
    @Cacheable(value = "testCache", keyGenerator = "prefixKeyGenerator")
    public Account getAccountByName(String userName) {
        System.out.println("real query account."+userName);
        return getFromDB(userName);
    }

    private Account getFromDB(String acctName) {
        System.out.println("real querying db..."+acctName);
        return new Account(acctName);
    }
}
