package core;

public class Address {
  public final String address;

  public Address(String value) {
    if (value != null && value.matches("^[a-fA-F0-9]{64}$")) {
      this.address = value;
    } else {
      throw new IllegalArgumentException("Invalid address format");
    }
  }

  public String getAddress() {
    return address;
  }
}