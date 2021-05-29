package structure;

public class Edge {
    Event from;
    Event to;
    int lb;
    int ub;

    public Edge(Event from, Event to, int lb, int ub) {
        this.from = from;
        this.to = to;
        this.lb = lb;
        this.ub = ub;
    }

    public Edge(Event from, Event to) {
        this.from = from;
        this.to = to;
    }

    public Event getFrom() {
        return from;
    }

    public void setFrom(Event from) {
        this.from = from;
    }

    public Event getTo() {
        return to;
    }

    public void setTo(Event to) {
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
