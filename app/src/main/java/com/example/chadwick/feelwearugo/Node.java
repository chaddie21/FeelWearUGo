package com.example.chadwick.feelwearugo;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by Chadwick on 3/3/2015.
 */
public class Node extends Location implements Comparable{

        public int node_id;
        public String name;
        public int[] nextNodes;
        public float distanceToDestination;
        public float cost;
        private boolean nodeStatus= false;
        public boolean isStart = false;
        public boolean isDestination = false;
        public float heading;
        public String  contactNumber;
        public boolean isPedestrianCrossing;
        public boolean isRoad;
        private String notification;



   public Node(double latitude,double longitude, String mName
                ,String contactNumber, boolean isPedestrianCrossing,boolean road) {
        super("");

        this.setLatitude(latitude);
        this.setLongitude(longitude);
        name = mName;
        isRoad = road;
        this.isPedestrianCrossing = isPedestrianCrossing;
        distanceToDestination =0;
        cost = 0;
        nodeStatus = false;
        this.contactNumber = contactNumber;
        this.isDestination = false;
        this.notification = "I'm a node";
    }

    public Node( int node_id, String mName, double latitude, double longitude){
        super("");
        this.node_id= node_id;
        name = mName;
        this.setLatitude(latitude);
        this.setLongitude(longitude);

    }
   private void nodeIdGenerator(){}


    public void setName( String mName){
        name = mName;
    }

   public void setLocation(double latitude, double longitude){
       this.setLongitude(longitude);
       this.setLatitude(latitude);
   }

   public void setNodeStatusToClose(){
        nodeStatus = false;
    }

   public void setNodeStatusToOpen(){
        nodeStatus = true;
    }

   public boolean isClose() {

        boolean status;

        if (!nodeStatus) status = true;
        else status =false;

        return status;
    }

   public boolean isOpen() {

        boolean status;
        if (nodeStatus) status = true;
        else status =false;

        return status;
    }

   public void setNextNodes(int[] nn){
        nextNodes = nn;
    }

   public int[] getNextNodes(){return nextNodes;}


   public float getDistanceToDestination(){
        return distanceToDestination;
    }

   public double getCost() {
        return cost;
    }

   public void setContactNumber(String contact){
        contactNumber = contact;
    }

   public float getHeading() {
        return heading;
    }

   public void setHeading(float heading) {

        this.heading = heading;
    }

   public Location getLocation(){
       return this;
   }

   public String getName(){ return name; }

   public void setAsDestination(){
       notification = "This is your stop, you have reached your destination, a notification of your arrival via text message should have been sent already";
       this.isDestination = true;
   }
    public void setAsStart(){
        notification = "hi there, this is where we start";
        this.isStart = true;
    }

   public boolean isDestination(){
       return isDestination;
   }

   public void setCost(float c){
        this.cost = c;
    }

   public void setNextAction(float heading){
       int i =0;
       if (heading>=-180.00 && heading<-135.00)i=4;
       else if(heading>=-135.00 && heading < -45.00)i=1;
       else if((heading >= -45.00)&&(heading <45.00))i =2;
       else if((heading >= 45.00)&&(heading <=135.00))i= 3;
       else if((heading>135.00)&&(heading<180))i=4;
       else if (isDestination())i = 5;
       //else if (heading == -1000.00f)

       switch(i){
           case 1: notification = "Left turn";
               break;
           case 2: notification = "Go straight ahead";
               break;
           case 3: notification = "Right turn";
               break;
           case 4: notification = "turn around";
               break;
           case 5: notification = " Slow down please, you have reached your destination, the notification of your arrival via text message should have been sent already";
                break;
       }
   }

    public void setNotification(String notification) {
        this.notification = notification;
    }

   public String getNotification(){
        return notification;
    }

   public int compareTo(Object other){
       Node o = (Node) other;

       float f = cost;
       float of = o.cost;
       if (f < of) {
           return -1;
       } else if(f > of){
           return 1;
       }else{
           return 0;
       }
   }
    public void setNode_id(int node_id) {
        this.node_id = node_id;
    }

    public int getNode_id() {
        return node_id;
    }

    public String getContactNumber(){return contactNumber;}

    public String toString(){
        return Integer.toString(getNode_id())+" "+getName()+" "+Double.toString(getLatitude())+" "+Double.toString(getLongitude())+" "+getNotification();
    }


}
/**
 *  switch(i){
 case 1: notification = " slow down, take the next left turn, and continue on the walkway";
 break;
 case 2: notification = "continue straight ahead, remember to stay on the walkway";
 break;
 case 3: notification = "slow down, take the next right turn, and continue on the walkway";
 break;
 case 4: notification = "Turn right back around";
 break;
 case 5: notification = " Slow down please, you have reached your destination, " +
 "swipe to your screen leftwards to request assistance from the attendants";
 break;
 }
 */
