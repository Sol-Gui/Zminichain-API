package tests;

import utils.LinkedList;

public class TestLinkedList {
  public static void main(String[] args) {
    System.out.println("=== TESTE LINKEDLIST ===\n");

    LinkedList<String> lista = new LinkedList<>();
    lista.Insert("joão");
    lista.Insert("bedim");
    lista.InsertAt("gui", 0);

    System.out.println("Posição 0: " + lista.Get(0));
    System.out.println("Posição 1: " + lista.Get(1));
    System.out.println("Posição 2: " + lista.Get(2));

    System.out.println("\n✓ Teste LinkedList passou!");
  }
}
