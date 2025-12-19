package core;

import java.util.HashMap;
import java.util.List;

public class Genesis extends Block {

  public Genesis(String hash, String previousHash, int index, List<Transaction> transactions,
      HashMap<Address, Account> worldState, long timestamp, int nonce) {
    super(hash, previousHash, index, transactions, worldState, timestamp, nonce);
  }

  public String returnHash() {
    return this.hash;
  }
}