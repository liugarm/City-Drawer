import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;

public class CityDrawer {

	private JFrame frame;
	private JComponent drawing;
	private int windowSize = 700;
	private int scale = 1;
	private int highlightNode = -1;
	private int highlightNode2 = -1;
	private JTextArea logging = null;
	private JTextField textField;

	private boolean aSearched = false;
	private boolean hasFile = false;
	private String mode = "Distance";

	private Location topLeft = null;

	// Collectons for the roads
	private List<String> labels = new ArrayList<String>();
	private Set<String> highlightOne = new HashSet<String>();

	private Trie trieLabels;

	// Creating sets of Nodes (intersections) and Edges (roads connecting the
	// intersections)
	private Map<Integer, Node> idToNodes;
	private Map<Integer, Road> roads;
	private Set<Segment> segments;

	// For printing information out for each road
	private Map<String, List<Road>> labelToRoads;

	// Polygon collection
	private Set<Polygon> Polygons;

	// Drawing locations, offset values and mouse locations
	private int finalX = 0;
	private int finalY = 0;
	private int offsetX = 250;
	private int offsetY = 220;
	private int pressedX = 0;
	private int pressedY = 0;

	private List<String> highlightRoad = new ArrayList<String>();
	private List<Integer> highlightNodes = new ArrayList<Integer>();
	private List<Segment> highlightSegments = new ArrayList<Segment>();

	// Articulation Points
	private Set<Node> aPoints = new HashSet<Node>();

	// Restriction map from INTERSECTION NODE to RESTRICTION OBJECT
	private Map<Node, List<Restriction>> restrictions = new HashMap<Node, List<Restriction>>();

	/*
	 * Initialise the collections in constructor
	 */
	public CityDrawer() {
		idToNodes = new HashMap<Integer, Node>();
		roads = new HashMap<Integer, Road>();
		segments = new HashSet<Segment>();
		labelToRoads = new HashMap<String, List<Road>>();
		Polygons = new HashSet<Polygon>();

		setupWindow();
	}

	/*
	 * This method will set up a window to display the map of Auckland.
	 */
	@SuppressWarnings("unchecked")
	public void setupWindow() {

		// ******************************************************************
		// Creates a window and sets the size.
		// ******************************************************************
		frame = new JFrame("Map of Auckland");
		frame.setSize(windowSize + 100, windowSize);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set up JComponent. When it draws itself, it will call the redraw
		// method, passing a
		// Graphics object to it.
		drawing = new JComponent() {
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				redraw(g);
			}
		};

		// Add it to the frame and also set a border for the drawing JComponent
		// to make it look nicer.
		frame.add(drawing, BorderLayout.CENTER);
		drawing.setBorder(BorderFactory.createLoweredBevelBorder());

		// Setting up JPanel
		JPanel panel = new JPanel();
		frame.add(panel, BorderLayout.NORTH);

		// Logging Text Area:
		logging = new JTextArea();

		logging.setDisabledTextColor(Color.black);
		JScrollPane scrollPane = new JScrollPane(logging);
		logging.setBackground(Color.white);
		logging.setBorder(BorderFactory.createLoweredBevelBorder());
		logging.setEnabled(false);
		scrollPane.getViewport().setPreferredSize(new Dimension(600, 200));

		frame.add(scrollPane, BorderLayout.SOUTH);

		// ******************************************************************
		// MOUSE SCROLL ZOOM:
		// Implementing zoom with the scroll on mouse
		// ******************************************************************

		frame.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {

				int notches = e.getWheelRotation();
				if (notches < 0) {
					scale = scale + 1;
				} else {
					if (scale > 1) {
						scale = scale - 1;
					}
				}

				drawing.repaint();
			}
		});

		// ******************************************************************
		// BUTTONS:
		// Creating buttons for zoom, reset, open and quit
		// ******************************************************************

		// class button
		JButton classr = new JButton("Distance");
		panel.add(classr);
		classr.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				mode = "Distance";
				logging.append("\nCurrent Mode: " + mode);
				drawing.repaint();
			}
		});

		// Speed button
		JButton speed = new JButton("Speed");
		panel.add(speed);
		speed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				mode = "Speed";
				logging.append("\nCurrent Mode: " + mode);
				drawing.repaint();
			}
		});

		// Find articulation points button
		JButton articPoints = new JButton("Articulation Points");
		panel.add(articPoints);
		articPoints.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (hasFile) {
					for(Node n: idToNodes.values()){
						n.setVisited(false);
					}
					
					for(Node n: idToNodes.values()){
						if(!n.getVisited()){
							findArticulationPoints(n);
						}
					}
					drawing.repaint();
				}
			}
		});

		// Open Map button
		JButton read = new JButton("Open");
		panel.add(read);
		read.addActionListener(new ActionListener() {

			// Need to process the file and display it

			public void actionPerformed(ActionEvent ev) {

				// ******************************************************************
				// Before reading in the file, make sure everything is cleared.
				// This avoids problems when opening another map when one is
				// already opened.
				// ******************************************************************

				drawing.repaint();
				highlightRoad = new ArrayList<String>();
				highlightNode = -1;
				highlightNode2 = -1;
				idToNodes.clear();
				aPoints.clear();
				restrictions.clear();
				Polygons.clear();
				highlightNodes.clear();
				highlightSegments.clear();
				roads.clear();
				segments.clear();
				logging.setText(null);
				scale = 1;
				offsetX = 250;
				offsetY = 220;
				hasFile = true;

				// Now start reading

				logging.append("Select directory. The files will automatically be read.\n");

				JFileChooser read = new JFileChooser();
				read.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				read.setAcceptAllFileFilterUsed(false);
				read.setDialogTitle("Select Directory");
				int returnVal = read.showOpenDialog(null);

				File dir = read.getSelectedFile();

				// The Files:
				File node = new File(dir + "/nodeID-lat-lon.tab");
				File road = new File(dir + "/roadID-roadInfo.tab");
				File segment = new File(dir + "/roadSeg-roadID-length-nodeID-nodeID-coords.tab");
				File Polygon = new File(dir + "/polygon-shapes.mp");
				File Restrictions = new File(dir + "/restrictions.tab");

				if (!node.exists()) {
					logging.append("\nThe file 'nodeID-lat-lon.tab' could not be found.");
				}
				if (!road.exists()) {
					logging.append("\nThe file 'roadID-roadInfo.tab' could not be found.");
				}
				if (!segment.exists()) {
					logging.append("\nThe file 'roadSeg-roadID-length-nodeID-nodeID-coords.tab' could not be found.");
				}
				if (!node.exists() || !road.exists() || !segment.exists()) {
					logging.append("\nERROR: You must select a directory which contains the required files.\n");
				}
				if (!Polygon.exists()) {
					logging.append("\nNote: Polygon file was not found. They will not be drawn.\n\n");
				}
				if (!Restrictions.exists()) {
					logging.append("Note: Restrictions file was not found.\n\n");
				}

				if (returnVal == JFileChooser.APPROVE_OPTION && node.exists() && road.exists() && segment.exists()) {

					logging.append("Loading...");

					// ******************************************************************
					// Reading Nodes
					// ******************************************************************
					try {
						BufferedReader scan = new BufferedReader(new FileReader(node));

						int id;
						double latitude;
						double longitude;

						while (true) {
							String s = scan.readLine();

							if (s == null) {
								break;
							}

							String sTrimmed = s.trim();
							String[] sArray = sTrimmed.split("\\s", -1);

							id = Integer.parseInt(sArray[0]);
							latitude = Double.parseDouble(sArray[1]);
							longitude = Double.parseDouble(sArray[2]);
							Node n = new Node(id, latitude, longitude);
							idToNodes.put(id, n);
						}

					} catch (IOException e) {
						e.printStackTrace();
					}

					// ******************************************************************
					// Reading Roads
					// ******************************************************************
					try {
						BufferedReader scanTwo = new BufferedReader(new FileReader(road));
						scanTwo.readLine();

						while (true) {
							String line = scanTwo.readLine();

							if (line == null) {
								break;
							}

							String[] values = line.split("\\t", -1);

							int id = Integer.parseInt(values[0]);
							int type = Integer.parseInt(values[1]);
							String label = values[2];
							String city = values[3];
							int oneWay = Integer.parseInt(values[4]);
							int speed = Integer.parseInt(values[5]);
							int roadClass = Integer.parseInt(values[6]);
							int notForCar = Integer.parseInt(values[7]);
							int notForPede = Integer.parseInt(values[8]);
							int notForByc = Integer.parseInt(values[9]);

							Road r = new Road(id, type, label, city, oneWay, speed, roadClass, notForCar, notForPede, notForByc);
							roads.put(r.getID(), r);
							if (!labelToRoads.containsKey(label)) {
								List<Road> roadList = new ArrayList<Road>();
								roadList.add(r);
								labelToRoads.put(label, roadList);
							} else {
								labelToRoads.get(label).add(r);
							}

							labels.add(label);
						}

						trieLabels = new Trie(labels);

					} catch (IOException e) {
						e.printStackTrace();
					}

					// ******************************************************************
					// READING ROAD SEGMENTS:
					// ******************************************************************
					try {
						BufferedReader scanThree = new BufferedReader(new FileReader(segment));
						scanThree.readLine();

						while (true) {
							String l = scanThree.readLine();

							if (l == null) {
								break;
							}

							String[] lArray = l.split("\\t", -1);

							int id = Integer.parseInt(lArray[0]);
							double length = Double.parseDouble(lArray[1]);
							int node1 = Integer.parseInt(lArray[2]);
							int node2 = Integer.parseInt(lArray[3]);

							List<Location> coords = new ArrayList<Location>();

							for (int i = 4; i < lArray.length; i += 2) {
								Double value = Double.parseDouble(lArray[i]);
								Double valueTwo = Double.parseDouble(lArray[i + 1]);
								Location loc = Location.newFromLatLon(value, valueTwo);
								coords.add(loc);
							}

							Segment seg = new Segment(id, length, node1, node2, coords);
							segments.add(seg);

							if (roads.get(seg.getID()).getOneWay() == 1) {
								idToNodes.get(node1).addSegment(seg);
							} else {
								if (idToNodes.containsKey(node1) || idToNodes.containsKey(node2)) {
									idToNodes.get(node1).addSegment(seg);
									idToNodes.get(node2).addSegment(seg);
								}
							}

							idToNodes.get(node1).addToAllSegments(seg);
							idToNodes.get(node2).addToAllSegments(seg);

							if (roads.containsKey(id)) {
								roads.get(id).addSegment(seg);
							}

							// Adding the neighbours of all the nodes
							idToNodes.get(seg.getFirstNode()).addNeighbourNode(idToNodes.get(seg.getSecondNode()));
							idToNodes.get(seg.getSecondNode()).addNeighbourNode(idToNodes.get(seg.getFirstNode()));
						}

					} catch (IOException e) {
						e.printStackTrace();
					}

					// ******************************************************************
					// READING Polygons:
					// ******************************************************************

					if (Polygon.exists()) {

						try {
							BufferedReader scanFour = new BufferedReader(new FileReader(Polygon));

							String type = "";
							String label = "";
							int endLevel = -1;
							int cityIdx = -1;
							List<Location> dataZero = new ArrayList<Location>();
							List<Location> dataOne = new ArrayList<Location>();
							Location loc = null;

							while (true) {
								String line = scanFour.readLine();

								if (line == null) {
									break;
								}

								if (line.startsWith("Type")) {
									String temp = line.replaceAll("=", " ");
									String tempTrim = temp.trim();
									String[] tempSplit = tempTrim.split("\\s", -1);

									type = tempSplit[1];
								} else if (line.startsWith("Label")) {
									String temp = line.replaceAll("=", " ");
									String tempTrim = temp.trim();
									String[] tempSplit = tempTrim.split("\\s", -1);

									for (int i = 1; i < tempSplit.length; i++) {
										if (label.length() < 1) {
											label = label + tempSplit[i];
										} else {
											label = label + " " + tempSplit[i];
										}
									}
								} else if (line.startsWith("EndLevel")) {
									String temp = line.replaceAll("=", " ");
									String tempTrim = temp.trim();
									String[] tempSplit = tempTrim.split("\\s", -1);

									endLevel = Integer.parseInt(tempSplit[1]);
								} else if (line.startsWith("CityIdx")) {
									String temp = line.replaceAll("=", " ");
									String tempTrim = temp.trim();
									String[] tempSplit = tempTrim.split("\\s", -1);

									cityIdx = Integer.parseInt(tempSplit[1]);
								} else if (line.startsWith("Data0")) {
									String temp = line.replaceAll("[^-.A-Za-z0-9 ]", " ");
									String tempTrim = temp.trim().replaceAll(" +", " ");
									;

									String[] tempSplit = tempTrim.split("\\s", -1);

									for (int i = 1; i < tempSplit.length; i += 2) {
										if (tempSplit[i].length() > 0) {
											double lat = Double.parseDouble(tempSplit[i]);
											double lon = Double.parseDouble(tempSplit[i + 1]);
											loc = Location.newFromLatLon(lat, lon);
											dataZero.add(loc);
										}
									}
								} else if (line.startsWith("Data1")) {
									String temp = line.replaceAll("[^-.A-Za-z0-9 ]", " ");
									String tempTrim = temp.trim().replaceAll(" +", " ");
									;

									String[] tempSplit = tempTrim.split("\\s", -1);

									for (int i = 1; i < tempSplit.length; i += 2) {
										if (tempSplit[i].length() > 0) {
											double lat = Double.parseDouble(tempSplit[i]);
											double lon = Double.parseDouble(tempSplit[i + 1]);
											loc = Location.newFromLatLon(lat, lon);
											dataOne.add(loc);
										}
									}
								} else if (line.startsWith("[END]")) {
									Polygon poly = new Polygon(type, label, endLevel, cityIdx, dataZero, dataOne);
									Polygons.add(poly);

									// Reset all data
									type = "";
									label = "";
									endLevel = -1;
									cityIdx = -1;
									dataZero = new ArrayList<Location>();
									dataOne = new ArrayList<Location>();
									loc = null;
								}
							}

						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					// ****************************************************************
					// Reading in Restrictions
					// ****************************************************************
					if (Restrictions.exists()) {
						try {
							BufferedReader scanFive = new BufferedReader(new FileReader(Restrictions));
							scanFive.readLine();

							while (true) {
								String line = scanFive.readLine();

								if (line == null) {
									break;
								}

								String[] lArray = line.split("\\t", -1);

								int nodeID1 = Integer.parseInt(lArray[0]);
								int roadID1 = Integer.parseInt(lArray[1]);
								int interInvolvedNode = Integer.parseInt(lArray[2]);
								int roadID2 = Integer.parseInt(lArray[3]);
								int nodeID2 = Integer.parseInt(lArray[4]);

								Restriction r = new Restriction(nodeID1, roadID1, interInvolvedNode, roadID2, nodeID2);

								if (!restrictions.containsKey(idToNodes.get(interInvolvedNode))) {
									List<Restriction> res = new ArrayList<Restriction>();
									res.add(r);
									restrictions.put(idToNodes.get(interInvolvedNode), res);
								} else {
									restrictions.get(idToNodes.get(interInvolvedNode)).add(r);
								}

							}

						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					topLeftestNodeLocation();
					drawing.repaint();
					logging.append("\nComplete.");
					logging.append("\nCurrent Mode: " + mode);
					hasFile = true;
				}

			}
		});

		// Exit button
		JButton button = new JButton("Exit");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				System.exit(0);
			}
		});

		// *********************************************************************************
		// TEXTBOX:
		// Create a textbox which has a drop down list when the user types in
		// something
		// The user can then select from the list or continue to type.
		// *********************************************************************************

		AutoSuggestionTextField<String> textField = new AutoSuggestionTextField<String>(logging);
		textField.setAutoSuggestor(new LabelAutoSuggestor(labels));

		textField.setPreferredSize(new Dimension(300, 20));
		textField.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXX");
		textField.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		textField.setSuggestionListener(new SuggestionListener<String>() {

			@Override
			public void onSuggestionSelected(String item) {
			}

			@Override
			public void onEnter(String query) {
				update(logging, query);
			}
		});

		// Add the word 'Road' next to the text box so users know what it does.
		panel.add(new JLabel("Road"));
		panel.add(textField);

		// ******************************************************************
		// MOUSE LISTENER:
		// Implementing the mouse listener so that users can click on the
		// intersections and look up information about them.
		// ******************************************************************

		drawing.addMouseListener(new MouseAdapter() {

			int clickedX = 0;
			int clickedY = 0;

			boolean found = false;

			public void mousePressed(MouseEvent e) {
				pressedX = e.getX();
				pressedY = e.getY();
			}

			public void mouseClicked(MouseEvent e) {
				clickedX = e.getX();
				clickedY = e.getY();

				Location mouseClick = new Location(clickedX, clickedY);

				aPoints.clear();

				// Unhighlight all the highlighted roads from one node to
				// another.
				if (aSearched) {
					aSearched = false;
					highlightRoad.clear();
					highlightSegments.clear();
					highlightNodes.clear();
					highlightNode = -1;
					highlightNode2 = -1;
				}

				// Check if the click was close to any intersections.
				// If so, print out information about the intersection.

				for (Node n : idToNodes.values()) {
					Location thisNode = new Location(n.getX(), n.getY());
					if (mouseClick.closeTo(thisNode, 2)) {

						// If the highlightNode is -1, then use that one else
						// use highlightNode2. This means you can highlight
						// two nodes at the same time. If the user reclicks on a
						// current highlighted node, they can unhighlight it.

						if (n.getID() == highlightNode) {
							highlightNode = -1;
							break;
						} else if (n.getID() == highlightNode2) {
							highlightNode2 = -1;
							break;
						} else if (highlightNode != -1) {
							highlightNode2 = n.getID();
						} else {
							highlightNode = n.getID();
						}

						found = true;
						logging.append("\n\nIntersection Information:");
						logging.append("\n-----------------------------------------------------");
						logging.append("\nID: " + n.getID());
						logging.append("\n\nRoads:\n");

						Set<String> noDuplicates = new HashSet<String>();

						for (Segment i : n.getSegments()) {
							for (Road r : roads.values()) {
								if (i.getID() == r.getID()) {
									if (!noDuplicates.contains(r.getLabel())) {
										logging.append("- " + r.getLabel() + "\n");
									}

									noDuplicates.add(r.getLabel());
								}
							}
						}

						noDuplicates.clear();

						logging.append("-----------------------------------------------------\n");
					}
				}

				if (mode.equals("Distance")) {
					aStarSearchDistance();
				} else {
					aStarSearchSpeed();
				}

				drawing.repaint();
			}
		});

		// ******************************************************************
		// MOUSE PANNING:
		// Implementing a mouse pan so that users can click on the screen
		// and drag it around to look at the map.
		// ******************************************************************

		drawing.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				finalX = pressedX;
				finalY = pressedY;

				pressedX = e.getX();
				pressedY = e.getY();

				offsetX += pressedX - finalX;
				offsetY += pressedY - finalY;

				drawing.repaint();
			}

			@Override
			public void mouseMoved(MouseEvent e) {
			}

		});

		// Once it's all set up. Set the frame visibility to true.
		frame.setVisible(true);
	}

	/*
	 * Updates the graphics panel.
	 */
	public void redraw(Graphics g) {
		// Polygons
		for (Polygon p : Polygons) {
			p.draw(g, topLeft, scale, offsetX, offsetY);
		}

		// Nodes and segments in each node
		for (Node n : idToNodes.values()) {
			n.draw(g, topLeft, scale, offsetX, offsetY, new Color(0, 90, 255));
			for (Segment s : n.getSegments()) {
				if (roads.containsKey(s.getID())) {
					if (highlightRoad.contains(roads.get(s.getID()).getLabel())) {
						s.draw(g, topLeft, scale, offsetX, offsetY, idToNodes, Color.red);
					} else {
						s.draw(g, topLeft, scale, offsetX, offsetY, idToNodes, new Color(0, 90, 255));
					}
				}
			}
		}

		// Route segment highlighting
		for (Segment s : highlightSegments) {
			s.draw(g, topLeft, scale, offsetX, offsetY, idToNodes, Color.red);
		}

		for (Integer n : highlightNodes) {
			idToNodes.get(n).draw(g, topLeft, scale, offsetX, offsetY, Color.green);
		}

		// Highlight articulation points
		for (Node n : aPoints) {
			n.draw(g, topLeft, scale, offsetX, offsetY, Color.green);
		}

		// Highlight Node:
		if (idToNodes.containsKey(highlightNode)) {
			idToNodes.get(highlightNode).draw(g, topLeft, scale, offsetX, offsetY, Color.red);
		}

		if (idToNodes.containsKey(highlightNode2)) {
			idToNodes.get(highlightNode2).draw(g, topLeft, scale, offsetX, offsetY, Color.red);
		}

		// System.out.println("APoints size: "+aPoints.size());
	}

	/*
	 * Finds the top leftest node so that we can use it's location and calculate
	 * the X and Y coordinates for all nodes from it. This makes sure all nodes
	 * are on the screen so we can see them when they are drawn.
	 */
	public void topLeftestNodeLocation() {
		Node topLeftest = new Node(999999999, 999999999, 999999999);

		for (Node n : idToNodes.values()) {
			if (n.getLatitude() < topLeftest.getLatitude() && n.getLongitude() < topLeftest.getLongitude()) {
				topLeftest = n;
			}
		}

		Location n = Location.newFromLatLon(topLeftest.getLatitude(), topLeftest.getLongitude());

		topLeft = n;
	}

	/*
	 * Updates the graphics so that users can see the changes, e.g When clicking
	 * on a intersection it should highlight. It will also print out information
	 * about the selected intersection.
	 */
	public void update(JTextArea logging, String t) {
		String entry = t.toLowerCase();
		Boolean found = false;

		// listMode.clear();

		if (trieLabels.containsPrefix(entry)) {
			highlightRoad.clear();
			highlightRoad.add(entry);
			found = true;

			if (trieLabels.containsWord(entry)) {
				// Print out facts about road
				for (Road r : labelToRoads.get(entry)) {
					logging.append("\n\nRoad Information:");
					logging.append("\n-----------------------------------------------------");
					logging.append(r.toString());
					logging.append("\n-----------------------------------------------------\n");
				}
			}

			if (!found) {
				// Unhighlights the road
				highlightRoad = new ArrayList<String>();
			}

			drawing.repaint();
		}
	}

	/*
	 * Articulation points
	 */
	public void findArticulationPoints(Node node) {

		// First set the depth of all nodes to 0
		for (Node n : idToNodes.values()) {
			n.setDepth(Integer.MAX_VALUE);
		}

		// Clear articulation points list
		aPoints.clear();

		Node start = node;
		int numSubtrees = 0;

		start.setDepth(0);

		// Look at all neighbours
		for (Node n : start.getNeighbourNodes()) {
			if (n.getDepth() == Integer.MAX_VALUE) {
				recArtPts(n, 1, start);
				numSubtrees++;
			}
		}

		if (numSubtrees > 1) {
			aPoints.add(start);
		}
	}

	public int recArtPts(Node node, int depth, Node fromNode) {

		node.setDepth(depth);
		node.setVisited(true);
		int reachBack = depth;
		int childReach = 0;

		for (Node n : node.getNeighbourNodes()) {
			if (n.getID() != fromNode.getID()) {
				if (n.getDepth() < Integer.MAX_VALUE) {
					reachBack = Math.min(n.getDepth(), reachBack);
				} else {
					childReach = recArtPts(n, depth + 1, node);
					reachBack = Math.min(childReach, reachBack);
					if (childReach >= depth) {
						aPoints.add(node);
					}
				}
			}
		}

		return reachBack;
	}

	/*
	 * A* Search Algorithm attempt...
	 */
	public void aStarSearchDistance() {
		if (!idToNodes.isEmpty() && highlightNode > -1 && highlightNode2 > -1) {
			aSearched = true;

			// The speed heuristic to get from the start to the end node
			double startToEnd = idToNodes.get(highlightNode).getLocation().distanceTo(idToNodes.get(highlightNode2).getLocation());

			// Resetting and initialising the nodes.
			for (Node n : idToNodes.values()) {
				n.setVisited(false);
			}

			// Creating a priority queue to store them.
			Comparator<PriorityNode> comparator = new PriorityNodeComparator();
			PriorityQueue<PriorityNode> queue = new PriorityQueue<PriorityNode>(10, comparator);
			List<PriorityNode> pNodesVisited = new ArrayList<PriorityNode>();

			PriorityNode lastNode = null;

			// Put start node onto the queue
			queue.offer(new PriorityNode(idToNodes.get(highlightNode), null, 0, startToEnd));

			while (!queue.isEmpty()) {
				PriorityNode n = queue.poll();

				if (!n.getNode().getVisited()) {
					n.getNode().setVisited(true);

					if (lastNode != null) {
						n.setFromNode(lastNode);
					}

					if (n.getNode() == idToNodes.get(highlightNode2)) {
						// Reached goal
						// System.out.println("Reached goal: "+n.getNode().getID());
						lastNode = n;
						break;
					}

					// Iterate through all the neighbours of this node
					all: for (Segment s : n.getNode().getSegments()) {

						Node neighbour = null;

						if (n.getNode() == idToNodes.get(s.getFirstNode())) {
							neighbour = idToNodes.get(s.getSecondNode());
						} else {
							neighbour = idToNodes.get(s.getFirstNode());
						}
						
						// Testing restrictions
						if(n.getFromNode()!=null && restrictions.containsKey(n.getNode())){
							for(Restriction r: restrictions.get(n.getNode())){
								
								if(n.getFromNode().getNode().getID()==r.getNodeIDOne() && neighbour.getID()==r.getNodeIDTwo()
										&& s.getID()==r.getRoadIDTwo()){
									
									for(Segment seg: n.getNode().getAllSegments()){
										if((seg.getFirstNode()== n.getFromNode().getNode().getID() && seg.getSecondNode()==r.getInterNode() 
												&& seg.getID()==r.getRoadIDOne()) ||
												(seg.getSecondNode()== n.getFromNode().getNode().getID() && seg.getFirstNode()==r.getInterNode() 
												&& seg.getID()==r.getRoadIDOne())){
											
											break all;
										}
									}
								}
							}
						}

						if (!neighbour.getVisited()) {
							double costToNeighbour = n.getCostToHere() + s.getLength();
							double estTotal = costToNeighbour + (neighbour.getLocation().distanceTo(idToNodes.get(highlightNode2).getLocation()));

							PriorityNode p = new PriorityNode(neighbour, n, costToNeighbour, estTotal);
							queue.offer(p);

							pNodesVisited.add(p);

						}
					}
				}
			}

			// A list of all the segments so I can print out all the road
			// information onto the logging text area
			Map<String, Double> routeSegInfo = new HashMap<String, Double>();
			double distance = 0;

			// Highlight the nodes and the segments to get from start to
			// destination.
			List<PriorityNode> list = new ArrayList<PriorityNode>();

			if (lastNode != null) {
				list.add(lastNode);
				highlightNodes.add(lastNode.getNode().getID());

				while (lastNode.getFromNode() != null) {
					list.add(lastNode.getFromNode());
					highlightNodes.add(lastNode.getFromNode().getNode().getID());
					lastNode = lastNode.getFromNode();
				}

				for (int i = 0; i < list.size() - 1; i++) {
					for (Segment s : list.get(i).getNode().getAllSegments()) {
						if (list.get(i).getNode().getID() == s.getFirstNode() && list.get(i + 1).getNode().getID() == s.getSecondNode()) {
							highlightSegments.add(s);

							if (!routeSegInfo.containsKey(roads.get(s.getID()).getLabel())) {
								routeSegInfo.put(roads.get(s.getID()).getLabel(), s.getLength());
							} else {
								double length = routeSegInfo.get(roads.get(s.getID()).getLabel()) + s.getLength();
								routeSegInfo.put(roads.get(s.getID()).getLabel(), length);
							}

							// highlightRoad.add(roads.get(s.getID()).getLabel());
						} else if (list.get(i + 1).getNode().getID() == s.getFirstNode() && list.get(i).getNode().getID() == s.getSecondNode()) {

							highlightSegments.add(s);

							if (!routeSegInfo.containsKey(roads.get(s.getID()).getLabel())) {
								routeSegInfo.put(roads.get(s.getID()).getLabel(), s.getLength());
							} else {
								double length = routeSegInfo.get(roads.get(s.getID()).getLabel()) + s.getLength();
								routeSegInfo.put(roads.get(s.getID()).getLabel(), length);
							}

							// highlightRoad.add(roads.get(s.getID()).getLabel());
						}
					}
				}

				// Iterate through routeSegInfo and print out all the segment
				// name and the length. Then at the end, print out the total
				// distance.
				logging.append("Distance Information:\n");
				for (String s : routeSegInfo.keySet()) {
					logging.append("\n" + s + ": " + (Math.round(routeSegInfo.get(s) * 100)) / 100.0 + "km");
					distance += routeSegInfo.get(s);
				}

				logging.append("\n\nTotal Distance = " + (Math.round(distance * 100)) / 100.0 + "km");
				logging.append("\n-----------------------------------------------------\n");

				// Reset all from nodes in the Priority Nodes
				for (PriorityNode p : pNodesVisited) {
					p.setFromNode(null);
				}

				for (PriorityNode p : list) {
					p.setFromNode(null);
				}
			}

			if (lastNode == null) {
				logging.append("A route could not be found between these two intersections.");
			}
		}
	}

	public void aStarSearchSpeed() {
		if (!idToNodes.isEmpty() && highlightNode > -1 && highlightNode2 > -1) {
			aSearched = true;

			// The cost from the start to end. Only used for the first node. WE
			// WANT THE TIME WHICH IS: TIME = DISTANCE/SPEED
			double startToEnd = (idToNodes.get(highlightNode).getLocation().distanceTo(idToNodes.get(highlightNode2).getLocation()) / (6) * getModifier(4));

			// Resetting and initialising the nodes.
			for (Node n : idToNodes.values()) {
				n.setVisited(false);
				// Set PriorityNodes 'from' to null?
			}

			// Creating a priority queue to store them.
			Comparator<PriorityNode> comparator = new PriorityNodeComparator();
			PriorityQueue<PriorityNode> queue = new PriorityQueue<PriorityNode>(10, comparator);
			List<PriorityNode> pNodesVisited = new ArrayList<PriorityNode>();

			PriorityNode lastNode = null;

			// Put start node onto the queue
			queue.offer(new PriorityNode(idToNodes.get(highlightNode), null, 0, startToEnd));

			while (!queue.isEmpty()) {
				PriorityNode n = queue.poll();

				if (!n.getNode().getVisited()) {
					n.getNode().setVisited(true);

					if (lastNode != null) {
						n.setFromNode(lastNode);
					}

					if (n.getNode() == idToNodes.get(highlightNode2)) {
						// Reached goal
						// System.out.println("Reached goal: "+n.getNode().getID());
						lastNode = n;
						break;
					}

					// Iterate through all the neighbours of this node
					all: for (Segment s : n.getNode().getSegments()) {

						Node neighbour = null;

						if (n.getNode() == idToNodes.get(s.getFirstNode())) {
							neighbour = idToNodes.get(s.getSecondNode());
						} else {
							neighbour = idToNodes.get(s.getFirstNode());
						}


						// Testing restrictions
						if(n.getFromNode()!=null && restrictions.containsKey(n.getNode())){
							for(Restriction r: restrictions.get(n.getNode())){
								
								if(n.getFromNode().getNode().getID()==r.getNodeIDOne() && neighbour.getID()==r.getNodeIDTwo()
										&& s.getID()==r.getRoadIDTwo()){
									
									for(Segment seg: n.getNode().getAllSegments()){
										if((seg.getFirstNode()== n.getFromNode().getNode().getID() && seg.getSecondNode()==r.getInterNode() 
												&& seg.getID()==r.getRoadIDOne()) ||
												(seg.getSecondNode()== n.getFromNode().getNode().getID() && seg.getFirstNode()==r.getInterNode() 
												&& seg.getID()==r.getRoadIDOne())){
											
											break all;
										}
									}
								}
							}
						}

						if (!neighbour.getVisited()) {
							double costToNeighbour = n.getCostToHere() + (s.getLength() / (roads.get(s.getID()).getSpeed() * getModifier(roads.get(s.getID()).getRoadClass())));
							double estTotal = costToNeighbour + ((neighbour.getLocation().distanceTo(idToNodes.get(highlightNode2).getLocation())) / (roads.get(s.getID()).getSpeed() * getModifier(roads.get(s.getID()).getRoadClass())));

							PriorityNode p = new PriorityNode(neighbour, n, costToNeighbour, estTotal);
							queue.offer(p);

							pNodesVisited.add(p);

						}

					}
				}
			}

			// A list of all the segments so I can print out all the road
			// information onto the logging text area
			Map<String, Double> routeSegInfo = new HashMap<String, Double>();
			double distance = 0;

			// Highlight the nodes and the segments to get from start to
			// destination.
			List<PriorityNode> list = new ArrayList<PriorityNode>();

			if (lastNode != null) {

				list.add(lastNode);
				highlightNodes.add(lastNode.getNode().getID());

				while (lastNode.getFromNode() != null) {
					list.add(lastNode.getFromNode());
					highlightNodes.add(lastNode.getFromNode().getNode().getID());
					lastNode = lastNode.getFromNode();
				}

				for (int i = 0; i < list.size() - 1; i++) {
					for (Segment s : list.get(i).getNode().getAllSegments()) {
						if (list.get(i).getNode().getID() == s.getFirstNode() && list.get(i + 1).getNode().getID() == s.getSecondNode()) {
							highlightSegments.add(s);

							if (!routeSegInfo.containsKey(roads.get(s.getID()).getLabel())) {
								routeSegInfo.put(roads.get(s.getID()).getLabel(), s.getLength());
							} else {
								double length = routeSegInfo.get(roads.get(s.getID()).getLabel()) + s.getLength();
								routeSegInfo.put(roads.get(s.getID()).getLabel(), length);
							}

							// highlightRoad.add(roads.get(s.getID()).getLabel());
						} else if (list.get(i + 1).getNode().getID() == s.getFirstNode() && list.get(i).getNode().getID() == s.getSecondNode()) {
							highlightSegments.add(s);

							if (!routeSegInfo.containsKey(roads.get(s.getID()).getLabel())) {
								routeSegInfo.put(roads.get(s.getID()).getLabel(), s.getLength());
							} else {
								double length = routeSegInfo.get(roads.get(s.getID()).getLabel()) + s.getLength();
								routeSegInfo.put(roads.get(s.getID()).getLabel(), length);
							}

							// highlightRoad.add(roads.get(s.getID()).getLabel());
						}
					}
				}

				// Iterate through routeSegInfo and print out all the segment
				// name and the length. Then at the end, print out the total
				// distance.
				logging.append("Distance Information:\n");
				for (String s : routeSegInfo.keySet()) {
					logging.append("\n" + s + ": " + (Math.round(routeSegInfo.get(s) * 100)) / 100.0 + "km");
					distance += routeSegInfo.get(s);
				}

				logging.append("\n\nTotal Distance = " + (Math.round(distance * 100)) / 100.0 + "km");
				logging.append("\n-----------------------------------------------------\n");

				// Reset all from nodes in the Priority Nodes
				for (PriorityNode p : pNodesVisited) {
					p.setFromNode(null);
				}

				for (PriorityNode p : list) {
					p.setFromNode(null);
				}
			}
			if (lastNode == null) {
				logging.append("A route could not be found between these two intersections.");
			}
		}
	}

	public double getModifier(int value) {
		double mod = 0.0;

		if (value == 0) {
			mod = 0.5;
		} else if (value == 1) {
			mod = 0.65;
		} else if (value == 2) {
			mod = 0.80;
		} else if (value == 3) {
			mod = 0.95;
		} else if (value == 4) {
			mod = 1.1;
		}

		return mod;
	}

	/*
	 * PriorityQueue comparator
	 */
	public class PriorityNodeComparator implements Comparator<PriorityNode> {

		@Override
		public int compare(PriorityNode x, PriorityNode y) {

			if (x.getTotalCostToGoal() < y.getTotalCostToGoal()) {
				return -1;
			}

			if (x.getTotalCostToGoal() > y.getTotalCostToGoal()) {
				return 1;
			}
			return 0;
		}
	}

	/*
	 * A main method that creates a CityDrawer object so that it will run.
	 */
	public static void main(String[] arguments) {
		CityDrawer d = new CityDrawer();
	}

	// ***************************************************************************
	// AutoSuggestionTextField class:
	// Put in here so I can resolve textbox issues
	// ***************************************************************************
	@SuppressWarnings("serial")
	public class AutoSuggestionTextField<E> extends JComboBox {

		private AutoSuggestor<E> suggestor;
		private SuggestionListener<E> listener;
		private String previousText = "";

		private List<E> currentSuggestions = new ArrayList<E>();
		private boolean suppressEvents = false;
		private JTextArea logging = null;

		public AutoSuggestionTextField(JTextArea logging) {
			super();

			this.logging = logging;

			// remove arrow
			setUI(new BasicComboBoxUI() {
				@Override
				protected JButton createArrowButton() {
					return new JButton() {
						@Override
						public int getWidth() {
							return 0;
						}
					};
				}

				@Override
				public void setPopupVisible(JComboBox c, boolean v) {
					// keeps the popup from coming down if there's nothing in
					// the combo box
					if (c.getItemCount() > 0) {
						super.setPopupVisible(c, v);
					} else {
						super.setPopupVisible(c, false);
					}
				}
			});

			// properties and components
			setEditable(true);
			ItemSelectionListener lstnr = new ItemSelectionListener();
			addActionListener(lstnr);
			addItemListener(lstnr);
			textField = (JTextField) getEditor().getEditorComponent();
			textField.addKeyListener(new TextListener());
		}

		public AutoSuggestor<E> getAutoSuggestor() {
			return suggestor;
		}

		public void setAutoSuggestor(AutoSuggestor<E> suggestor) {
			this.suggestor = suggestor;
		}

		public SuggestionListener<E> getSuggestionListener() {
			return listener;
		}

		public void setSuggestionListener(SuggestionListener<E> listener) {
			this.listener = listener;
		}

		private class TextListener implements KeyListener {

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				String text = textField.getText();

				if (text.equals(previousText))
					return;

				previousText = text;

				suppressEvents = true;

				if (getItemCount() != 0)
					removeAllItems();

				// populate drop down
				currentSuggestions = suggestor.getSuggestions(text);
				for (E item : currentSuggestions) {
					if (!highlightOne.contains(String.valueOf(item))) {
						addItem(item);
					}

					highlightOne.add(String.valueOf(item));
				}

				setPopupVisible(false);

				if (!currentSuggestions.isEmpty())
					setPopupVisible(true);

				setSelectedItem(text);

				suppressEvents = false;

				update(logging);
				highlightOne.clear();
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			public void update(JTextArea logging) {
				String entry = textField.getText().toLowerCase();
				Boolean found = false;

				if (hasFile && entry.length() > 0 && trieLabels.containsPrefix(entry)) {
					highlightRoad.clear();
					highlightRoad.add(entry);
					found = true;

					if (trieLabels.containsWord(entry)) {
						// Print out facts about road
						for (Road r : labelToRoads.get(entry)) {
							// Print out facts about road
							logging.append("\n\nRoad Information:");
							logging.append("\n-----------------------------------------------------");
							logging.append(r.toString());
							logging.append("\n-----------------------------------------------------\n");
						}
					}
				}

				if (!found) {
					// Unhighlights the road
					highlightRoad = new ArrayList<String>();
				}

				if (found && highlightOne.size() == 1) {
					for (String r : highlightOne) {
						highlightRoad.clear();
						highlightRoad.add(r);
						// System.out.println("auto");

						if (trieLabels.containsWord(r)) {
							for (Road q : roads.values()) {
								if (q.getLabel().equals(r)) {
									logging.append("\n\nRoad Information:");
									logging.append("\n-----------------------------------------------------");
									logging.append(q.toString());
									logging.append("\n-----------------------------------------------------\n");
								}
							}
						}
					}
				}

				drawing.repaint();
			}
		}

		private class ItemSelectionListener implements ItemListener, ActionListener {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (suppressEvents)
					return;

				if (e.getStateChange() == ItemEvent.SELECTED)
					passEvent();
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				if (suppressEvents)
					return;

				if (e.getActionCommand().equals("comboBoxEdited"))
					passEvent();
			}

			@SuppressWarnings("unchecked")
			private void passEvent() {
				if (listener == null)
					return;

				Object item = getSelectedItem();
				if (item instanceof String) {
					listener.onEnter((String) item);
				} else {
					listener.onSuggestionSelected((E) item);
				}
			}
		}
	}

}
