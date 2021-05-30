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
     * 可达的其它event
     */
    private ArrayList<Event> reachableEvents = new ArrayList<>();
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

    public void addInEdge(Edge edge) {
        this.inEdges.add(edge);
    }

    public void addOutEdge(Edge edge) {
        this.outEdges.add(edge);
    }

    /**
     * 递归地更新可达的event
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
     * 递归地检查该节点或其predecessors是否向指定的event直接相连(如果有的话，添加的这条边是多余边)
     *
     * @param event 指定的event
     * @return true，如果直接相连
     */
    public boolean upperLinkReachable(Event event) {
        if (!this.outEdges.isEmpty()) {
            for (Edge edge : outEdges) {
                if (edge.to.equals(event)) {
                    return true;
                }
            }
        }
        if (!this.inEdges.isEmpty()) {
            for (Edge edge : inEdges) {
                if (edge.from.upperLinkReachable(event)) return true;
            }
        }
        return false;
    }

    /**
     * 检查指定的event是否向当前节点或当前节点的可达节点直接相连
     *
     * @param event 指定的event
     * @return true，如果相连
     */
    public boolean lowerLinkReachable(Event event) {
        if (this.getReachableEvents().size() > 0) {
            if (event.getOutEdges().size() > 0)
                for (Edge outEdge : event.getOutEdges()) {
                    if (this.getReachableEvents().contains(outEdge.to)) return true;
                }
        }
        return false;
    }

    public static void checkFromTo(Event from, Event to, int num_maxOutEdge) {
        System.out.println("----------------------------------------------");
        System.out.println("checking two nodes: "+from.getName()+"->"+to.getName());
        //检查是否已经相连
        if (to.getFrom().contains(from)) System.out.println("two events are already connected");
        //检查是否已经可达
        if (from.getReachableEvents().contains(to)) System.out.println("two events are reachable");
        if (from.upperLinkReachable(to)) System.out.println("upperLinkReachable");
        if (to.lowerLinkReachable(from)) System.out.println("lowerLinkReachable");
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
