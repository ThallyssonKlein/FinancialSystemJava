package dev.thallyssonklein.financial_system.domain.bank.service;

import dev.thallyssonklein.financial_system.domain.bank.entity.Transaction;
import dev.thallyssonklein.financial_system.adapter.outbound.OutboundKeyValueDatabaseAdapter;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;

@Service
public class ProcessFilesService {

    @Autowired
    private OutboundKeyValueDatabaseAdapter creditCardAdapter;

    @Autowired
    private OutboundKeyValueDatabaseAdapter extractAdapter;

    public void processFiles(List<Transaction> transactions, String userId, String type) {
        // 0 - Ordenar os itens por data
        List<Transaction> orderedItems = orderItemsByDate(transactions);

        // 1 - Buscar o primeiro item salvo no Redis
        Transaction firstItem = getFirstItemKeyValueDatabase(userId, type);

        if (firstItem == null) {
            printNewItems(orderedItems);

            // Se houver itens, salvamos o primeiro item (mais recente)
            if (orderedItems.size() > 0) {
                firstItem = orderedItems.get(0);
                if (type.equals("credit")) {
                    creditCardAdapter.saveLastItem(userId, firstItem, type);
                } else {
                    extractAdapter.saveLastItem(userId, firstItem, type);
                }
            }
            return;
        }

        // 2 - Procurar nos itens recebidos se há novos itens após o último item salvo
        int foundIndex = lookForFirstItemInArray(orderedItems, firstItem);

        // 2.1 - Se não encontrar o último item, considerar todos como novos
        if (foundIndex == -1) {
            // Se não encontrar o último item, considerar todos como novos
            printNewItems(orderedItems);
        } else {
            // Se encontrar o último item, considerar apenas os novos itens após ele
            printNewItems(orderedItems.subList(0, foundIndex));
        }

        // 4 - Salvar o último item no Redis
        if (type.equals("credit")) {
            creditCardAdapter.saveLastItem(userId, firstItem, type);
        } else {
            extractAdapter.saveLastItem(userId, firstItem, type);
        }
    }

    // order the items by date (descending - most recent first)
    private List<Transaction> orderItemsByDate(List<Transaction> items) {
        items.sort(Comparator.comparing(Transaction::getDate).reversed());
        return items;
    }

    private Transaction getLastItemKeyValueDatabase(String userId, String type) {
        if (type.equals("credit")) {
            return creditCardAdapter.getLastItem(userId, type);
        }
        return extractAdapter.getLastItem(userId, type);
    }

    // function to look for the first item in the array of items (where the fields
    // are equal)
    private int lookForFirstItemInArray(List<Transaction> items, Transaction lastItem) {
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