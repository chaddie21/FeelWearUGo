package com.example.chadwick.feelwearugo;

/**
 * Created by Chadwick on 3/25/2015.
 */
public interface AStarHeuristic {
    public float getCost(UWIMap map, Node node, Node destination);
}
