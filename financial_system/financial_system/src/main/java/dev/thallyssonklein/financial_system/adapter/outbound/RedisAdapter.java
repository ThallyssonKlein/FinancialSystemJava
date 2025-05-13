package dev.thallyssonklein.financial_system.adapter.outbound;

import dev.thallyssonklein.financial_system.domain.bank.entity.Transaction;

public interface RedisAdapter {
    Transaction getLastItem(String userId, String type);

    void saveLastItem(String userId, Transaction transaction, String type);
}