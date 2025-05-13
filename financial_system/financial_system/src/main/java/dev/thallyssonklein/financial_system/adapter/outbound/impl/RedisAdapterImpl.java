package dev.thallyssonklein.financial_system.adapter.outbound.impl;

import dev.thallyssonklein.financial_system.adapter.outbound.RedisAdapter;
import dev.thallyssonklein.financial_system.domain.bank.entity.Transaction;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisAdapterImpl implements RedisAdapter {

    private final RedisTemplate<String, Transaction> redisTemplate;

    public RedisAdapterImpl(RedisTemplate<String, Transaction> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Transaction getLastItem(String userId, String type) {
        String key = String.format("%s:%s", userId, type);
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void saveLastItem(String userId, Transaction transaction, String type) {
        String key = String.format("%s:%s", userId, type);
        redisTemplate.opsForValue().set(key, transaction);
    }
}