package game.chernousovaya.checkers.model;

import java.util.ArrayList;

public class Tree {

    private ArrayList<Node> nodes = new ArrayList<>();

    public Tree(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    public Tree() {
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    public ArrayList<Node> getChildrens(int idxParent){
        ArrayList<Node> childrens = new ArrayList<>();
        for (int i = 0; i < nodes.size() ; i++) {
            if(nodes.get(i).getIdxParent() == idxParent)
                childrens.add(nodes.get(i));
        }
        return childrens;
    }

    public void addChildren(PairCell move, Board board, int idxParent){
         nodes.add(new Node(move, board, idxParent));
    }

    public static class Node {
        private PairCell move;
        private Board board;
        private int idxParent;

        public PairCell getMove() {
            return move;
        }

        public void setMove(PairCell move) {
            this.move = move;
        }

        public Board getBoard() {
            return board;
        }

        public void setBoard(Board board) {
            this.board = board;
        }

        public Node(PairCell move, Board board, int idxParent) {
            this.move = move;
            this.board = new Board(board);
            this.idxParent = idxParent;
        }

        public int getIdxParent() {
           return idxParent;
       }

       public void setIdxParent(int idxParent) {
           this.idxParent = idxParent;
       }
    }
}
