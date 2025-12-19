package core;

public class Account {
  private long zesties;
  private final Address address;

  public Account(long zesties, Address address) {
    this.zesties = zesties;
    this.address = address;
  }

  public long getZesties() {
    return zesties;
  }

  public Address getAddress() {
    return address;
  }

  public void setZesties(long newZesties) {
    zesties = newZesties;
  }
}
