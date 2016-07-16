package com.example.chadwick.feelwearugo;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by Chadwick on 3/25/2015.
 */
public class Path {

    private ArrayList<Node> steps;



    public Path(){

    }

    public int pathLength(){
        return steps.size();
    }

    // TODO analyse whether or not STEP needs to be a cost or just use nodes
    public Node getStep(int index){
        return (Node) steps.get(index);
    }

    public Location getStepLocation(int index){
        return steps.get(index).getLocation();
    }

    // appends nodes to the path list
    public void appendStep( Node node){
        steps.add(node);
    }

    // adds node to the top of the path list
    public void prependStep (Node node){
        steps.add(0, node);
    }

    // check it this path has the node that is being sought for.
    public boolean contains(Node node){
        return steps.contains(node);
    }

    public void removeStep(int i){
        steps.remove(i);
    }

}
