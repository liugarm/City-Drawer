import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Segment {
	
	private int id;
	private double length;
	private int nodeID1;
	private int nodeID2;
	private List<Location> coordinates;
	
	public Segment(int id, double length, int nodeID1, int nodeID2, List<Location> coordinates){
		this.id = id;
		this.length = length;
		this.nodeID1 = nodeID1;
		this.nodeID2 = nodeID2;
		this.coordinates = coordinates;
	}
	
	public int getID(){
		return id;
	}
	
	public double getLength(){
		return length;
	}
	
	public int getFirstNode(){
		return nodeID1;
	}
	
	public int getSecondNode(){
		return nodeID2;
	}
	
	public List<Location> getCoordinates(){
		return coordinates;
	}
	
	public void draw(Graphics g, Location n, int scale, int mouseX, int mouseY, Map<Integer, Node> nodes, Color colour){
		
		//Draw the lines to connect the Segment coordinates

		for(int i=0;i<coordinates.size()-1;i++){
			
			int firstX = 0;
			int firstY = 0;
			int secondX = 0;
			int secondY = 0;
			
			firstX = (int)((coordinates.get(i).x-n.x)*scale)+mouseX;
			firstY = (int)((n.y - coordinates.get(i).y)*scale)+mouseY;
				
			secondX = (int)((coordinates.get(i+1).x-n.x)*scale)+mouseX;
			secondY = (int)((n.y - coordinates.get(i+1).y)*scale)+mouseY;
			
			g.setColor(colour);
			g.drawLine(firstX,firstY,secondX,secondY);
		}
		
		//Connect Node1 and Node2 with the start and end of the road segments
		
		Location nodeOneLoc = null;
		Location nodeTwoLoc = null;
		
		//Look for the nodes with NodeID1 & NodeID2
		if(nodes.containsKey(nodeID1)){
			nodeOneLoc = Location.newFromLatLon(nodes.get(nodeID1).getLatitude(), nodes.get(nodeID1).getLongitude());
		}
		
		if(nodes.containsKey(nodeID2)){
			nodeTwoLoc = Location.newFromLatLon(nodes.get(nodeID2).getLatitude(), nodes.get(nodeID2).getLongitude());
		}
		
		Location startSeg = coordinates.get(0);
		Location endSeg = coordinates.get(coordinates.size()-1);
		
		int nodeOneX = 0;
		int nodeOneY = 0;
		
		int startSegX = 0;
		int startSegY = 0;
		
		int nodeTwoX = 0;
		int nodeTwoY = 0;
		
		int endSegX = 0;
		int endSegY = 0;
		
		//Calculate the x and y locations for the segment points and draw them

		nodeOneX = (int)((nodeOneLoc.x-n.x)*scale)+mouseX;
		nodeOneY = (int)((n.y - nodeOneLoc.y)*scale)+mouseY;
			
		startSegX = (int)((startSeg.x-n.x)*scale)+mouseX;
		startSegY = (int)((n.y - startSeg.y)*scale)+mouseY;
			
		nodeTwoX = (int)((nodeTwoLoc.x-n.x)*scale)+mouseX;
		nodeTwoY = (int)((n.y - nodeTwoLoc.y)*scale)+mouseY;
			
		endSegX = (int)((endSeg.x-n.x)*scale)+mouseX;
		endSegY = (int)((n.y - endSeg.y)*scale)+mouseY;
		
		g.setColor(colour);
		g.drawLine(nodeOneX, nodeOneY, startSegX, startSegY);
		g.drawLine(nodeTwoX, nodeTwoY, endSegX, endSegY);
	}

}
