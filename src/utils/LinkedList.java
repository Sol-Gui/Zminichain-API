package utils;

public class LinkedList<T> {
  private static class Node<T> {
    T value;
    Node<T> next;

    Node(T value) {
      this.value = value;
      this.next = null;
    }
  }

  private int size;
  private Node<T> head;

  public T Get(int index) {
    CheckIndexError(index);

    Node<T> current = head;
    for (int i = 0; i < index; i++) {
      current = current.next;
    }
    return current.value; 
  }

  public void Insert(T value) {
    Node<T> newNode = new Node<>(value);

    if (head == null) {
      head = newNode;
    } else {
      Node<T> current = head;
      while (current.next != null) {
        current = current.next;
      }
      current.next = newNode;
    }
    size++;
  }

  public void InsertAt(T value, int index) {
    CheckIndexError(index);

    if (index == 0) {
        Node<T> newNode = new Node<>(value);
        newNode.next = head;
        head = newNode;
    } else {
        Node<T> current = head;
        for (int i = 0; i < index - 1; i++) {
            current = current.next;
        }
        Node<T> newNode = new Node<>(value);
        newNode.next = current.next;
        current.next = newNode;
    }
    size++;
  }

  public void remove(int index) {
    CheckIndexError(index);

    if (index == 0) {
      head = head.next;
    } else {
      Node<T> current = head;
      for (int i = 0; i < index - 1; i++) {
        current = current.next;
      }
      current.next = current.next.next;
    }
    size--;
  }

  public int getSize() {
    return size;
  }

  public void CheckIndexError(int index) {
    if (index < 0 || index > size) {
      throw new IndexOutOfBoundsException("Index: " + index);
    }
  }
}