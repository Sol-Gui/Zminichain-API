package core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import utils.Hashing;

// Falta validar o Hash, no caso, verificar se é realmente do tipo Hash

// Os blocos são mutáveis, eles devem SIM ser mutáveis porém apenas antes de entrarem
// para a blockchain, arrumar isso ou com um verificador ou com uma classe intermediária
public class Block {
  private final String hash;
  private final String previousHash;
  private final int index;
  private final List<Transaction> transactions;
  private final Map<Address, Account> worldState;
  private final long timestamp;
  private final int nonce;

  public Block(String previousHash, int index,
    List<Transaction> transactions, Map<Address, Account> worldState,
    long timestamp, int nonce
  ) throws IOException, NoSuchAlgorithmException {
    this.previousHash = previousHash;
    this.index = index;
    this.transactions = transactions;
    this.worldState = new TreeMap<>(worldState);
    this.timestamp = timestamp;
    this.nonce = nonce;

    this.hash = calculateHash();
  }

  public String calculateHash() throws IOException, NoSuchAlgorithmException {
    ByteArrayOutputStream byteArray = new ByteArrayOutputStream();

    for (Transaction transaction : transactions) {
      byteArray.write(transaction.getBytes());
    }

    for (Account account : worldState.values()) {
      byteArray.write(account.getBytes());
    }

    byteArray.write(ByteBuffer.allocate(8).putLong(timestamp).array());
    byteArray.write(ByteBuffer.allocate(4).putInt(nonce).array());
    byteArray.write(previousHash.getBytes());
    byteArray.write(ByteBuffer.allocate(4).putInt(index).array());

    byte[] bytes = byteArray.toByteArray();
    byte[] byteHash = Hashing.sha256BytesHash(bytes);
    String hash = Hashing.toHexString(byteHash);
    return hash;
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

  public Map<Address, Account> getWorldState() {
    return worldState;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public int getNonce() {
    return nonce;
  }
}
