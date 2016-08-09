package com.example.chadwick.feelwearugo;

/**
 * Created by Chadwick on 3/27/2015.
 */


import android.location.Location;
import android.util.Log;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class UWIMap {


    public Node start;
    public Node end;
    public Location locationOnMap;
    public float headingOnMap;
    SimpleWeightedGraph<Node,DefaultWeightedEdge> graph;
    boolean atANodeOnMap;
    private static UWIMap uwiMap = null;
    protected UWIMap tempMap = null;
    public static Node currentNode;

    public Node node;
    public Node previousNode;
    public Edge edge;
    public static int indx2;

    public static ArrayList<Node> nodeStack;
    public static ArrayList<Node> nodesOnMap;
    public static ArrayList<Edge> edgesOnMap;
    public static ArrayList<DefaultWeightedEdge> edgeList;
    //_______________________________________________________________________
    public static boolean isAtANode= false;
    //___________________VARIABLES FOR DS PROCESSING_________________________
    private static int lastNodeId;
    private static int lastEdgeId;
//    private static ArrayList<Edge> edgesFromDB;
//    private static ArrayList<Node> nodesFromDB;



    private UWIMap(){}

    //TODO: create graph based on returned PostgreSQL query


    public static UWIMap getUWIMap(){
        if (uwiMap == null) {
            //--------------------------UWIMap Init-----------------------------------------------------
            uwiMap = new UWIMap();
            indx2 = 0;
            //TODO: ConnectTask- retrieve nodes from DB and then add to NodesOnMap Static List
            //TODO: RetrieveEdgeTask - retrieves edges from dB
            //TODO: Model2Graph - Add Graph edges and reverse edges
            uwiMap.graph = new SimpleWeightedGraph<Node, DefaultWeightedEdge>(DefaultWeightedEdge.class);
            //Retrieve Nodes
            new ConnectionTask().execute("");
            //Convert node data model to graph
            uwiMap.node2Vertex();
            //Retrieve edges
            new RetreiveEdgeTask().execute("");
            //convert edge data model to graph
            uwiMap.modelEdge2GraphEdge();

            //----------------------Graph Construction -------------------------------------------------

//            //----------------------------Vertices--------------------------------------------------
//
//            Node assemblyHall = new Node(18.00542586, -76.74743291,"assembly hall",null, false,false);
//            uwiMap.graph.addVertex(assemblyHall);
//
//            Node admissions = new Node(18.005958, -76.74738, "admissions", null, false,false);
//            uwiMap.graph.addVertex(admissions);
//
//            Node undercoft = new Node(18.006003, -76.747264, "undercroft", null, false,false);
//            uwiMap.graph.addVertex(undercoft);
//
//            Node studentServices = new Node(18.005742, -76.747603, "student services", null, false,false);
//            uwiMap.graph.addVertex(studentServices);
//
//            Node inFrontOfStudentServices = new Node(18.005723, -76.747586, "infss", null, false, false);
//            uwiMap.graph.addVertex(inFrontOfStudentServices);
//
//            Node fieldInt1 = new Node(18.005682, -76.747846, "fi", null, false,false);
//            uwiMap.graph.addVertex(fieldInt1);
//
//            Node queenswayInt1 = new Node(18.005662, -76.747419, "q1", null, false,false);
//            uwiMap.graph.addVertex(queenswayInt1);
//
//            Node queenswayInt3 = new Node(18.005454, -76.747713, "q3", null, false, false);
//            uwiMap.graph.addVertex(queenswayInt3);
//
//            Node ped1 = new Node(18.005596, -76.748408, "ped1",null,true,true);
//            uwiMap.graph.addVertex(ped1);
//
//
//
//            // ----------------------------------Edges------------------------------------------------------------
//
//            DefaultWeightedEdge qw3ToAssemb =uwiMap.graph.addEdge(queenswayInt3,assemblyHall);
//            uwiMap.graph.setEdgeWeight(qw3ToAssemb, queenswayInt3.distanceTo(assemblyHall));
//
//            DefaultWeightedEdge adminToUnderCroft = uwiMap.graph.addEdge(admissions, undercoft);
//            uwiMap.graph.setEdgeWeight(adminToUnderCroft, admissions.distanceTo(undercoft));
//
//            DefaultWeightedEdge adminToStudentServices = uwiMap.graph.addEdge(admissions,studentServices);
//            uwiMap.graph.setEdgeWeight(adminToStudentServices, admissions.distanceTo(studentServices));
//
//            DefaultWeightedEdge ssToinFrontofsservice = uwiMap.graph.addEdge(inFrontOfStudentServices, studentServices);
//            uwiMap.graph.setEdgeWeight(ssToinFrontofsservice, studentServices.distanceTo(inFrontOfStudentServices));
//
//            DefaultWeightedEdge i1Toss = uwiMap.graph.addEdge(fieldInt1, inFrontOfStudentServices);
//            uwiMap.graph.setEdgeWeight(i1Toss, fieldInt1.distanceTo(inFrontOfStudentServices));
//
//            DefaultWeightedEdge ssToqw3 = uwiMap.graph.addEdge(inFrontOfStudentServices, queenswayInt1);
//            uwiMap.graph.setEdgeWeight(ssToqw3, inFrontOfStudentServices.distanceTo(queenswayInt1));
//
//            DefaultWeightedEdge qw1Toqw3 = uwiMap.graph.addEdge(queenswayInt1, queenswayInt3);
//            uwiMap.graph.setEdgeWeight(qw1Toqw3, queenswayInt1.distanceTo(queenswayInt3));
//
//            DefaultWeightedEdge fieldInt1ToPed1 = uwiMap.graph.addEdge(ped1, fieldInt1);
//            uwiMap.graph.setEdgeWeight(fieldInt1ToPed1, ped1.distanceTo(fieldInt1));
//
//            DefaultWeightedEdge fieldInt1ToQw3 = uwiMap.graph.addEdge(queenswayInt3, fieldInt1);
//            uwiMap.graph.setEdgeWeight(fieldInt1ToQw3, queenswayInt1.distanceTo(fieldInt1));


            //----------------------------------INIT START-END For Pathfinder------------------------------------------

//            uwiMap.start =queenswayInt3 ;
//            uwiMap.end =undercoft;
//
//            uwiMap.locationOnMap = ped1.getLocation();
//            uwiMap.headingOnMap = 0.00f;
        }

        return uwiMap;
    }

    public void node2Vertex(){
        //TODO: get edges and nodes and contruct a graph.
        for(Node node: nodesOnMap){
            uwiMap.graph.addVertex(node);
        }

    }

    private Node getNodeByID(int id){
        for(int i = 0; i<nodesOnMap.size(); i++) {
              if(id == nodesOnMap.get(i).getNode_id()){
                 node =  nodesOnMap.get(i);
              }
        }
        return node;
    }

    private Edge getEdgeByID(int id){
        for (int i=0; i<edgesOnMap.size(); i++){
            if(id == edgesOnMap.get(i).getEdge_id()){
                edge = edgesOnMap.get(i);
            }
        }
        return edge;
    }


    public void modelEdge2GraphEdge(){

        for(Edge edge : edgesOnMap){
            //TODO: uwiMap.graph.addEdge()
            Node startNode = getNodeByID(edge.getStart_node_id());
            Node endNode = getNodeByID(edge.getEnd_node_id());
            edgeList.add(indx2, uwiMap.graph.addEdge(startNode, endNode));
            indx2++;
            edgeList.add(indx2, uwiMap.graph.addEdge(endNode,startNode));
            indx2++;

        }
    }

    /*TODO: get real-time feedback on current edge from user (good, crowded, hard to navigate, flooded)
             geo tag and time stamp feedback. Run response in the SVM algorithm to establish weight
             update to different edges at different times of the day

             HOW?
             1. get edge the user is currently on
             2. post user comment/feedback to server Path-learning algorithm.
             3. the path will then predict the condition of the street and then adjust edge weight based on the prediction.
             4. Also use weather prediction to alert and adjust edge weights

     TODO: learning GPS filter:

           HOW?
           1. make gps trace of users traversing paths. and A estimate a gps band filter to pass predicted coordinate values
           2. filter that learns and predict coordinate values based on gps trace




    */
    //TODO: create method that creates a temporary map with data from from the server.
    public UWIMap getTempMap(List<Node> pathList2){
        uwiMap = new UWIMap();
        uwiMap.graph = new SimpleWeightedGraph<Node, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        List<Node> vertexList;

        for(int i=0;i<pathList2.size();i++){
            int node_id= pathList2.get(i).getNode_id();
            String node_name= pathList2.get(i).getName();
            Double latitude= pathList2.get(i).getLatitude();
            Double longitude= pathList2.get(i).getLongitude();

            //Adds vertices (nodes) to the created graph structure.
            uwiMap.graph.addVertex(new Node(node_id, node_name, latitude, longitude));
            //TODO: add edges.
        }

        //Create edges to connect these newly create nodes to make a path graph
        return uwiMap;
    }


    public static boolean addNodeToMap(Node node) {
        UWIMap.nodesOnMap.add(node);
        return true;
    }

    public static boolean addEdgeToMap(Edge edge) {
        UWIMap.edgesOnMap.add(edge);
        return true;
    }

    public void setNodeAsDestination(){end.setAsDestination();}

    public boolean isInMap(Node node) {
        return graph.containsVertex(node);
    }

    public void setStartVertexViaLocation(Location location){

        ArrayList<Node> vertexList = new ArrayList<>();
        vertexList.addAll(getUWIMap().getGraph().vertexSet());
        //float firstBearing;

        for (int i = 0; i < vertexList.size(); i++) {
            vertexList.get(i).setCost(vertexList.get(i).distanceTo(location));
        }
        Collections.sort(vertexList);
        start = vertexList.get(0);
        Log.d("START_SET:",getUWIMap().start.toString());
        // Log.d("VERTEX_LIST",vertexList.toString());
        // Log.d("Start:",getUWIMap().start.toString());

    }

    public void setCurrentNode(Node node){
        this.currentNode = node;
    }

    public Node getCurrentNodeAndSetItAsStart() {
        start = currentNode;
        return currentNode;
    }

    public float getHeadingOnMap() {
        return headingOnMap;
    }

    public boolean isAtANodeOnMap(){
        return this.atANodeOnMap;
    }

    public void setAtANodeOnMap(boolean atANodeOnMap) {
        this.atANodeOnMap = atANodeOnMap;
    }

    public void setLocationOnMap(Location locationOnMap) {
        this.locationOnMap = locationOnMap;
    }

    public void setHeadingOnMap(float headingOnMap) {
        this.headingOnMap = headingOnMap;
    }

    public void setStartVertexViaName(String name){
        ArrayList<Node> vertexList1 = new ArrayList<>();
        vertexList1.addAll(getUWIMap().getGraph().vertexSet());

        try {
            for (int i = 0; i < vertexList1.size(); i++) {
                if (name.equals(vertexList1.get(i).getName())) {
                    start = vertexList1.get(i);
                    //TODO: WHY set asDestination and not start?
                    vertexList1.get(i).setAsDestination();
                    Log.d("START_SET:",getUWIMap().start.toString());
                }
            }
        }catch (IndexOutOfBoundsException e){
            Log.d("UWIMAP:","Trying to search beyond the graph");
        }
    }

    public  void setEndVertexViaName(String name){
        ArrayList<Node> vertexList1 = new ArrayList<>();
        vertexList1.addAll(getUWIMap().getGraph().vertexSet());

        try {
            for (int i = 0; i < vertexList1.size(); i++) {
                if (name.equals(vertexList1.get(i).getName())) {
                    end = vertexList1.get(i);
                    vertexList1.get(i).setAsDestination();
                    Log.d("DESTINATION_SET:",getUWIMap().end.toString());
                }
            }
        }catch (IndexOutOfBoundsException e){
            Log.d("UWIMAP:","you are trying to search beyond the graph");
        }


    }

    public Location getLocationOnMap() {
        return locationOnMap;
    }

    public SimpleWeightedGraph<Node, DefaultWeightedEdge> getGraph() {
        return graph;
    }

    public Node getStart(){
            return start;
    }

    public Node getEnd(){
        return end;
    }

    //----------------------- Important Addition to UWIMap-------------------------------------------

    public void addCurrentLocationToMapAndSetAsStart(){

            Node startPoint = new Node(locationOnMap.getLatitude(),locationOnMap.getLongitude(),"start","",false,false);
            ArrayList<Node> vertexList = new ArrayList<>();
            vertexList.addAll(getUWIMap().getGraph().vertexSet());
            for (int i = 0; i < vertexList.size(); i++) {
                vertexList.get(i).setCost(vertexList.get(i).distanceTo(locationOnMap));
            }
            Collections.sort(vertexList);
            uwiMap.graph.addVertex(startPoint);
            DefaultWeightedEdge  startPointToStart = uwiMap.graph.addEdge(startPoint, vertexList.get(0));
            uwiMap.graph.setEdgeWeight(startPointToStart, startPoint.distanceTo(vertexList.get(0)));
            start = startPoint;
        }

    public void resetAllNodesNotification(){
        ArrayList<Node> vertexList = new ArrayList<>();
        vertexList.addAll(getUWIMap().getGraph().vertexSet());
        for(int i=0;i<vertexList.size();i++){
            vertexList.get(i).setNotification("I'm a node");
        }
    }

    //TODO: create method that creates a temporary map with data from from the server.




}




