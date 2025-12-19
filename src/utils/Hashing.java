package utils;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashing {
  
  public static byte[] sha256StringHash(String input) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    return md.digest(input.getBytes(StandardCharsets.UTF_8));
  }

  public static byte[] sha256BytesHash(byte[] input) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    return md.digest(input);
  }

  public static String toHexString(byte[] hash) {
    StringBuilder hex = new StringBuilder(hash.length * 2);
    for (byte b : hash) {
        hex.append(String.format("%02x", b));
    }
    return hex.toString();
  }
}