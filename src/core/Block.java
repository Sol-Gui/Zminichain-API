package core;

import java.util.List;
import java.util.HashMap;
import utils.Hashing;

// Falta validar o Hash, no caso, verificar se é realmente do tipo Hash
public class Block {
  private final String hash;
  private final String previousHash;
  private final int index;
  private List<Transaction> transactions;
  private HashMap<Address, Account> worldState;
  private long timestamp;
  private int nonce;

  public Block(String hash, String previousHash, int index, 
    List<Transaction> transactions, HashMap<Address, Account> worldState, 
    long timestamp, int nonce
  ) {
    this.hash = hash;
    this.previousHash = previousHash;
    this.index = index;
    this.transactions = transactions;
    this.worldState = worldState;
    this.timestamp = timestamp;
    this.nonce = nonce;
  }

  public String getHash() {
    return hash;
  }

  public String getPreviousHash() {
    return previousHash;
  }

  public int getIndex() {
    return index;
  }

  public List<Transaction> getTransactions() {
    return transactions;
  }

  public HashMap<Address, Account> getWorldState() {
    return worldState;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public int getNonce() {
    return nonce;
  }
}
