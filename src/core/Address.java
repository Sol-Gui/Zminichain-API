package core;

public class Address implements Comparable<Address>, BytesSerializable {
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

  @Override
  public byte[] getBytes() {
    return address.getBytes();
  }

  @Override
  public int compareTo(Address other) {
    if (other == null) {
      return 1;
    }
    return this.address.compareTo(other.address);
  }
}