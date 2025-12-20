package core;

import java.nio.ByteBuffer;

public class Transaction {
  private final long zesties; // 10^6 (10^6Zesties = 1 Zecoin)
  private final Account receiver;
  private final Account sender;
  private final long timestamp;
  private final String signature;
  private final String transactionHash;

  public Transaction(
    long zesties, Account receiver, Account sender, long timestamp,
    String signature, String transactionHash
  ) {
    this.zesties = zesties;
    this.receiver = receiver;
    this.sender = sender;
    this.timestamp = timestamp;
    this.signature = signature;
    this.transactionHash = transactionHash;
  }

  public void processTransaction() {
    long senderZesties = sender.getZesties();
    long receiverZesties = receiver.getZesties();
    if (senderZesties < zesties) {
      throw new IllegalStateException("Sender doesn't have enough Zecoins!");
    }
    
    sender.setZesties(senderZesties - zesties);
    receiver.setZesties(receiverZesties + zesties);
    //blockTransaction();
  }

  public byte[] getBytes() {
    byte[] zestiesBytes = ByteBuffer.allocate(8).putLong(zesties).array();
    byte[] timestampBytes = ByteBuffer.allocate(8).putLong(timestamp).array();
    byte[] receiverBytes = receiver.getBytes();
    byte[] senderBytes = receiver.getBytes();
    byte[] signatureBytes = signature.getBytes();
    byte[] transactionHashBytes = transactionHash.getBytes();

    return ByteBuffer.allocate(zestiesBytes.length + timestampBytes.length +
            receiverBytes.length + senderBytes.length + signatureBytes.length + transactionHashBytes.length)
        .putLong(zesties)
        .putLong(timestamp)
        .put(receiverBytes)
        .put(senderBytes)
        .put(signatureBytes)
        .put(transactionHashBytes)
        .array();
  }
}
