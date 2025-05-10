package dev.thallyssonklein.financial_system.domain.bank.service;

import dev.thallyssonklein.financial_system.domain.bank.entity.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcessFilesService {

    public void processFiles(List<Transaction> transactions, String userId, String type) {
        // Implementação do processamento dos arquivos
        // Aqui você pode adicionar a lógica específica para processar as transações
        // Por exemplo, salvar no banco de dados, validar os dados, etc.
    }
}