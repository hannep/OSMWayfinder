import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

import org.w3c.dom.*;
import org.xml.sax.*;

/*
 * Parses OSM Files using XPath. 
 */

public class OSMParser {
	XPath xpath;
	Document doc;

	public static class OSMParserData {
		public HashMap<String, MapNode> mapNodeHash;
		public HashMap<String, MapWay> mapWayHash;

	}

	private OSMParser(String filename) throws ParserConfigurationException,
			SAXException, IOException {
		XPathFactory xpathFactory = XPathFactory.newInstance();
		xpath = xpathFactory.newXPath();
		DocumentBuilderFactory DOMfactory = DocumentBuilderFactory
				.newInstance();
		DOMfactory.setNamespaceAware(true);
		DocumentBuilder builder = DOMfactory.newDocumentBuilder();
		doc = builder.parse(filename);
	}

	public static OSMParserData parse(String filename)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {
		OSMParser parser = new OSMParser(filename);
		OSMParserData parserData = new OSMParserData();
		// System.out.println("Getting nodes.");
		parserData.mapNodeHash = parser.getNodes();
		System.out.println("There are " + parser.getNodes().size() + " nodes.");
		// System.out.println("Getting ways.");
		parserData.mapWayHash = parser.getWays(parserData.mapNodeHash);
		// System.out.println("Done getting ways.");
		return parserData;

	}

	HashMap<String, MapNode> getNodes() throws XPathExpressionException {
		HashMap<String, MapNode> mapNodeHash = new HashMap<String, MapNode>();
		NodeList nodeList = (NodeList) xpath.evaluate("//node", doc,
				XPathConstants.NODESET);
		for (int i = 0; i < nodeList.getLength(); i++) {
			NamedNodeMap map = nodeList.item(i).getAttributes();
			String id = map.getNamedItem("id").getNodeValue();
			double latitude = Double.parseDouble(map.getNamedItem("lat")
					.getNodeValue());
			double longitude = Double.parseDouble(map.getNamedItem("lon")
					.getNodeValue());
			MapNode mapNode = new MapNode(id, latitude, longitude);
			mapNodeHash.put(id, mapNode);
		}
		return mapNodeHash;
	}

	HashMap<String, MapWay> getWays(HashMap<String, MapNode> mapNodeHash)
			throws XPathExpressionException {
		HashMap<String, MapWay> wayHash = new HashMap<String, MapWay>();
		NodeList nodeList = (NodeList) xpath.evaluate("//way", doc,
				XPathConstants.NODESET);
		for (int i = 0; i < nodeList.getLength(); i++) {
			// System.out.println("Getting way " + i + " of "
			// + nodeList.getLength());
			Node wayNode = nodeList.item(i);
			ArrayList<String> wayNodeRefs = getRefsFromWayNode(wayNode);
			// NodeList refAttributeList = (NodeList) refExpression.evaluate(
			// wayNode, XPathConstants.NODESET);
			ArrayList<MapNode> wayMapNodes = new ArrayList<MapNode>();
			for (String wayNodeRef : wayNodeRefs) {
				// TODO: Try Guava for list comprehensions next time.
				wayMapNodes.add(mapNodeHash.get(wayNodeRef));
			}
			NamedNodeMap map = wayNode.getAttributes();
			String id = map.getNamedItem("id").getNodeValue();
			String name = getNameFromWayNode(wayNode);
			MapWay.Type type = getTypeFromWayNode(wayNode);
			wayHash.put(id, new MapWay(id, name, type, wayMapNodes));

		}
		return wayHash;
	}

	// Note: Java's XPath queries suck. Oops. Replacing with this.
	ArrayList<String> getRefsFromWayNode(Node wayNode) {
		NodeList children = wayNode.getChildNodes();
		ArrayList<String> wayRefs = new ArrayList<String>();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeName().equals("nd")) {
				wayRefs.add(children.item(i).getAttributes()
						.getNamedItem("ref").getNodeValue());
			}
		}
		return wayRefs;
	}

	String getNameFromWayNode(Node wayNode) {
		NodeList children = wayNode.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeName().equals("tag")
					&& node.getAttributes().getNamedItem("k").getNodeValue()
							.equals("name")) {
				return node.getAttributes().getNamedItem("v").getNodeValue();
			}
		}
		return null;
	}

	// Gets details about the way, for drawing
	MapWay.Type getTypeFromWayNode(Node wayNode) {
		NodeList children = wayNode.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeName().equals("tag")
					&& node.getAttributes().getNamedItem("k").getNodeValue()
							.equals("highway")) {
				if (node.getAttributes().getNamedItem("v").getNodeValue()
						.equals("primary")
						|| node.getAttributes().getNamedItem("v")
								.getNodeValue().equals("trunk")
						|| node.getAttributes().getNamedItem("v")
								.getNodeValue().equals("trunk_link")) {
					return MapWay.Type.HIGHWAY_PRIMARY;
				}
				if (node.getAttributes().getNamedItem("v").getNodeValue()
						.equals("residential")) {
					return MapWay.Type.HIGHWAY_RESIDENTIAL;
				}
				return MapWay.Type.HIGHWAY_SECONDARY;
			}
			if (node.getNodeName().equals("tag")
					&& node.getAttributes().getNamedItem("k").getNodeValue()
							.equals("natural")) {
				if (node.getAttributes().getNamedItem("v").getNodeValue()
						.equals("fell") || node.getAttributes().getNamedItem("v").getNodeValue()
						.equals("grassland")) {
					return MapWay.Type.NATURAL_GRASS;
				}
				if (node.getAttributes().getNamedItem("v").getNodeValue()
						.equals("scrub") || node.getAttributes().getNamedItem("v").getNodeValue()
						.equals("tree") || node.getAttributes().getNamedItem("v").getNodeValue()
						.equals("tree_row") || node.getAttributes().getNamedItem("v").getNodeValue()
						.equals("wood") || node.getAttributes().getNamedItem("v").getNodeValue()
						.equals("wetland")) {
					return MapWay.Type.NATURAL_LEAF;
				}
				if (node.getAttributes().getNamedItem("v").getNodeValue()
						.equals("cliff") || node.getAttributes().getNamedItem("v").getNodeValue()
						.equals("heath") || node.getAttributes().getNamedItem("v").getNodeValue()
						.equals("mud") || node.getAttributes().getNamedItem("v").getNodeValue()
						.equals("peak") || node.getAttributes().getNamedItem("v").getNodeValue()
						.equals("ridge") || node.getAttributes().getNamedItem("v").getNodeValue()
						.equals("rock")) {
					return MapWay.Type.NATURAL_ROCK;
				}
				if (node.getAttributes().getNamedItem("v").getNodeValue()
						.equals("bay") || node.getAttributes().getNamedItem("v").getNodeValue()
						.equals("coastline") || node.getAttributes().getNamedItem("v").getNodeValue()
						.equals("spring") || node.getAttributes().getNamedItem("v").getNodeValue()
						.equals("water")) {
					return MapWay.Type.NATURAL_WATER;
				}
				if (node.getAttributes().getNamedItem("v").getNodeValue()
						.equals("beach") || node.getAttributes().getNamedItem("v").getNodeValue()
						.equals("sand")) {
					return MapWay.Type.NATURAL_SAND;
				}
				if (node.getAttributes().getNamedItem("v").getNodeValue()
						.equals("glacier")) {
					return MapWay.Type.NATURAL_ICE;
				}
				
				
				
				return MapWay.Type.NATURAL; 
			}
			if (node.getNodeName().equals("tag")
					&& node.getAttributes().getNamedItem("k").getNodeValue()
							.equals("building")) {
				return MapWay.Type.BUILDING; 
			}
			if (node.getNodeName().equals("tag")
					&& node.getAttributes().getNamedItem("k").getNodeValue()
							.equals("waterway")) {
				return MapWay.Type.NATURAL_WATER; 
			}
			if (node.getNodeName().equals("tag")
					&& (node.getAttributes().getNamedItem("k").getNodeValue()
							.equals("operator") || node.getAttributes().getNamedItem("k").getNodeValue()
							.equals("power") || node.getAttributes().getNamedItem("k").getNodeValue()
							.equals("boundary") || node.getAttributes().getNamedItem("k").getNodeValue()
							.equals("landuse") || node.getAttributes().getNamedItem("k").getNodeValue()
							.equals("network") || node.getAttributes().getNamedItem("k").getNodeValue()
							.equals("sport"))) {
				return MapWay.Type.NOT_A_ROAD; 
			}

		}
		return MapWay.Type.NONE;
	}
}
