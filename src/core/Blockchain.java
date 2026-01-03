package core;
import utils.LinkedList;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Blockchain {
  public final LinkedList<Block> blockchain = new LinkedList<>();
  private final Map<Address, Account> ledger = new TreeMap<>();
  private Address genesisAddress;
  private Account genesisAccount;


  private final List<Transaction> genesisTransactions = new ArrayList<>();

  private Block createGenesisBlock() throws IOException, NoSuchAlgorithmException {

    Block genesisBlock = new Block(
        "0000000000000000000000000000000000000000000000000000000000000000", 0,
        genesisTransactions, ledger, System.currentTimeMillis(), 0
    );

    return genesisBlock;
  }

  public Blockchain(long mintValue) throws IOException, NoSuchAlgorithmException {
    createGenesis(mintValue);
  }

  public void addBlock(Block block) {
    blockchain.Insert(block);
  }

  private void createGenesis(long mintValue) throws IOException, NoSuchAlgorithmException {
    this.genesisAddress = new Address("GENESIS");
    this.genesisAccount = new Account(mintValue, genesisAddress);

    ledger.put(genesisAddress, genesisAccount);

    addBlock(createGenesisBlock());
  }

    public Address getGenesisAddress() {
      return this.genesisAddress;
    }

    public Account getGenesisAccount() {
      return this.genesisAccount;
    }
    
}
