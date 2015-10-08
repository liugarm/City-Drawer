import java.util.HashSet;
import java.util.Set;


public class Road {
	
	private int id;
	private int type;
	private String label;
	private String city;
	private int oneWay;
	private int speed;
	private int roadClass;
	private int notForCar;
	private int notForPede;
	private int notForBicy;
	private Set<Segment> segments;
	
	public Road(int id, int type, String label, String city, int oneWay, int speed, int roadClass, int notForCar, int notForPede, int notForBicy){
		this.id = id;
		this.type = type;
		this.label = label;
		this.city = city;
		this.oneWay = oneWay;
		this.speed = speed;
		this.roadClass = roadClass;
		this.notForCar = notForCar;
		this.notForPede = notForPede;
		this.notForBicy = notForBicy;
		
		segments = new HashSet<Segment>();
	}
	
	public int getID(){
		return id;
	}
	
	public int getType(){
		return type;
	}
	
	public String getLabel(){
		return label;
	}
	
	public String getCity(){
		return city;
	}
	
	public int getOneWay(){
		return oneWay;
	}
	
	public int getSpeed(){
		return speed;
	}
	
	public int getRoadClass(){
		return roadClass;
	}
	
	public int getNotForCar(){
		return notForCar;
	}
	
	public int getNotForPede(){
		return notForPede;
	}
	
	public int getNotForBicy(){
		return notForBicy;
	}
	
	public void addSegment(Segment s){
		segments.add(s);
	}
	
	public Set<Segment> getSegments(){
		return segments;
	}
	
	public String toString(){
		int speedLimit = 0;
		String car = "";
		String pede = "";
		String bicy = "";
		String way = "";
		String road = "";
		
		if(speed==0){
			speedLimit = 5;
		}
		else if(speed==1){
			speedLimit = 20;
		}
		else if(speed==2){
			speedLimit = 40;
		}
		else if(speed==3){
			speedLimit = 60;
		}
		else if(speed==4){
			speedLimit = 80;
		}
		else if(speed==5){
			speedLimit = 100;
		}
		else if(speed==6){
			speedLimit = 110;
		}
		
		if(notForCar==1){
			car = "No";
		}
		else{
			car = "Yes";
		}
		
		if(notForPede==1){
			pede = "No";
		}
		else{
			pede = "Yes";
		}
		
		if(notForBicy==1){
			bicy = "No";
		}
		else{
			bicy = "Yes";
		}
		
		if(oneWay==1){
			way = "One Way Road";
		}
		else{
			way = "Both Directions Allowed";
		}
		
		if(roadClass==0){
			road = "Residential";
		}
		else if(roadClass==1){
			road = "Collector";
		}
		else if(roadClass==2){
			road = "Arterial";
		}
		else if(roadClass==3){
			road = "Principal HW";
		}
		else if(roadClass==4){
			road = "Major HW";
		}
		
		
		
		String info = "\nID: "+id+"\nType:"+type+"\nLabel: "+label+"\nCity: "+city+"\nOne Way: "+way+"\nSpeed Limit: "+speedLimit+
				" KM/H\nRoad Class: "+road+"\nCars Allowed: "+car+"\nPedestrians Allowed: "+pede+"\nBicycles Allowed: "+bicy;
		
		return info;
	}
}
