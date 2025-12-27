package core;

/**
 * Interface para objetos que podem ser serializados em bytes
 * para cálculo de hash ou persistência.
 */
public interface BytesSerializable {
  byte[] getBytes();
}

