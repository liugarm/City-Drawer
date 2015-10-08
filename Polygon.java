import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class Polygon {

	private String type;
	private String label;
	private String colourType = "";
	private int endLevel;
	private int cityIdx;
	private List<Location> dataZero;
	private List<Location> dataOne;

	public Polygon(String type, String label, int endLevel, int cityIdx,
			List<Location> dataZero, List<Location> dataOne) {
		this.type = type;
		this.label = label;
		this.endLevel = endLevel;
		this.cityIdx = cityIdx;
		this.dataZero = dataZero;
		this.dataOne = dataOne;

		colourType = colourType();
	}

	public String getType() {
		return type;
	}

	public String getLabel() {
		return label;
	}

	public int getEndLevel() {
		return endLevel;
	}

	public int getCityIdx() {
		return cityIdx;
	}

	public List<Location> getDataZero() {
		return dataZero;
	}

	public void draw(Graphics g, Location n, int scale, int mouseX, int mouseY) {

		Graphics2D g2 = (Graphics2D) g;

		Path2D.Double path = new Path2D.Double();

		if (!dataZero.isEmpty()) {
			
			for (int i = 0; i < dataZero.size(); i++) {

				int firstX = 0;
				int firstY = 0;

				firstX = (int) ((dataZero.get(i).x - n.x) * scale) + mouseX;
				firstY = (int) ((n.y - dataZero.get(i).y) * scale) + mouseY;
				
				if(i>0){
					path.lineTo(firstX, firstY);
				}
				else{
					path.moveTo(firstX, firstY);
				}
			}
			
			path.closePath();
			

			if (colourType.equals("CITY")) {
				g.setColor(new Color(239, 235, 229));
			} else if (colourType.equals("SCHOOL")) {
				g.setColor(new Color(209, 204, 194));
			} else if (colourType.equals("HOSPITAL")) {
				g.setColor(new Color(225, 28, 28));
			} else if (colourType.equals("MAN_MADE_SECONDARY")) {
				g.setColor(new Color(225, 98, 28));
			} else if (colourType.equals("MAN_MADE")) {
				g.setColor(new Color(208, 88, 23));
			} else if (colourType.equals("PARK_RESERVE")) {
				g.setColor(new Color(202, 223, 170));
			} else if (colourType.equals("WATER")) {
				g.setColor(new Color(179, 209, 255));
			} else if (colourType.equals("WOODS")) {
				g.setColor(new Color(205, 173, 145));
			}

			g2.fill(path);
		}
	}

	public String colourType() {
		int t = Integer.decode(type);
		if (t >= 1 && t <= 3)
			return "CITY";
		if (t == 0xa)
			return "SCHOOL";
		if (t == 0xb)
			return "HOSPITAL";
		if (t >= 7 && t <= 0xd)
			return "MAN_MADE_SECONDARY";
		if (t == 0xe || t == 0x13)
			return "MAN_MADE";
		if (t == 0x1a)// cemetary
			return "MAN_MADE_SECONDARY";
		if (t >= 0x14 && t <= 0x1f)
			return "PARK_RESERVE";
		if (t >= 0x28 && t <= 0x49)
			return "WATER";
		if (t == 0x50)
			return "WOODS";
		
		return "";
	}

}
