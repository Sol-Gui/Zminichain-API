package tests;

import core.Account;
import core.Transaction;
import core.Address;
import java.text.DecimalFormat;

public class TestTransaction {
    public static void main(String[] args) throws Exception {
        System.out.println("=== TESTE TRANSACTION ===\n");
        
        DecimalFormat blockchainFormat = new DecimalFormat("#.################");

        // Create sample accounts
        Account sender = new Account(1_000_000, new Address("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
        Account receiver = new Account(0, new Address("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"));

        // Create a transaction
        Transaction transaction = new Transaction(
            500_000, // 0.5 Zecoins
            receiver,
            sender,
            System.currentTimeMillis(),
            "sampleSignature",
            "sampleTransactionHash"
        );

        // Process the transaction
        try {
            transaction.processTransaction();
            System.out.println("Transaction processed successfully!");
            System.out.println("Sender balance: " + blockchainFormat.format(sender.getZesties() / 1_000_000.0));
            System.out.println("Receiver balance: " + blockchainFormat.format(receiver.getZesties() / 1_000_000.0));
        } catch (IllegalStateException e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }
        
        System.out.println("\n✓ Teste Transaction passou!");
    }
}
