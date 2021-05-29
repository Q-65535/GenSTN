package generators;

import structure.Edge;
import structure.Event;
import structure.Literal;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GraphGenerator implements Generator{
    /**
     * 生成的DAG的数量
     */
    private int num_graph = 3;
    /**
     * 每个DAG中event的数量（对应于节点的数量）
     */
    private int num_event = 8;
    /**
     * 每个event中literal的数量
     */
    private int num_literal = 3;
    /**
     * 每个DAG中边的数量
     */
    private int num_edge = 10;
//    /**
//     * 对event进行分组，属于同一个DAG的在一组
//     */
//    ArrayList<ArrayList<Event>> eventGroups = new ArrayList<>();
    /**
     * 所有可用于生成动态环境模型的环境变量
     */
    ArrayList<String> envVariables;
    /**
     * 环境变量组，一组环境变量对应于一个DAG
     */
    ArrayList<ArrayList<String>> envGroups = new ArrayList<>();
    private Random rm = new Random();
    /**
     * event标号
     */
    private int eventNumber=0;

    public GraphGenerator() {

    }

    public GraphGenerator(ArrayList<String> envs) {
        this.envVariables = envs;
    }

    public ArrayList<ArrayList<Event>> generate() {
        ArrayList<Event> eventGroup;
        ArrayList<ArrayList<Event>> eventGroups = new ArrayList<>();
        ArrayList<ArrayList<String>> envGroups;
        envGroups = divide_envVariables(envVariables, num_graph);
        for (ArrayList<String> envGroup : envGroups) {
            eventGroup = genEvents(envGroup, this.num_event);
            genGraph(eventGroup, num_edge);
            eventGroups.add(eventGroup);
        }

        return eventGroups;
    }

    /**
     * 根据给定的参数，随机生成多组event与DAG
     *
     * @return
     */
    public ArrayList<ArrayList<Event>> generate(int num_graph, int num_event, int num_literal, int num_edge) {
        this.num_graph = num_graph;
        this.num_event = num_event;
        this.num_literal = num_literal;
        this.num_edge = num_edge;
        this.eventNumber=0;
        return generate();
    }

    /**
     * 给定一系列环境变量，将它们分为若干组
     *
     * @param envVariables the set of environment variables
     * @param number       number of groups
     * @return the separated groups
     */
    public ArrayList<ArrayList<String>> divide_envVariables(ArrayList<String> envVariables, int number) {
        //每一个组中环境变量的个数
        ArrayList<ArrayList<String>> envGroups = new ArrayList<>();
        int varNum = envVariables.size() / number;
        //余数
        int remainderNum = envVariables.size() % number;
        int index = 0;
        //对每一组进行添加环境变量
        for (int i = 0; i < number; i++) {
            ArrayList<String> subEnvVariables = new ArrayList<>();
            for (int j = 0; j < varNum; j++) {
                subEnvVariables.add(envVariables.get(index++));
            }
//                    (ArrayList<String>) envVariables.subList(index, index + varNum);
            envGroups.add(subEnvVariables);
        }
        //将剩余的没有被分配的环境变量进行分配
        for (int i = 0; i < remainderNum; i++) {
            envGroups.get(i).add(envVariables.get(index++));
        }
        this.envGroups = envGroups;
        return envGroups;
    }

    /**
     * 给定一组环境变量，生成多个event
     *
     * @param envGroup       给定的一组环境变量
     * @param numberOfEvents 需要生成的event的数量
     * @return 一组event
     */

    public ArrayList<Event> genEvents(ArrayList<String> envGroup, int numberOfEvents) {
        ArrayList<Event> events = new ArrayList<>();
        while (events.size() < numberOfEvents) {
            Event event = genEvent(envGroup);
            //make sure no duplicated events
            while (events.contains(event)) event = genEvent(envGroup);
            events.add(event);
        }
        return events;
    }


    /**
     * 给定环境变量，生成一个event
     *
     * @param envGroup 给定的一组环境变量
     * @return 随机生成的一个event
     */
//    public Event genEvent(int envGroupIndex) {
//        boolean value;
//        // the size of the selected set of environment variables
//        int groupSize = envGroups.get(envGroupIndex).size();
//        ArrayList<Integer> indexes = new ArrayList<>();
//        for (int i = 0; i < num_literal; i++) {
//            indexes.add(rm.nextInt(groupSize));
//        }
//        ArrayList<Literal> literals = new ArrayList<>();
//        for (int i : indexes) {
//            value = rm.nextBoolean();
//            Literal literal = new Literal(envVariables.get(i), value);
//            literals.add(literal);
//        }
//        return new Event(literals, eventNumber++);
//    }
    private Event genEvent(ArrayList<String> envGroup) {
        boolean value;
        int index;
        // the size of the selected set of environment variables
        int groupSize = envGroup.size();
        //索引，用来选择环境变量并构建一个event
        ArrayList<Integer> indexes = new ArrayList<>();
        while (indexes.size() < num_literal) {
            index = rm.nextInt(groupSize);
            while (indexes.contains(index)) index = rm.nextInt(groupSize);
            indexes.add(index);
        }
        ArrayList<Literal> literals = new ArrayList<>();
        for (int i : indexes) {
            value = rm.nextBoolean();
            Literal literal = new Literal(envGroup.get(i), value);
            literals.add(literal);
        }
        return new Event(literals, eventNumber++);
    }

    private Event genGraph(ArrayList<Event> events, int edgeNum) {
        int fromIndex;
        int toIndex;
        int count = 0;
        //对第一个节点之后的每一个节点v都随机选择一个u进行相连（u在v之前），确保改图是一个连通图
        for (int i = 1; i < events.size(); i++) {
            //头节点
            Event to = events.get(i);
            //随机选择之前的一个节点作为尾节点
            fromIndex = rm.nextInt(i);
            Event from = events.get(fromIndex);
//            //若俩节点已经是可达的，则继续搜索
//            while (from.getReachableEvents().contains(to)) {
//                fromIndex = rm.nextInt(i);
//                from = events.get(fromIndex);
//            }
            to.getFrom().add(from);
            from.getTo().add(to);
            //添加新生成的边
            Edge edge = new Edge(from, to);
            to.addInEdge(edge);
            from.addOutEdge(edge);
            //对尾节点递归地更新可达节点
            from.updateReachable(to);
            count++;
        }
        // 如果边的数量没有达到edgNum, 添加更多的边
        outer:
        while (count < edgeNum) {
            //随机选择一个节点作为头节点
            toIndex = rm.nextInt(events.size());
            while (toIndex == 0) toIndex = rm.nextInt(events.size());
            Event to = events.get(toIndex);
            //记录搜索尾节点的次数（确定头节点后，随机搜索可用的尾节点）
            int searchCount = 0;
            fromIndex = rm.nextInt(toIndex);
            Event from = events.get(fromIndex);
            //如果这两个节点已经相连或者是可达或添加该边后会造成多余边，则继续搜索
            while (to.getFrom().contains(from)||from.getReachableEvents().contains(to)||from.direcReachable(to)) {
                fromIndex = rm.nextInt(toIndex);
                from = events.get(fromIndex);
                searchCount++;
                //如果搜索次数过多，则放弃搜索，选择其它的节点作为头节点
                if (searchCount > 2 * toIndex) continue outer;
            }
            //将选出的尾节点与头节点相连
            to.getFrom().add(from);
            from.getTo().add(to);

            Edge edge = new Edge(from, to);
            to.addInEdge(edge);
            from.addOutEdge(edge);
            //对尾节点递归地更新可达节点
            from.updateReachable(to);
            count++;

        }
        return events.get(0);
    }

    /**
     * 给定一个DAG，生成其对应的UML图
     *
     * @param graphName UML图的名字
     * @param DAG       给定的DAG
     * @return
     */
    public static String convertToDot(String graphName, List<Event> DAG) {
        StringBuilder sb = new StringBuilder();
        sb.append("@startuml\n\n")
                .append("digraph ").append(graphName).append(" {\n");
        DAG.forEach(event -> sb.append("    ").append(event.getName()).append(";\n"));
        sb.append("\n");
        DAG.forEach(from -> from.getTo().forEach(to -> {
            sb.append("    ").append(from.getName()).append(" -> ").append(to.getName()).append(";\n");
        }));
        sb.append("}\n")
                .append("\n@enduml\n");
        return sb.toString();
    }

    public static String convertToDotE(String graphName, List<Event> DAG) {
        StringBuilder sb = new StringBuilder();
        sb.append("@startuml\n\n")
                .append("digraph ").append(graphName).append(" {\n");
        DAG.forEach(event -> sb.append("    ").append(event.getName()).append(";\n"));
        sb.append("\n");
        DAG.forEach(from -> from.getOutEdges().forEach(outEdge -> {
            sb.append("    ").append(from.getName()).append(" -> ").append(outEdge.getTo().getName()).append(";\n");
        }));
        sb.append("}\n")
                .append("\n@enduml\n");
        return sb.toString();
    }

    public static void graphWrite(String s) {
        FileWriter fw = null;
        try {
            fw = new FileWriter("graphView.txt");
            fw.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fw != null) fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
