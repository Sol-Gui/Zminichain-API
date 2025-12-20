package core;
import utils.LinkedList;
import utils.Hashing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Blockchain {
  public LinkedList<Block> blockchain = new LinkedList<Block>();
  private final HashMap<Address, Account> ledger = new HashMap<>();
  private Address genesisAddress;
  private Account genesisAccount;


  private final List<Transaction> genesisTransactions = new ArrayList<>();

  private Block createGenesisBlock() throws IOException, NoSuchAlgorithmException {

    Block genesisBlock = calculateBlock(
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

  public Block calculateBlock(String previousHash, int index,
                                    List<Transaction> transactions, HashMap<Address, Account> ledger,
                                    long timestamp, int nonce
  ) throws IOException, NoSuchAlgorithmException {
      ByteArrayOutputStream byteArray = new ByteArrayOutputStream();

      for (Transaction transaction : transactions) {
        byteArray.write(transaction.getBytes());
      }

      for (Account account : ledger.values()) {
        byteArray.write(account.getBytes());
      }
      
      byteArray.write(ByteBuffer.allocate(8).putLong(timestamp).array());
      byteArray.write(ByteBuffer.allocate(4).putInt(nonce).array());
      byteArray.write(previousHash.getBytes());
      byteArray.write(ByteBuffer.allocate(4).putInt(index).array());

      byte[] bytes = byteArray.toByteArray();
      byte[] byteHash = Hashing.sha256BytesHash(bytes);
      String hash = Hashing.toHexString(byteHash);
      Block newBlock = new Block(hash, previousHash, index, transactions, ledger, timestamp, nonce);

      return newBlock;

    }

    public Address getGenesisAddress() {
      return this.genesisAddress;
    }

    public Account getGenesisAccount() {
      return this.genesisAccount;
    }
    
}
