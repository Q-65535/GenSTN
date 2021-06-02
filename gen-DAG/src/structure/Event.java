package structure;

import java.util.ArrayList;

public class Event {
    /**
     * event中对变量的赋值
     */
    private ArrayList<Literal> literals;
    /**
     * event名称标号
     */
    private int name;
    /**
     * 以改节点为尾节点的边
     */
    private ArrayList<Edge> outEdges = new ArrayList<>();
    /**
     * 以改节点为头节点的边
     */
    private ArrayList<Edge> inEdges = new ArrayList<>();
    /**
     * 该event可达的其它event
     */
    private ArrayList<Event> reachableEvents = new ArrayList<>();
    /**
     * 可达该event的其它event
     */
    private ArrayList<Event> beReachableEvents = new ArrayList<>();
    private ArrayList<Event> from = new ArrayList<>();
    private ArrayList<Event> to = new ArrayList<>();

    public Event(ArrayList<Literal> literals, int name) {
        this.literals = literals;
        this.name = name;
    }


    public Event(int name) {
        this.name = name;
    }

    public int getName() {
        return name;
    }

    public ArrayList<Event> getTo() {
        return to;
    }

    public ArrayList<Event> getFrom() {
        return from;
    }

    public void setName(int name) {
        this.name = name;
    }

    public void setTo(ArrayList<Event> to) {
        this.to = to;
    }

    public void setFrom(ArrayList<Event> from) {
        this.from = from;
    }

    public ArrayList<Literal> getLiterals() {
        return this.literals;
    }

    public ArrayList<Edge> getOutEdges() {
        return outEdges;
    }

    public ArrayList<Edge> getInEdges() {
        return inEdges;
    }

    public ArrayList<Event> getReachableEvents() {
        return reachableEvents;
    }

    public ArrayList<Event> getBeReachableEvents() {
        return beReachableEvents;
    }

    public void addInEdge(Edge edge) {
        this.inEdges.add(edge);
    }

    public void addOutEdge(Edge edge) {
        this.outEdges.add(edge);
    }

    /**
     * 递归地更新可达的event,边的方向为：this--->e
     *
     * @param e 对于该event新增的可达event
     */
    public void updateReachable(Event e) {
        //如果e原来不可达，则添加e到可达event集合中
        if (!this.reachableEvents.contains(e)) {
            this.reachableEvents.add(e);
            this.reachableEvents.addAll(e.reachableEvents);
        }
        //对每一条入边递归地更新
        if (!inEdges.isEmpty()) {
            for (Edge edge : inEdges) {
                edge.from.updateReachable(e);

            }
        }
    }

    /**
     * 递归地更新可达的event,边的方向为：e--->this
     *
     * @param e 对于该event新增的可被达event
     */
    public void updateBeReachable(Event e) {
        //如果e原来不可达，则添加e到可达event集合中
        if (!this.beReachableEvents.contains(e)) {
            this.beReachableEvents.add(e);
            this.beReachableEvents.addAll(e.beReachableEvents);
        }
        //对每一条入边递归地更新
        if (!outEdges.isEmpty()) {
            for (Edge edge : outEdges) {
                edge.to.updateBeReachable(e);
            }
        }
    }



    /**
     * 给定两个event，from和to，检查from和其上层节点中是否有某个节点与to及其下层节点中的一个节点直接相连
     *
     * @param from 给定的from
     * @param to   给定的to
     * @return ture, 如果有相连的节点
     */
    public static boolean groupDirectReachable(Event from, Event to) {
        ArrayList<Event> fromGroup = new ArrayList<>(from.beReachableEvents);
        fromGroup.add(from);
        ArrayList<Event> toGroup = new ArrayList<>(to.reachableEvents);
        toGroup.add(to);
        //对每一个fromGroup中的节点遍历，检查其是否和toGroup中的任意节点相连
        for (Event f : fromGroup) {
            for (Event t : toGroup) {
                for (Edge e : f.getOutEdges()) {
                    if (e.to.equals(t)) return true;
                }
                for (Edge e : f.getInEdges()) {
                    if (e.from.equals(t)) return true;
                }
            }
        }

        return false;
    }

    public static void checkFromTo(Event from, Event to, int num_maxOutEdge) {
        System.out.println("----------------------------------------------");
        System.out.println("checking two nodes: " + from.getName() + "->" + to.getName());
        //检查是否已经相连
        if (to.getFrom().contains(from)) System.out.println("two events are already connected");
        //检查是否已经可达
        if (from.getReachableEvents().contains(to)) System.out.println("two events are reachable");
        if (from.getOutEdges().size() >= num_maxOutEdge) System.out.println("exceed maximum number of outEdges");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != this.getClass()) return false;
        Event event = (Event) o;
        if (this.literals.size() != event.literals.size()) return false;
        for (Literal l : literals) {
            if (!event.literals.contains(l)) return false;
        }
        return true;
    }
}
