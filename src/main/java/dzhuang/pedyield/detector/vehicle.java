package dzhuang.pedyield.detector;

import java.awt.Polygon;
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

public class vehicle extends track {
	public static int fps = 60;
	public static int seconds_in_aoi = 6;

	public int theFirstFrame;
	public int theLastFrame;
	public int theFirstFrame_in_aoi;
	public int theLastFrame_in_aoi;
	public boolean valid;

	public vehicle(track t) throws ParserConfigurationException, SAXException, IOException {
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
						.parseInt(eElement.getElementsByTagName("seconds_in_aoi_vehicle").item(0).getTextContent());
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

			for (detection i : t.trajs) {
				if (aoi.intersects(i.bbox.left, i.bbox.top, i.position.w, i.position.h)) {
					if (this.theFirstFrame_in_aoi == -1) {
						this.theFirstFrame_in_aoi = i.frame;
					}
					this.theLastFrame_in_aoi = i.frame;
				}
			}

			if (this.theFirstFrame_in_aoi != -1 && this.theLastFrame_in_aoi != -1) {
				this.theLastFrame_in_aoi = this.theLastFrame_in_aoi + 1 <= this.theLastFrame
						? this.theLastFrame_in_aoi + 1
						: this.theLastFrame;
				if (this.theLastFrame_in_aoi - this.theFirstFrame_in_aoi <= fps * seconds_in_aoi)
					this.valid = true;
			}
		}
	}
}
