package dzhuang.pedyield.detector;

import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import dzhuang.pedyield.tracker.detection;
import dzhuang.pedyield.tracker.track;

public class pedestrian extends track {
	public static int fps = 60;
	public static int seconds_in_aoi = 20;

	public int theFirstFrame;
	public int theLastFrame;
	public int theFirstFrame_in_aoi;
	public int theLastFrame_in_aoi;
	public boolean valid;

	public pedestrian(track t) throws ParserConfigurationException, SAXException, IOException {
		super(t);

		// read in configure/configure.xml
		File fXmlFile = new File("configure/configure.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("conf");
		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node;
				fps = Integer.parseInt(eElement.getElementsByTagName("fps_int").item(0).getTextContent());
			}
		}

		nList = doc.getElementsByTagName("detector");
		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node;
				seconds_in_aoi = Integer
						.parseInt(eElement.getElementsByTagName("seconds_in_aoi_pedestrian").item(0).getTextContent());
			}
		}

		this.valid = false;
		if (!t.trajs.isEmpty()) {
			this.theFirstFrame = t.trajs.get(0).frame;
			this.theLastFrame = t.trajs.get(t.trajs.size() - 1).frame;

			int npoints = util_detector.AoI().npoints;
			int[] xpoints = util_detector.AoI().xpoints;
			int[] ypoints = util_detector.AoI().ypoints;
			Polygon aoi = new Polygon(xpoints, ypoints, npoints);
			this.theFirstFrame_in_aoi = -1;
			this.theLastFrame_in_aoi = -1;
			int theFirstFrame_in_aoi_bottom = -1;
			int theLastFrame_in_aoi_bottom = -1;
			int pivotal_y = (ypoints[1] + ypoints[3]) / 2;

			for (detection i : t.trajs) {
				Point2D pt = new Point2D.Double(i.bbox.left, i.bbox.bottom);
				if (aoi.contains(pt)) {
					if (this.theFirstFrame_in_aoi == -1) {
						this.theFirstFrame_in_aoi = i.frame;
						theFirstFrame_in_aoi_bottom = i.bbox.bottom;
					}
					this.theLastFrame_in_aoi = i.frame;
					theLastFrame_in_aoi_bottom = i.bbox.bottom;
				}
			}

			if (this.theFirstFrame_in_aoi != -1 && this.theLastFrame_in_aoi != -1 && theFirstFrame_in_aoi_bottom != -1
					&& theLastFrame_in_aoi_bottom != -1
					&& ((theFirstFrame_in_aoi_bottom > pivotal_y && theLastFrame_in_aoi_bottom <= pivotal_y)
							|| (theFirstFrame_in_aoi_bottom <= pivotal_y && theLastFrame_in_aoi_bottom > pivotal_y))) {
				this.theLastFrame_in_aoi = this.theLastFrame_in_aoi + 1 <= this.theLastFrame
						? this.theLastFrame_in_aoi + 1
						: this.theLastFrame;
				if (this.theLastFrame_in_aoi - this.theFirstFrame_in_aoi <= fps * seconds_in_aoi)
					this.valid = true;
			}
		}
	}
}
