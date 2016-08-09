/**
 * Created by Chadwick on 3/15/2015.
 */

package com.example.chadwick.feelwearugo;



import android.util.Log;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.ArrayList;
import java.util.List;


public class PathFinder {
    private final String URL_TO_HIT = "http://104.155.128.82/";
    DijkstraShortestPath<Node, DefaultWeightedEdge> pathfinderAlgorithm;
    UWIMap uwiMap;
    Node currentNode;
    Node destination;


    public PathFinder() {

        // KEYNOTE: Initialization of the UWIMap;
        this.uwiMap = UWIMap.getUWIMap();

        // Set the start and end node for the pathfinder
        this.currentNode = uwiMap.getStart();
        this.destination = uwiMap.getEnd();

        pathfinderAlgorithm = new DijkstraShortestPath<Node, DefaultWeightedEdge>(uwiMap.getGraph(),currentNode,destination);
    }

    public  List<DefaultWeightedEdge> getPath(){
       return  pathfinderAlgorithm.getPathEdgeList();
    }


    // This function starts pathfinder algo. NEVER KNEW!


    public float[] getPathBearing(){

        /**
         * I am trying to access the Edges from the list of edges returned from the dijkstraShortestPath.
         * After that i want to get the bearing between the two nodes connected by the said edge within the path.
        */
        List<DefaultWeightedEdge> edgeList = pathfinderAlgorithm.getPathEdgeList();

        float[] bearings = new float[edgeList.size()];
        int i=0;
        for( DefaultWeightedEdge edge: edgeList){
           bearings[i] = uwiMap.getGraph().getEdgeSource(edge).bearingTo(uwiMap.getGraph().getEdgeTarget(edge));
           i++;
        }
        return bearings;
    }



    //TODO: call pathfinder.getPathBearingTemp() to start ConnectionTask then get bearing.
    //TODO: Split into getter and setter!

    public float[] getPathBearingTemp(List<Node> pathList){
        //This should execute onPostLoad which display on UI or log.
        Log.d("pathList", pathList.toString());

        float[] bearings = new float[pathList.size()];
        int i = 0;
        for (Node node: pathList){

             if (i < pathList.size()-1) bearings[i] = node.bearingTo(pathList.get(i + 1));

            i++;
        }

        return bearings;
    }

    public String getLats(List<Node> pathList){
        String list="";
        try {
            list= pathList.get(1).toString();
            for (int i = 0; i < pathList.size(); i++) {
                list += " " + pathList.get(i).getLocation() + ", ";

            }
        }catch (IndexOutOfBoundsException e){
            e.printStackTrace();
        }

        return list;
    }



    public void setFirstNodeTurn(float angle){

        List<DefaultWeightedEdge> edgeList = pathfinderAlgorithm.getPathEdgeList();
        int i=0;
        float bearing =0;
        for( DefaultWeightedEdge edge: edgeList){
            if(i == 0){
                bearing = uwiMap.getGraph().getEdgeSource(edge).bearingTo(uwiMap.getGraph().getEdgeTarget(edge));
                uwiMap.getGraph().getEdgeSource(edge).setNextAction((float) (((((bearing- angle)%360.00)+540.00)%360.00)-180.00));
            }
            i++;
        }




    }

    public float[] setNextTurnAngles(float[] bearing){

        float[] nextHeading= new float[bearing.length];
        float buffer=0;

        for(int i=0;i<bearing.length-1;i++) {
           /* nextHeading[i] = (float)Math.min((bearing[i] - bearing[i+1])<0?
                                                 bearing[i]-bearing[i+1]+360.00: bearing[i]-bearing[i+1],
                                             (bearing[i+1]-bearing[i])<0?
                                                 bearing[i+1]-bearing[i]+360.00: bearing[i+1]-bearing[i]);
                                                 */
           nextHeading[i] = (float) (((((bearing[i+1]- bearing[i])%360.00)+540.00)%360.00)-180.00);
        }
        return nextHeading;
    }

    public void setNextTurnInstructions(float[] nextHeadings){
        List<DefaultWeightedEdge> edgeList = pathfinderAlgorithm.getPathEdgeList();
        int i=0;
        for( DefaultWeightedEdge edge: edgeList){
            if(i == 0){
                float bearing = uwiMap.getGraph().getEdgeSource(edge).bearingTo(uwiMap.getGraph().getEdgeTarget(edge));
                uwiMap.getGraph().getEdgeSource(edge).setNextAction(bearing);
            }
            uwiMap.getGraph().getEdgeTarget(edge).setNextAction(nextHeadings[i]);
            i++;
        }
    }

    public void setNextTurnInstructionTemp(float[] nextHeadings, List<Node> pathList){
        for(int i = 0;i<pathList.size();i++){
            pathList.get(i).setNextAction(nextHeadings[i]);
        }
    }

    public void pathFinder_setNodeBearing(){
        List<DefaultWeightedEdge> edgeList = pathfinderAlgorithm.getPathEdgeList();
        ArrayList<DefaultWeightedEdge> edgeArrayList = new ArrayList<>();
        edgeArrayList.addAll(edgeList);
        ArrayList<Node> path = new ArrayList<>();
        boolean currentFlipped = false;
        boolean prevFlipped = false;

        int i=0;
        for( DefaultWeightedEdge edge: edgeList){

            if(i == 0 && currentNode.equals(uwiMap.getGraph().getEdgeSource(edge))){
                path.add(uwiMap.getGraph().getEdgeSource(edge));
                path.add(uwiMap.getGraph().getEdgeTarget(edge));
//                Log.d("i == 0 && currentNode.equals edgeSource: ","entered");
                Log.d("edge"+i+"",edge.toString()+" "+currentFlipped+" "+path.size()+"");
                currentFlipped = false;
            }
            else if(i== 0 && currentNode.equals(uwiMap.getGraph().getEdgeTarget(edge))){
//                Log.d("i == 0 && currentNode.equals edgeTarget: ","entered");
                Log.d("edge"+i+"",edge.toString()+" "+currentFlipped+" "+path.size()+"");
                currentFlipped = true;
                path.add(uwiMap.getGraph().getEdgeTarget(edge));
                path.add(uwiMap.getGraph().getEdgeSource(edge));

            }
            else if (prevFlipped){
                if (path.get(i).equals(uwiMap.getGraph().getEdgeSource(edge))){
                    Log.d("first else if ","if");
                    Log.d("edge"+i+"",edge.toString()+" "+currentFlipped+" "+path.size()+"");
                    currentFlipped = false;
                    path.add(uwiMap.getGraph().getEdgeTarget(edge));


                }
                else{
                    path.add(uwiMap.getGraph().getEdgeSource(edge));
                    Log.d("first else if ","else");
                    Log.d("edge"+i+"",edge.toString()+" "+currentFlipped+" "+path.size()+"");
                    currentFlipped = true;
                }
            }
            else{
                if( path.get(i).equals(uwiMap.getGraph().getEdgeSource(edge))){
                    path.add(uwiMap.getGraph().getEdgeTarget(edge));
                    Log.d("else ","if");
                    Log.d("edge"+i+"",edge.toString()+" "+currentFlipped+" "+path.size()+"");
                    currentFlipped = false;


                }
                else{
                    path.add(uwiMap.getGraph().getEdgeSource(edge));
                    Log.d("else ","else");
                    Log.d("edge"+i+"",edge.toString()+" "+currentFlipped+" "+path.size()+"");
                    currentFlipped = true;
                }
            }


            prevFlipped = currentFlipped;
            i++;
        }
        path.add(destination);
        //TODO: PREE THIS!!

        for(int j=0;j<path.size()-1;j++){
            path.get(j).setHeading( path.get(j).bearingTo(path.get(j+1)) );
            Log.d("Path in correct Order:",path.get(j).toString()+" "+path.get(j).getHeading()+"");
        }

    }


    //TODO: receive array list of nodes in route order - Eg. path
    //
    //    for(int j=0;j<path.size()-1;j++){
    //        path.get(j).setHeading( path.get(j).bearingTo(path.get(j+1)) );
    //        Log.d("Path in correct Order:",path.get(j).toString()+" "+path.get(j).getHeading()+"");
    //    }

    public String[] getTurn(){
        List<DefaultWeightedEdge> edgeList = pathfinderAlgorithm.getPathEdgeList();
        String[] turn = new String[edgeList.size()+1];
        int i=0;
        for( DefaultWeightedEdge edge: edgeList){
            if (i==0) turn[i] = uwiMap.getGraph().getEdgeSource(edge).getNotification();
            turn[i+1]= uwiMap.getGraph().getEdgeTarget(edge).getNotification();
            i++;
        }
        return turn;
    }

    //TODO: modify getTURN!
    public String[] getTurnTemp(List<Node> pathList){
        String[] turn = new String[pathList.size()];
        int i = 0;
        for (Node node: pathList){
            turn[i] = node.getNotification();
            i++;
        }
        return turn;
    }

  }
/**
 *
 if(i == 0 && currentNode.equals(uwiMap.getGraph().getEdgeSource(edge))){
 path.add(uwiMap.getGraph().getEdgeSource(edge));
 path.add(uwiMap.getGraph().getEdgeTarget(edge));

 }
 else if(i== 0 && currentNode.equals(uwiMap.getGraph().getEdgeTarget(edge))){
 currentFlipped = true;
 path.add(uwiMap.getGraph().getEdgeTarget(edge));
 path.add(uwiMap.getGraph().getEdgeSource(edge));
 }
 else if(uwiMap.getGraph().getEdgeTarget(edge).equals(uwiMap.getGraph().getEdgeSource(edgeArrayList.get(i+1))) ) {
 if (!currentFlipped){
 path.add(uwiMap.getGraph().getEdgeTarget(edge));
 }

 path.add(uwiMap.getGraph().getEdgeTarget(edge));
 }
 else {
 if (currentFlipped)uwiMap.getGraph().getEdgeSource(edgeArrayList.get(i+1));
 else{
 path.add(uwiMap.getGraph().getEdgeSource(edge));
 currentFlipped = true;
 }
 }
  */