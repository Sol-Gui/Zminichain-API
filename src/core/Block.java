package core;

import java.util.List;
import java.util.HashMap;

public class Block {
  protected final String hash;
  protected final String previousHash;
  protected final int index;
  protected List<Transaction> transactions;
  protected HashMap<Address, Account> worldState;
  protected long timestamp;
  protected int nonce;

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
}
