package dzhuang.pedyield.detector;

import java.awt.Polygon;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import dzhuang.pedyield.tracker.iou_tracker;
import dzhuang.pedyield.tracker.radius_tracker;
import dzhuang.pedyield.tracker.track;

public class util_detector {

	public static Polygon AoI() throws ParserConfigurationException, SAXException, IOException {
/*		
 		int npoints = 4;
		int[] xpoints = new int[npoints];
		int[] ypoints = new int[npoints];

		xpoints[0] = 1350;
		ypoints[0] = 370;
		xpoints[1] = 1520;
		ypoints[1] = 485;

		xpoints[2] = 1420;
		ypoints[2] = 585;
		xpoints[3] = 1100;
		ypoints[3] = 487;

		Polygon aoi = new Polygon(xpoints, ypoints, npoints);
*/
		
		//read in configure/configure.xml
		File fXmlFile = new File("configure/configure.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
						
		// area_of_interest
		int npoints=-1;
		int[] xpoints=null;
		int[] ypoints=null;
		NodeList nList = doc.getElementsByTagName("area_of_interest");
		for(int i=0;i<nList.getLength();i++) {
			Node node = nList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node;
						
				npoints = Integer.parseInt(eElement.getElementsByTagName("npoints_area_of_interest").item(0).getTextContent());
				xpoints = new int[npoints];
				ypoints = new int[npoints];
				
				for(int j=0;j<eElement.getElementsByTagName("point").getLength();j++) {
					Node node_point = eElement.getElementsByTagName("point").item(j);
					if (node_point.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement_point = (Element) node_point;
						xpoints[j]=Integer.parseInt(eElement_point.getElementsByTagName("x").item(0).getTextContent());
						ypoints[j]=Integer.parseInt(eElement_point.getElementsByTagName("y").item(0).getTextContent());
					}
				}
			}
		}
		Polygon aoi = new Polygon(xpoints, ypoints, npoints);
		return aoi;
	}

	public static ArrayList<pedestrian> load_pedestrian_list(String input) throws IOException, ParserConfigurationException, SAXException {
		LinkedHashMap<Integer, track> pedestrian_tracks = radius_tracker.track_radius(input, 0.3, 10.0, 10.0, 1.0, 60, 125.0, 0.5, 10, 1.0);
		ArrayList<pedestrian> pedestrian_list = new ArrayList<pedestrian>();
		for (Map.Entry<Integer, track> t : pedestrian_tracks.entrySet()) {
			pedestrian p = new pedestrian(t.getValue());
			if (p.valid) {
				pedestrian_list.add(p);
			}
		}

		return pedestrian_list;
	}

	public static ArrayList<vehicle> load_vehicle_list(String input) throws IOException, SAXException, ParserConfigurationException {
		LinkedHashMap<Integer, track> vehicle_tracks = iou_tracker.track_iou(input, 0.3, 0.75, 0.5, 0.5, 60);
		ArrayList<vehicle> vehicle_list = new ArrayList<vehicle>();
		for (Map.Entry<Integer, track> t : vehicle_tracks.entrySet()) {
			vehicle v = new vehicle(t.getValue());
			if (v.valid) {
				vehicle_list.add(v);
			}
		}

		return vehicle_list;
	}
}
