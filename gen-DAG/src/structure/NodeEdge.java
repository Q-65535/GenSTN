package structure;

public class NodeEdge {
    /**
     * 该边的尾节点
     */
    Node from;
    /**
     * 改边的头节点
     */
    Node to;
    /**
     * from与to之间时序距离的下界
     */
    int lb;
    /**
     * from与to之间时序距离的上界
     */
    int ub;

    public NodeEdge(Node from, Node to, int lb, int ub) {
        this.from = from;
        this.to = to;
        this.lb = lb;
        this.ub = ub;
    }
    public NodeEdge(Node from, Node to) {
        this.from = from;
        this.to = to;
    }

    public Node getFrom() {
        return from;
    }

    public void setFrom(Node from) {
        this.from = from;
    }

    public Node getTo() {
        return to;
    }

    public void setTo(Node to) {
        this.to = to;
    }

    public int getLb() {
        return lb;
    }

    public void setLb(int lb) {
        this.lb = lb;
    }

    public int getUb() {
        return ub;
    }

    public void setUb(int ub) {
        this.ub = ub;
    }
}
