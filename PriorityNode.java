
public class PriorityNode {
	
	private Node node;
	private PriorityNode from;
	private double costToHere;
	private double totalCostToGoal;
	
	
	public PriorityNode(Node node, PriorityNode from, double costToHere, double totalCostToGoal){
		this.node = node;
		this.from = from;
		this.costToHere = costToHere;
		this.totalCostToGoal = totalCostToGoal;
	}
	
	public Node getNode(){
		return node;
	}
	
	public PriorityNode getFromNode(){
		return from;
	}
	
	public void setFromNode(PriorityNode n){
		from = n; 
	}
	
	public double getTotalCostToGoal(){
		return totalCostToGoal;
	}
	
	public double getCostToHere(){
		return costToHere;
	}
	
	public void setTotalCostToGoal(double cost){
		totalCostToGoal = cost;
	}
	
	public void setCostToHere(double cost){
		costToHere = cost;
	}

}
