package generators;

import structure.Event;

import java.util.ArrayList;

public interface Generator {
    ArrayList<ArrayList<Event>> generate(int num_graph, int num_event, int num_literal, int num_edge);
}
