package com.example.chadwick.feelwearugo;

/**
 * Created by Chadwick on 3/25/2015.
 */
public class ClosestHeuristic implements AStarHeuristic {


    @Override
    public float getCost(UWIMap map, Node node, Node destination) {
         return node.distanceTo(destination);
    }
}
