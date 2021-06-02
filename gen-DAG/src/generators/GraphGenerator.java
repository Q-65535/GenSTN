package generators;

import structure.Edge;
import structure.Event;
import structure.Literal;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GraphGenerator implements Generator {
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
    private int num_edge = 14;
    /**
     * 每个event的最大入边数量
     */
    private int num_maxInEdge = 999;
    /**
     * 每个event的最大出边数量
     */
    private int num_maxOutEdge = 999;
    /**
     * 边中上界或下界的取值范围的大小
     */
    private int boundRange = 3;
    /**
     * 边中的时序下界的最小值
     */
    private int min_lb = 4;
    /**
     * 边中的时序上界的最小值与时序下界的最大值的差值
     */
    private int diff_maxL_minU = 3;
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
    private int eventNumber = 0;

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
        this.eventNumber = 0;
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
    private Event genEvent(ArrayList<String> envGroup) {
        boolean value;
        int index;
        // the size of the selected set of environment variables
        int groupSize = envGroup.size();
        //索引，用来选择环境变量并构建一个event
        ArrayList<Integer> indexes = new ArrayList<>();
        //选择出指定数量的环境变量
        while (indexes.size() < num_literal) {
            index = rm.nextInt(groupSize);
            while (indexes.contains(index)) index = rm.nextInt(groupSize);
            indexes.add(index);
        }
        ArrayList<Literal> literals = new ArrayList<>();
        //给每个环境变量赋值
        for (int i : indexes) {
            value = rm.nextBoolean();
            Literal literal = new Literal(envGroup.get(i), value);
            literals.add(literal);
        }
        return new Event(literals, eventNumber++);
    }

    /**
     * 给定一组event，随机将它们相连形成DAG
     *
     * @param events  给定的一组event
     * @param edgeNum 指定的图中边的数量
     * @return 生成的DAG中的“根”event
     */
    private Event genGraph(ArrayList<Event> events, int edgeNum) {
        ArrayList<Edge> edgeSequence = new ArrayList<>();
        int fromIndex;
        int toIndex;
        int count = 0;
        //上界与下界，后续将为边中的时序上下界赋值
        int lb, ub;

        //打印出所给定的events的名字
        StringBuilder sbu = new StringBuilder();
        events.forEach(event -> sbu.append("    ").append(event.getName()).append(";\n"));
        System.out.println(sbu);

        //对第一个节点之后的每一个节点v都随机选择一个u进行相连（u在v之前），确保该图是一个连通图
        for (int i = 1; i < events.size(); i++) {
            //头节点
            Event to = events.get(i);
            ArrayList<Event> firstAvailableFroms = new ArrayList<>();
            for (Event from : events.subList(0, i)) {
                boolean satisfy = from.getOutEdges().size() < num_maxOutEdge;
                if (satisfy) {
                    firstAvailableFroms.add(from);
                }
            }

            if (firstAvailableFroms.size() > 0) {
                //随机选择之前的一个节点作为尾节点
                fromIndex = rm.nextInt(firstAvailableFroms.size());
                Event from = firstAvailableFroms.get(fromIndex);
                to.getFrom().add(from);
                from.getTo().add(to);

                //添加新生成的边
                lb = rm.nextInt(boundRange) + min_lb;
                ub = rm.nextInt(boundRange) + min_lb + boundRange + diff_maxL_minU;
                Edge edge = new Edge(from, to, lb, ub);
//                edgeSequence.add(edge);
                to.addInEdge(edge);
                from.addOutEdge(edge);
                //对尾节点向上递归地更新可达的节点
                from.updateReachable(to);
                //对头节点向下递归地更新可被达的节点
                to.updateBeReachable(from);
                count++;
                System.out.println(edge.getFrom().getName() + "->" + edge.getTo().getName() + ";");
            }
        }

        //用于记录可作为头节点的event，在接下来的while循环中会不断更新
        ArrayList<Event> availableTos = new ArrayList<>(events.subList(2, events.size()));
        for (Event availableTo : availableTos) {

        }
        // 如果边的数量没有达到edgNum, 添加更多的边
        outer:
        while (count < edgeNum) {
            //更新可作为头节点的event
            for (int i = 0; i < availableTos.size(); i++)
                //如果该节点的入边数量超过上限，则将其移除
                if (availableTos.get(i).getInEdges().size() >= num_maxInEdge) availableTos.remove(i);
            StringBuilder sb = new StringBuilder();
            //如果有可作为头节点的event
            if (availableTos.size() > 0) {
                availableTos.forEach(event -> sb.append("-").append(event.getName()));
                System.out.println("the availableTos: " + sb);
                //在availableTos中随机选择一个event作为头节点
                toIndex = rm.nextInt(availableTos.size());
                Event to = availableTos.get(toIndex);
                System.out.println("to node selected: " + to.getName());
                //用于记录可作为尾节点的event
                ArrayList<Event> secondAvailableFroms = new ArrayList<>();
//                for (Event from : events.subList(0, events.indexOf(to)))
                for (Event from : events) {
                    Event.checkFromTo(from, to, num_maxOutEdge);
                    boolean satisfy2 = !(to.equals(from)//检查是否相同
                            || to.getFrom().contains(from)//检查是否已经相连
                            || from.getReachableEvents().contains(to)//检查是否已经可达
                            || to.getReachableEvents().contains(from)//检查是否已经可达
                            || Event.groupDirectReachable(from, to)
                            || from.getOutEdges().size() >= num_maxOutEdge);//检查边的数量是否满足要求
                    if (satisfy2) {
                        System.out.println("this from node: " + from.getName() + " is applicable!!!!!!");
                        secondAvailableFroms.add(from);
                    } else System.out.println("this from node: " + from.getName() + " is not applicable");
                }
                //对于选定的头节点，如果有可作为尾节点的event，则将其相连
                if (secondAvailableFroms.size() > 0) {
                    System.out.println("available froms is not empty!");
                    fromIndex = rm.nextInt(secondAvailableFroms.size());
                    Event from = secondAvailableFroms.get(fromIndex);
                    to.getFrom().add(from);
                    from.getTo().add(to);

                    lb = rm.nextInt(boundRange) + min_lb;
                    ub = rm.nextInt(boundRange) + min_lb + boundRange + diff_maxL_minU;
                    Edge edge = new Edge(from, to, lb, ub);

                    to.addInEdge(edge);
                    from.addOutEdge(edge);
                    System.out.println("connect two nodes: " + edge.getFrom().getName() + "--->" + edge.getTo().getName());
                    System.out.println("lower bound: " + edge.getLb() + "----upper bound: " + edge.getUb());
                    //对尾节点递归地更新可达节点
                    from.updateReachable(to);
                    //对头节点向下递归地更新可被达的节点
                    to.updateBeReachable(from);
                    count++;
                    //否则将该选中的头节从availableTos中移除
                } else {
                    availableTos.remove(toIndex);
                    continue outer;
                }
                //如果availableTos为空，即没有可作为头节点的event
            } else {
                System.out.println("no event to be selected as head node! Clean up and try again to generate graph!");
                ConnCleanUp(events);
                return genGraph(events, edgeNum);
            }


        }

        return events.get(0);
    }

    /**
     * 对一组event，清除它们之间的连接关系
     *
     * @param events 给定的一组event
     */
    public void ConnCleanUp(ArrayList<Event> events) {
        for (Event event : events) {
            event.getReachableEvents().clear();
            event.getOutEdges().clear();
            event.getInEdges().clear();
            event.getTo().clear();
            event.getFrom().clear();
        }
    }

    /**
     * 给定一个DAG，生成其对应的UML图
     *
     * @param graphName UML图的名字
     * @param DAG       给定的DAG
     * @return
     */
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
