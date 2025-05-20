package dev.thallyssonklein.financial_system.port.inbound.http;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import dev.thallyssonklein.financial_system.adapter.inbound.ReceiveUserExtractInboundAdapter;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/v1/users")
public class ReceiveUserExtractController {

    @Autowired
    private ReceiveUserExtractInboundAdapter receiveUserExtractInboundAdapter;

    @PostMapping("/{userId}/files")
    public ResponseEntity<?> processFiles(
            @PathVariable("userId") String userId,
            @RequestParam("extract") MultipartFile extractFile,
            @RequestParam("creditCard") MultipartFile creditCardFile) {

        if (extractFile.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Arquivo de extrato é obrigatório");
        }

        if (creditCardFile.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Arquivo de fatura é obrigatório");
        }

        try {
            receiveUserExtractInboundAdapter.processFiles(userId, extractFile, creditCardFile);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}
