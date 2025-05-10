package dev.thallyssonklein.financial_system.adapter.inbound;

import dev.thallyssonklein.financial_system.domain.bank.entity.Transaction;
import dev.thallyssonklein.financial_system.domain.bank.service.ProcessFilesService;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ReceiveUserExtractInboundAdapter {

    private final ProcessFilesService processFilesService;

    public ReceiveUserExtractInboundAdapter(ProcessFilesService processFilesService) {
        this.processFilesService = processFilesService;
    }

    public void processFiles(String userId, MultipartFile extractFile, MultipartFile creditCardFile)
            throws IOException {
        List<Transaction> extractItems = processExtractFile(extractFile);
        List<Transaction> creditCardItems = processCreditCardFile(creditCardFile);

        processFilesService.processFiles(extractItems, userId, "extract");
        processFilesService.processFiles(creditCardItems, userId, "credit");
    }

    private List<Transaction> processExtractFile(MultipartFile file) throws IOException {
        List<Transaction> items = new ArrayList<>();
        boolean headerFound = false;
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] record = line.split(";");

                if (record.length == 0 || (record.length == 1 && record[0].isEmpty())) {
                    continue;
                }

                if (!headerFound) {
                    if (record.length > 0 && record[0].equals("Data Lançamento")) {
                        headerFound = true;
                        continue;
                    }
                    continue;
                }

                while (record.length < 4) {
                    record = new String[record.length + 1];
                }

                Transaction transaction = new Transaction();
                transaction.setDate(record[0]);
                transaction.setDescription(record[1]);
                transaction.setValue(record[2]);
                items.add(transaction);
            }
        }

        if (items.isEmpty()) {
            throw new IOException("Nenhum item encontrado no arquivo após processar " + lineNumber + " linhas");
        }

        return items;
    }

    private List<Transaction> processCreditCardFile(MultipartFile file) throws IOException {
        List<Transaction> items = new ArrayList<>();
        boolean headerFound = false;
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] record = line.split(",");

                if (record.length == 0 || (record.length == 1 && record[0].isEmpty())) {
                    continue;
                }

                if (!headerFound) {
                    if (record.length > 0 && record[0].contains("Descricao")) {
                        headerFound = true;
                        continue;
                    }
                    continue;
                }

                if (record[0].equals("Cartão Principal") || record[0].equals("Vencimento")
                        || record[0].equals("Total")) {
                    continue;
                }

                while (record.length < 7) {
                    record = new String[record.length + 1];
                }

                for (int i = 0; i < record.length; i++) {
                    record[i] = record[i].trim();
                }

                String valor = record[6];
                if (valor.startsWith("-R$ ")) {
                    valor = "-" + valor.substring(4);
                } else if (valor.startsWith("R$ ")) {
                    valor = valor.substring(3);
                }

                for (int i = 0; i < record.length; i++) {
                    record[i] = record[i].replace("\"", "");
                }

                if (record[1].isEmpty() || record[3].isEmpty() || record[6].isEmpty()) {
                    continue;
                }

                Transaction transaction = new Transaction();
                transaction.setDescription(record[3]);
                transaction.setDate(record[1]);
                transaction.setValue(valor);
                items.add(transaction);
            }
        }

        if (items.isEmpty()) {
            throw new IOException("Nenhum item encontrado no arquivo após processar " + lineNumber + " linhas");
        }

        return items;
    }
}
