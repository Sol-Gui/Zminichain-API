package tests;

import core.Account;
import core.Address;
import java.util.HashMap;
import java.util.List;

public class TestWorldState {
    public static void main(String[] args) {
        System.out.println("=== TESTE WORLD STATE ===\n");
        
        HashMap<Address, Account> lesta = new HashMap<>();
        Address ad1 = new Address("0000000000000000000000000000000000000000000000000000000000000001");

        List<Account> accounts = List.of(
            new Account(
                12_000_000,
                ad1
            ),
            new Account(
                18_500_000,
                new Address("0000000000000000000000000000000000000000000000000000000000000002")
            ),
            new Account(
                25_000_000,
                new Address("0000000000000000000000000000000000000000000000000000000000000003")
            ),
            new Account(
                42_000_000,
                new Address("0000000000000000000000000000000000000000000000000000000000000004")
            ),
            new Account(
                75_000_000,
                new Address("0000000000000000000000000000000000000000000000000000000000000005")
            )
        );

        // adiciona essas contas no worldState
        for (Account account : accounts) {
            lesta.put(account.getAddress(), account);
        }

        // mostra a conta por <endereço>
        System.out.println("Conta do endereço: " + ad1 + " é: " + lesta.get(ad1));

        // mostra todas as contas no worldState
        for (Address address : lesta.keySet()) {
            System.out.println("\nAccount: " + lesta.get(address) + "\nAddress: " + address);
        }
        
        System.out.println("\n✓ Teste World State passou!");
    }
}
