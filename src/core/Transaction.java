package core;

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
}
