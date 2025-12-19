package core;
import utils.LinkedList;

public class Blockchain {
  LinkedList<Block> blockchain = new LinkedList<Block>();

  public void addBlock(Block block) {
    blockchain.Insert(block);
  }
}
