package core;

public class Address {
  public final String address;

  public Address(String value) {
    if (value != null && (value.matches("^[a-fA-F0-9]{64}$") || "GENESIS".equals(value))) {
      this.address = value;
    } else {
      throw new IllegalArgumentException("Invalid address format");
    }
  }

  public String getAddress() {
    return address;
  }

  public byte[] getBytes() {
    return address.getBytes();
  }
}