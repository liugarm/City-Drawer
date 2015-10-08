import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Node {

	private int id;
	private double latitude;
	private double longitude;
	private List<Segment> segments;
	private List<Segment> allSegments;
	private List<Node> neighbourNodes;

	private int x;
	private int y;
	
	private Location thisNode;
	private boolean visited = false;
	
	private int depth;

	public Node(int id, double latitude, double longitude) {
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;

		segments = new ArrayList<Segment>();
		neighbourNodes = new ArrayList<Node>();
		allSegments = new ArrayList<Segment>();
		
		thisNode = Location.newFromLatLon(latitude, longitude);
	}

	public int getID() {
		return id;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void addSegment(Segment seg) {
		segments.add(seg);
	}

	public List<Segment> getSegments() {
		return segments;
	}
	
	public Location getLocation(){
		return thisNode;
	}
	
	public boolean getVisited(){
		return visited;
	}
	
	public void setVisited(boolean visit){
		visited = visit;
	}
	
	public List<Node> getNeighbourNodes(){
		return neighbourNodes;
	}
	
	public void addNeighbourNode(Node n){
		neighbourNodes.add(n);
	}
	
	public int getDepth(){
		return depth;
	}
	
	public void setDepth(int d){
		depth = d;
	}
	
	public List<Segment> getAllSegments(){
		return allSegments;
	}
	
	public void addToAllSegments(Segment s){
		allSegments.add(s);
	}


	public void draw(Graphics g, Location n, int scale, int mouseX, int mouseY, Color colour) {
		g.setColor(colour);

		x = (int) ((thisNode.x - n.x) * scale) + mouseX;
		y = (int) ((n.y - thisNode.y) * scale) + mouseY;

		if (colour.equals(new Color(0,90,255))) {
			g.fillRect(x, y, 1, 1);
		} else {
			g.fillRect(x-2, y-2, 4, 4);
		}
	}

}
