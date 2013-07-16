import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.starkeffect.highway.GPSDevice;
import com.starkeffect.highway.GPSEvent;
import com.starkeffect.highway.GPSListener;

public class GPSDisplay {

	JFrame frame;
	JList mapDirectionsList;
	GPSMapPanel mapPanel;

	GPSPrinter gpsPrinter = new GPSPrinter();

	Graph graph;

	//double heading; 
	public GPSDisplay() throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException {
		setupGUI();

	}

	private void updateDirectionsList(ArrayList<MapNode> nodes) {

		if (nodes == null) {
			mapDirectionsList
					.setListData(new String[] { "No directions found" });
			return;

		}
		Vector<String> directionsVector = new Vector<String>();
		directionsVector.add(String.format("Origin: %s.", nodes.get(0)
				.getPoint()));
		for (int i = 0; i < nodes.size() - 1; i++) {
			MapWay commonWay = nodes.get(i).getCommonWay(nodes.get(i + 1));
			directionsVector.add(String.format("Go to %s via %s.",
					nodes.get(i + 1).getPoint(), commonWay));
			// "Go to" (i+1) via (way containing i and i+1)

		}
		directionsVector.add(String.format("Destination: %s.",
				nodes.get(nodes.size() - 1).getPoint()));
		mapDirectionsList.setListData(directionsVector);
		frame.getContentPane().validate();
	}

	private void setupGUI() throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException {
		frame = new JFrame("OSM");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container c = frame.getContentPane();
		c.setLayout(new BorderLayout());

		mapDirectionsList = new JList();

		JScrollPane scrollPane = new JScrollPane(mapDirectionsList);
		c.add(scrollPane, BorderLayout.WEST);

		mapPanel = new GPSMapPanel(this);
		c.add(mapPanel, BorderLayout.CENTER);

		JButton loadNewMap = new JButton("Load New Map");
		loadNewMap.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"OSM Files", "osm");
				chooser.setFileFilter(filter);
				int returnVal = chooser.showOpenDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					System.out.println("Loading file "
							+ chooser.getSelectedFile().getName());
					try {
						setupNewMap(chooser.getSelectedFile().getAbsolutePath());
					} catch (XPathExpressionException e) {
						JOptionPane.showMessageDialog(frame,
								"Error Parsing File");
						e.printStackTrace();
					} catch (ParserConfigurationException e) {
						JOptionPane.showMessageDialog(frame,
								"Error Parsing File");
						e.printStackTrace();
					} catch (SAXException e) {
						JOptionPane.showMessageDialog(frame,
								"Error Parsing File");
						e.printStackTrace();
					} catch (IOException e) {
						JOptionPane.showMessageDialog(frame,
								"Error Parsing File");
						e.printStackTrace();
					}
				}
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(loadNewMap);
		c.add(buttonPanel, BorderLayout.SOUTH);

		setupNewMap("usb.osm");

		frame.pack();
		frame.setBounds(0, 0, 1100, 768);
		frame.setVisible(true);

	}

	public void updatePathFromMapPanel() {
		MapNode startNode = mapPanel.getStartNode();
		MapNode endNode = mapPanel.getEndNode();

		ArrayList<MapNode> path = graph.getShortestPath(startNode, endNode);

		updateDirectionsList(path);
		mapPanel.setCurrentPath(path);
	}

	void setupNewMap(String filename) throws XPathExpressionException,
			ParserConfigurationException, SAXException, IOException {
		OSMParser.OSMParserData data = OSMParser.parse(filename);
		Collection<MapWay> waysCollection = data.mapWayHash.values();
		Object[] waysArray = waysCollection.toArray();
		GPSDevice gpsDevice = new GPSDevice(filename);
		Arrays.sort(waysArray);
		graph = new Graph(waysCollection);
		gpsDevice.addGPSListener(new GPSListener() {

			@Override
			public void processEvent(GPSEvent event) {
				GeoPoint current = new GeoPoint(event.getLatitude(), event
						.getLongitude());
				//heading = event.getHeading(); 
				mapPanel.setCurrentLocation(current);
				MapNode start = graph.getClosestGraphNode(current).getMapNode();
				mapPanel.setStartNode(start); 
			}
		});
		mapPanel.setWays(new ArrayList<MapWay>(waysCollection));
		mapPanel.repaint();
	}
}
