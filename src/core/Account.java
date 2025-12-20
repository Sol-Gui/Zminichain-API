package core;

import java.nio.ByteBuffer;

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

  public byte[] getBytes() {
    byte[] zestiesBytes = ByteBuffer.allocate(8).putLong(zesties).array();
    byte[] addressBytes = address.getBytes();

    byte[] result = new byte[addressBytes.length + zestiesBytes.length];
    System.arraycopy(zestiesBytes, 0, result, 0, zestiesBytes.length);
    System.arraycopy(addressBytes, 0, result, zestiesBytes.length, addressBytes.length);
    return result;
  }
}
