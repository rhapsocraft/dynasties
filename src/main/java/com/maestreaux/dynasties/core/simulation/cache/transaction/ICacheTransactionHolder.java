package com.maestreaux.dynasties.core.simulation.cache.transaction;

import java.util.List;

public interface ICacheTransactionHolder<T extends ICacheTransaction> {
    List<T> getTransactions();
    void insertTransaction(T cacheTransaction);
    boolean flushTransactions();
}
