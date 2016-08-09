package com.example.chadwick.feelwearugo;

/**
 * Created by Photonovation on 07/26/2016.
 */
public class Edge {
    public int edge_id;
    public String name;
    public int start_node_id;
    public int end_node_id;
    public double length;
    public double reverse_length;
    public boolean isRoad;
    public boolean hasPedestrian;
    public boolean hasStreetConnect;

    public Node startNode;

    public Node endNode;

    public Edge(int edge_id,
                int start_node,
                int end_node,
                double length,
                boolean isRoad,
                boolean hasPedestrian,
                boolean hasStreetConnect){

        this.edge_id = edge_id;
        this.start_node_id = start_node;
        this.end_node_id = end_node;
        this.length = this.reverse_length = length;

        this.isRoad = isRoad;
        this.hasPedestrian = hasPedestrian;
        this.hasStreetConnect = hasStreetConnect;
    }
    public Edge(int edge_id,
                String name,
                int start_node,
                int end_node,
                boolean isRoad,
                boolean hasPedestrian,
                boolean hasStreetConnect){

        this.edge_id = edge_id;
        this.name = name;
        this.start_node_id = start_node;
        this.end_node_id = end_node;
        this.isRoad = isRoad;
        this.hasPedestrian = hasPedestrian;
        this.hasStreetConnect = hasStreetConnect;
    }

    public void setEdge_id(int edge_id) {
        this.edge_id = edge_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStart_node(int start_node) {
        this.start_node_id = start_node;
    }

    public void setEnd_node_id(int end_node_id) {
        this.end_node_id = end_node_id;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public void setReverse_length(double reverse_length) {
        this.reverse_length = reverse_length;
    }

    public void setRoad(boolean road) {
        isRoad = road;
    }

    public void setHasPedestrian(boolean hasPedestrian) {
        this.hasPedestrian = hasPedestrian;
    }

    public void setHasStreetConnect(boolean hasStreetConnect) {
        this.hasStreetConnect = hasStreetConnect;
    }

    public Node getStartNode() {
        return startNode;
    }

    public Node getEndNode() {
        return endNode;
    }

    public void setStart_node_id(int start_node_id) {
        this.start_node_id = start_node_id;
    }

    public void setStartNode(Node startNode) {
        this.startNode = startNode;
    }

    public void setEndNode(Node endNode) {
        this.endNode = endNode;
    }

    public int getEdge_id() {
        return edge_id;
    }

    public String getName() {
        return name;
    }

    public int getStart_node_id() {
        return start_node_id;
    }

    public int getEnd_node_id() {
        return end_node_id;
    }

    public double getLength() {
        return length;
    }

    public double getReverse_length() {
        return reverse_length;
    }

    public boolean isRoad() {
        return isRoad;
    }

    public boolean isHasPedestrian() {
        return hasPedestrian;
    }

    public boolean isHasStreetConnect() {
        return hasStreetConnect;
    }
}

