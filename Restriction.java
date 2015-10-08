
public class Restriction {
	
	private int nodeID1;
	private int roadID1;
	private int interNode;
	private int nodeID2;
	private int roadID2;
	
	public Restriction(int nodeID1, int roadID1, int interNode, int roadID2, int nodeID2){
		this.nodeID1 = nodeID1;
		this.roadID1 = roadID1;
		this.interNode = interNode;
		this.nodeID2 = nodeID2;
		this.roadID2 = roadID2;
	}
	
	public int getNodeIDOne(){
		return nodeID1;
	}
	
	public int getRoadIDOne(){
		return roadID1;
	}
	
	public int getInterNode(){
		return interNode;
	}
	
	public int getNodeIDTwo(){
		return nodeID2;
	}
	
	public int getRoadIDTwo(){
		return roadID2;
	}

}
