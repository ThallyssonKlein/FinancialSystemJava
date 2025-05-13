package dev.thallyssonklein.financial_system.domain.bank.service;

import dev.thallyssonklein.financial_system.domain.bank.entity.Transaction;
import dev.thallyssonklein.financial_system.adapter.outbound.RedisAdapter;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ProcessFilesService {

    private final RedisAdapter creditCardAdapter;
    private final RedisAdapter extractAdapter;

    public ProcessFilesService(RedisAdapter creditCardAdapter, RedisAdapter extractAdapter) {
        this.creditCardAdapter = creditCardAdapter;
        this.extractAdapter = extractAdapter;
    }

    public void processFiles(List<Transaction> transactions, String userId, String type) {
        // 0 - Ordenar os itens por data
        List<Transaction> orderedItems = orderItemsByDate(transactions);

        // 1 - Buscar o último item salvo no Redis
        Transaction lastItem = getLastItemKeyValueDatabase(userId, type);

        // 2 - Procurar nos itens recebidos se há novos itens após o último item salvo
        int foundIndex = lookForLastItemInArray(orderedItems, lastItem);

        // 2.1 - Se não encontrar o último item, considerar todos como novos
        if (foundIndex == -1) {
            printNewItems(orderedItems);
        } else {
            // Se encontrar o último item, considerar apenas os novos itens após ele
            printNewItems(orderedItems.subList(foundIndex + 1, orderedItems.size()));
        }

        // 4 - Salvar o último item no Redis
        if (type.equals("credit")) {
            creditCardAdapter.saveLastItem(userId, lastItem, type);
        } else {
            extractAdapter.saveLastItem(userId, lastItem, type);
        }
    }

    private List<Transaction> orderItemsByDate(List<Transaction> items) {
        items.sort(Comparator.comparing(Transaction::getDate));
        return items;
    }

    private Transaction getLastItemKeyValueDatabase(String userId, String type) {
        if (type.equals("credit")) {
            return creditCardAdapter.getLastItem(userId, type);
        }
        return extractAdapter.getLastItem(userId, type);
    }

    private int lookForLastItemInArray(List<Transaction> items, Transaction lastItem) {
        for (int i = 0; i < items.size(); i++) {
            Transaction item = items.get(i);
            if (item.getDate().equals(lastItem.getDate()) &&
                    item.getDescription().equals(lastItem.getDescription()) &&
                    item.getValue().equals(lastItem.getValue())) {
                return i;
            }
        }
        return -1;
    }

    private void printNewItems(List<Transaction> items) {
        for (Transaction item : items) {
            System.out.println(item);
        }
    }
}