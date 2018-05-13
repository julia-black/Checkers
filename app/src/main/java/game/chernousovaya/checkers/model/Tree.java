package game.chernousovaya.checkers.model;

import java.util.ArrayList;
import java.util.Random;

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

    //Получить максимальный элемент на уровне level
    public Node getMaxInLevel(int level) {
        int maxEval = -100;
        ArrayList<Node> maxElements = new ArrayList<>();
        ArrayList<Node> nodeArrayList = getNodesByLevel(level);
        Node result = null;
        for (int i = 0; i < nodeArrayList.size(); i++) {
            if (nodeArrayList.get(i).getBoard().evaluationFunction(nodeArrayList.get(i).getMove().getmEndCell().getX(),
                    nodeArrayList.get(i).getMove().getmEndCell().getY(), 2) >= maxEval) {
                maxEval = nodeArrayList.get(i).getBoard().evaluationFunction(nodeArrayList.get(i).getMove().getmEndCell().getX(), nodeArrayList.get(i).getMove().getmEndCell().getY(), 2);
                result = nodeArrayList.get(i);
            }
        }
        for (int i = 0; i < nodeArrayList.size(); i++) {
            if (nodeArrayList.get(i).getEvalMinMax() == maxEval) {
                maxElements.add(nodeArrayList.get(i));
            }
        }
        //Если количество одинаковых элементов > 1, то выбираем случайным образом из них
        if (maxElements.size() > 1) {
            Random random = new Random();
            result = maxElements.get(random.nextInt(maxElements.size() - 1));
        }
        return result;
    }

    //Получить уровень узла
    public int getLevelNode(int idx) {
        int level = 1;
        if (idx < nodes.size() && idx != -1) {
            //Находим его предка
            int idxParent = nodes.get(idx).idxParent;
            while (idxParent > -1) {
                level++;
                //Получаем всех предков, пока не дойдем до верхнего (с предком -1)
                idxParent = nodes.get(idxParent).idxParent;
            }
        }
        return level;
    }

    //Получить все узлы дерева на уровне level
    public ArrayList<Node> getNodesByLevel(int level) {
        ArrayList<Node> arrayList = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getLevel() == level) {
                arrayList.add(nodes.get(i));
            }
        }
        return arrayList;
    }

    //Получить детей узла
    public ArrayList<Node> getChildrens(int idxParent) {
        ArrayList<Node> childrens = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getIdxParent() == idxParent)
                childrens.add(nodes.get(i));
        }
        return childrens;
    }

    public void addChildren(PairCell move, Board board, int idxParent, int level) {
        nodes.add(new Node(move, board, idxParent, level));
    }

    public synchronized void addChildren(PairCell move, Board board, int idxParent, int level, int evalMinMax) {
        nodes.add(new Node(move, board, idxParent, level, evalMinMax));
    }

    public static class Node {
        private PairCell move;
        private Board board;
        private int idxParent;
        private int level;
        private int evalMinMax;
        private int alpha;
        private int beta;

        public int getAlpha() {
            return alpha;
        }

        public void setAlpha(int alpha) {
            this.alpha = alpha;
        }

        public int getBeta() {
            return beta;
        }

        public void setBeta(int betta) {
            this.beta = betta;
        }

        public int getEvalMinMax() {
            return evalMinMax;
        }

        public void setEvalMinMax(int evalMinMax) {
            this.evalMinMax = evalMinMax;
            //Если это мин элемент
            if (level % 2 == 0) {
                this.beta = evalMinMax;
            } else {
                this.alpha = evalMinMax;
            }
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public Node(PairCell move, Board board, int idxParent, int level) {
            this.move = move;
            this.board = board;
            this.idxParent = idxParent;
            this.level = level;
            if (level % 2 == 0) {
                this.beta = evalMinMax;
            } else {
                this.alpha = evalMinMax;
            }
        }

        public Node(PairCell move, Board board, int idxParent, int level, int evalMinMax) {
            this.move = move;
            this.board = board;
            this.idxParent = idxParent;
            this.level = level;
            this.evalMinMax = evalMinMax;
            if (level % 2 == 0) {
                this.beta = evalMinMax;
            } else {
                this.alpha = evalMinMax;
            }
        }

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


        public int getIdxParent() {
            return idxParent;
        }

        public void setIdxParent(int idxParent) {
            this.idxParent = idxParent;
        }
    }
}
