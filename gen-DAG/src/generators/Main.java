package generators;

import structure.Event;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        //用于存储环境变量
        ArrayList<String> stringArr = new ArrayList<>();
        String str="abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMN";
        for (int i = 0; i < str.length(); i++) {
            stringArr.add(str.substring(i,i+1));
        }
        System.out.println(stringArr);
        //根据给定的一组环境变量(stringArr)获得一个生成器
        GraphGenerator gen = new GraphGenerator(stringArr);
        ArrayList<ArrayList<Event>>eventGroups=gen.generate();
        ArrayList<Event> eventGroup=eventGroups.get(0);
        //打印分组后的所有变量
        System.out.println(gen.envGroups);
        //打印一个event组中每个event包含的所有literal
        for (Event event : eventGroup) {
            System.out.println(event.getLiterals());
        }
        //转化为uml文件，方便可视化
        String UMLString=GraphGenerator.convertToDotE("graph1", eventGroup);
//        System.out.println(UMLString);
        GraphGenerator.graphWrite(UMLString);

    }
}
