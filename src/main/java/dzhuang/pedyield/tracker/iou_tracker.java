package dzhuang.pedyield.tracker;

import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
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

public class iou_tracker {
	// for vehicle tracking

	public static String[] vehicle_type = { "bus", "car", "truck" };

	public static Bbox predictBbox(int frame_diff, track track_cur) {
		// to predict the next frame bounding box position
		int total_frame = track_cur.trajs.size();

		if (frame_diff == 0) {
			return track_cur.trajs.get(track_cur.trajs.size() - 1).bbox;
		}

		if (total_frame == 1) {
			return track_cur.trajs.get(0).bbox;
		} else {
			int left = 0;
			int top = 0;
			int right = 0;
			int bottom = 0;

			if (frame_diff >= total_frame - 1) {
				double w_max = Double.MIN_VALUE;
				double h_max = Double.MIN_VALUE;
				double avg_dis_move = 0.0;
				for (int i = total_frame - 1; i >= 1; i--) {
					double h1 = track_cur.trajs.get(i).position.h;
					double w1 = track_cur.trajs.get(i).position.w;
					double x1 = track_cur.trajs.get(i).position.x;
					double y1 = track_cur.trajs.get(i).position.y;

					double h2 = track_cur.trajs.get(i - 1).position.h;
					double w2 = track_cur.trajs.get(i - 1).position.w;
					double x2 = track_cur.trajs.get(i - 1).position.x;
					double y2 = track_cur.trajs.get(i - 1).position.y;

					avg_dis_move += Math.sqrt(Math.pow(x1 - x2, 2.0) + Math.pow(y1 - y2, 2.0));

					if (h1 > h_max)
						h_max = h1;
					if (h2 > h_max)
						h_max = h2;
					if (w1 > w_max)
						w_max = w1;
					if (w2 > w_max)
						w_max = w2;
				}
				avg_dis_move = avg_dis_move / (total_frame - 1);

				double x1_1 = track_cur.trajs.get(total_frame - 1).position.x;
				double y1_1 = track_cur.trajs.get(total_frame - 1).position.y;
				double x1_0 = track_cur.trajs.get(total_frame - 2).position.x;
				double y1_0 = track_cur.trajs.get(total_frame - 2).position.y;

				double x_p = 0.0;
				double y_p = 0.0;
				if (x1_1 != x1_0) {
					double m = (y1_1 - y1_0) / (x1_1 - x1_0);

					if (y1_1 - y1_0 > 0)
						y_p = y1_1 + Math.abs(avg_dis_move * m / Math.sqrt(1 + m * m));
					else
						y_p = y1_1 - Math.abs(avg_dis_move * m / Math.sqrt(1 + m * m));

					if (x1_1 - x1_0 > 0)
						x_p = x1_1 + Math.abs(avg_dis_move / Math.sqrt(1 + m * m));
					else
						x_p = x1_1 - Math.abs(avg_dis_move / Math.sqrt(1 + m * m));
				} else {
					x_p = x1_1;
					if (y1_1 - y1_0 > 0)
						y_p = y1_1 + avg_dis_move;
					else
						y_p = y1_1 - avg_dis_move;
				}

				int xmin = (int) (x_p - (w_max / 2.0));
				int xmax = (int) (x_p + (w_max / 2.0));
				int ymin = (int) (y_p - (h_max / 2.0));
				int ymax = (int) (y_p + (h_max / 2.0));

				left = xmin;
				top = ymin;
				right = xmax;
				bottom = ymax;
			} else if (frame_diff < total_frame - 1) {
				double w_max = Double.MIN_VALUE;
				double h_max = Double.MIN_VALUE;
				double avg_dis_move = 0.0;
				for (int i = total_frame - 1; i >= total_frame - frame_diff; i--) {
					double h1 = track_cur.trajs.get(i).position.h;
					double w1 = track_cur.trajs.get(i).position.w;
					double x1 = track_cur.trajs.get(i).position.x;
					double y1 = track_cur.trajs.get(i).position.y;

					double h2 = track_cur.trajs.get(i - 1).position.h;
					double w2 = track_cur.trajs.get(i - 1).position.w;
					double x2 = track_cur.trajs.get(i - 1).position.x;
					double y2 = track_cur.trajs.get(i - 1).position.y;

					avg_dis_move += Math.sqrt(Math.pow(x1 - x2, 2.0) + Math.pow(y1 - y2, 2.0));

					if (h1 > h_max)
						h_max = h1;
					if (h2 > h_max)
						h_max = h2;
					if (w1 > w_max)
						w_max = w1;
					if (w2 > w_max)
						w_max = w2;
				}
				avg_dis_move = avg_dis_move / (frame_diff);

				double x1_1 = track_cur.trajs.get(total_frame - 1).position.x;
				double y1_1 = track_cur.trajs.get(total_frame - 1).position.y;
				double x1_0 = track_cur.trajs.get(total_frame - 2).position.x;
				double y1_0 = track_cur.trajs.get(total_frame - 2).position.y;

				double x_p = 0.0;
				double y_p = 0.0;
				if (x1_1 != x1_0) {
					double m = (y1_1 - y1_0) / (x1_1 - x1_0);

					if (y1_1 - y1_0 > 0)
						y_p = y1_1 + Math.abs(avg_dis_move * m / Math.sqrt(1 + m * m));
					else
						y_p = y1_1 - Math.abs(avg_dis_move * m / Math.sqrt(1 + m * m));

					if (x1_1 - x1_0 > 0)
						x_p = x1_1 + Math.abs(avg_dis_move / Math.sqrt(1 + m * m));
					else
						x_p = x1_1 - Math.abs(avg_dis_move / Math.sqrt(1 + m * m));
				} else {
					x_p = x1_1;
					if (y1_1 - y1_0 > 0)
						y_p = y1_1 + avg_dis_move;
					else
						y_p = y1_1 - avg_dis_move;
				}

				int xmin = (int) (x_p - (w_max / 2.0));
				int xmax = (int) (x_p + (w_max / 2.0));
				int ymin = (int) (y_p - (h_max / 2.0));
				int ymax = (int) (y_p + (h_max / 2.0));

				left = xmin;
				top = ymin;
				right = xmax;
				bottom = ymax;

			}
			Bbox bbox = new Bbox(bottom, left, top, right);
			return bbox;
		}
	}

	public static LinkedHashMap<Integer, track> track_iou(LinkedHashMap<Integer, ArrayList<detection>> data,
			double sigma_l, double iou_vehicle_remove, double sigma_iou, double t_seconds, int fps, String output)
			throws SAXException, IOException, ParserConfigurationException {
		int TTL = (int) (t_seconds * fps);
		/************************************************************/
		// read in configure/configure.xml
		File fXmlFile = new File("configure/configure.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();

		// vehicle_type
		NodeList nList = doc.getElementsByTagName("vehicle_type");
		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node;
				vehicle_type = new String[eElement.getElementsByTagName("vtype").getLength()];
				for (int j = 0; j < eElement.getElementsByTagName("vtype").getLength(); j++) {
					vehicle_type[j] = eElement.getElementsByTagName("vtype").item(j).getTextContent();
				}
			}
		}

		// vehicle_init_area
		int npoints_vehicle_init = -1;
		int[] xpoints_vehicle_init = null;
		int[] ypoints_vehicle_init = null;
		nList = doc.getElementsByTagName("vehicle_init_area");
		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node;

				npoints_vehicle_init = Integer
						.parseInt(eElement.getElementsByTagName("npoints_vehicle_init").item(0).getTextContent());
				xpoints_vehicle_init = new int[npoints_vehicle_init];
				ypoints_vehicle_init = new int[npoints_vehicle_init];

				for (int j = 0; j < eElement.getElementsByTagName("point").getLength(); j++) {
					Node node_point = eElement.getElementsByTagName("point").item(j);
					if (node_point.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement_point = (Element) node_point;
						xpoints_vehicle_init[j] = Integer
								.parseInt(eElement_point.getElementsByTagName("x").item(0).getTextContent());
						ypoints_vehicle_init[j] = Integer
								.parseInt(eElement_point.getElementsByTagName("y").item(0).getTextContent());
					}
				}
			}
		}

		// vehicle_valid_area
		int npoints_vehicle_valid = -1;
		int[] xpoints_vehicle_valid = null;
		int[] ypoints_vehicle_valid = null;
		nList = doc.getElementsByTagName("vehicle_valid_area");
		for (int i = 0; i < nList.getLength(); i++) {
			Node node = nList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node;

				npoints_vehicle_valid = Integer
						.parseInt(eElement.getElementsByTagName("npoints_vehicle_valid").item(0).getTextContent());
				xpoints_vehicle_valid = new int[npoints_vehicle_valid];
				ypoints_vehicle_valid = new int[npoints_vehicle_valid];

				for (int j = 0; j < eElement.getElementsByTagName("point").getLength(); j++) {
					Node node_point = eElement.getElementsByTagName("point").item(j);
					if (node_point.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement_point = (Element) node_point;
						xpoints_vehicle_valid[j] = Integer
								.parseInt(eElement_point.getElementsByTagName("x").item(0).getTextContent());
						ypoints_vehicle_valid[j] = Integer
								.parseInt(eElement_point.getElementsByTagName("y").item(0).getTextContent());
					}
				}
			}
		}

		Polygon vehicle_init_area = new Polygon(xpoints_vehicle_init, ypoints_vehicle_init, npoints_vehicle_init);
		Polygon vehicle_valid_area = new Polygon(xpoints_vehicle_valid, ypoints_vehicle_valid, npoints_vehicle_valid);
		/************************************************************/

		HashSet<String> objs_vehicle_type = new HashSet<String>(Arrays.asList(vehicle_type));

		LinkedHashMap<Integer, track> tracks_active = new LinkedHashMap<Integer, track>();
		LinkedHashMap<Integer, track> tracks_finished = new LinkedHashMap<Integer, track>();
		int cnt = 0;
		for (Map.Entry<Integer, ArrayList<detection>> entry : data.entrySet()) {
			int frame_num = entry.getKey();
			ArrayList<detection> dets_org = entry.getValue();
			ArrayList<detection> dets_temporary = new ArrayList<detection>();

			for (detection i : dets_org) {
				if (i.prob >= sigma_l) {
					if (objs_vehicle_type.contains(i.type)) {
						Point2D pt_left_bottom = new Point2D.Double(i.bbox.left, i.bbox.bottom);
						if (vehicle_valid_area.contains(pt_left_bottom)) {
							dets_temporary.add(i);
						}
					}
				}
			}

			boolean flag = true;
			while (flag) {
				flag = false;
				ArrayList<Integer> dets_removed_index = new ArrayList<Integer>();
				for (int i = 0; i < dets_temporary.size(); i++) {
					for (int j = i + 1; j < dets_temporary.size(); j++) {
						Bbox b1 = new Bbox(dets_temporary.get(i).bbox);
						double prob1 = dets_temporary.get(i).prob;
						Bbox b2 = new Bbox(dets_temporary.get(j).bbox);
						double prob2 = dets_temporary.get(j).prob;

						double iou_help = util_tracker.iou(b1, b2);

						if (iou_help >= iou_vehicle_remove && objs_vehicle_type.contains(dets_temporary.get(i).type)
								&& objs_vehicle_type.contains(dets_temporary.get(j).type)) {
							flag = true;
							if (prob1 > prob2)
								dets_removed_index.add(j);
							else
								dets_removed_index.add(i);
						}
					}
				}

				Collections.sort(dets_removed_index, Collections.reverseOrder());
				for (int i : dets_removed_index) {
					dets_temporary.remove(i);
				}
			}

			ArrayList<detection> dets = new ArrayList<detection>();
			for (detection i : dets_temporary) {
				if (i.prob >= sigma_l) {
					dets.add(i);
				}
			}

			ArrayList<Integer> track_removed_index = new ArrayList<Integer>();
			for (int i : tracks_active.keySet()) {
				int frame_diff = frame_num - tracks_active.get(i).trajs.get(tracks_active.get(i).trajs.size() - 1).frame
						- 1;
				if (frame_diff <= TTL) {
					if (dets.size() > 0) {
						double max_iou = 0.0;
						int max_iou_det_index = -1;

						for (int j = 0; j < dets.size(); j++) {
							double iou = -1.0;

							Bbox bbox = predictBbox(frame_diff, tracks_active.get(i));
							iou = util_tracker.iou(dets.get(j).bbox, bbox);

							if (iou > max_iou) {
								max_iou = iou;
								max_iou_det_index = j;
							}
						}

						if (max_iou > 0.0 && max_iou >= sigma_iou) {
							tracks_active.get(i).trajs.add(dets.get(max_iou_det_index));
							dets.remove(max_iou_det_index);
						}
					}
				} else {
					tracks_finished.put(i, tracks_active.get(i));
					track_removed_index.add(i);
				}
			}

			Collections.sort(track_removed_index, Collections.reverseOrder());
			for (int i : track_removed_index) {
				tracks_active.remove(i);
			}

			for (detection k : dets) {
				if (objs_vehicle_type.contains(k.type)) {
					Point2D pt_left_bottom = new Point2D.Double(k.bbox.left, k.bbox.bottom);
					if (vehicle_init_area.contains(pt_left_bottom)) {
						track new_track = new track(cnt);
						new_track.trajs.add(k);
						tracks_active.put(new_track.Id, new_track);
						cnt++;
					}
				}
			}
		}

		for (int t : tracks_active.keySet()) {
			tracks_finished.put(t, tracks_active.get(t));
		}

		PrintWriter pw = new PrintWriter(output);

		for (Map.Entry<Integer, track> entry : tracks_finished.entrySet()) {
			track tr = entry.getValue();
			for (detection d : tr.trajs) {
				pw.println(d.frame + "," + tr.Id + "," + d.position.x + "," + d.position.y + "," + d.position.w + ","
						+ d.position.h);
			}
		}
		pw.close();
		return tracks_finished;
	}

	/**
	 * @param input: input directory
	 * @param sigma_l: lowest probability of the object detection results be be
	 *        considered, [0.3, 0.5]
	 * @param iou_vehicle_remove: threshold of vehicle's intersection of union in
	 *        the same frame to remove the duplicates
	 * @param sigma_radius: the distance threshold for each pedestrian object, [5.0,
	 *        20.0]
	 * @param t_seconds: the ttl seconds to handle the missing detections, [0.5,
	 *        1.5]
	 * @param fps: the fps of the video
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * 
	 */
	public static LinkedHashMap<Integer, track> track_iou(String input, double sigma_l, double iou_vehicle_remove,
			double sigma_iou, double t_seconds, int fps)
			throws IOException, SAXException, ParserConfigurationException { // function to be called, return a list of
		File input_f=new File(input);
		String dir=input_f.getParent();
		String fi=input_f.getName();
		String output="track_"+fi;
		if(dir!=null && !dir.isEmpty())
			output=dir+"/"+"track_"+fi;															// tracks
		LinkedHashMap<Integer, ArrayList<detection>> data = util_tracker.load_mot(input);
		LinkedHashMap<Integer, track> tracks = track_iou(data, sigma_l, iou_vehicle_remove, sigma_iou, t_seconds, fps,
				output);
		return tracks;
	}

	public static LinkedHashMap<Integer, track> track_iou(String input, double sigma_l, double iou_vehicle_remove,
			double sigma_iou, double t_seconds) throws IOException, SAXException, ParserConfigurationException {
		// function to be called, return a list of tracks
		int fps = -1;
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

		File input_f=new File(input);
		String dir=input_f.getParent();
		String fi=input_f.getName();
		String output="track_"+fi;
		if(dir!=null && !dir.isEmpty())
			output=dir+"/"+"track_"+fi;
		LinkedHashMap<Integer, ArrayList<detection>> data = util_tracker.load_mot(input);
		LinkedHashMap<Integer, track> tracks = track_iou(data, sigma_l, iou_vehicle_remove, sigma_iou, t_seconds, fps,
				output);
		return tracks;
	}

	public static LinkedHashMap<Integer, track> track_iou(String input, int fps)
			throws IOException, SAXException, ParserConfigurationException {
		File input_f=new File(input);
		String dir=input_f.getParent();
		String fi=input_f.getName();
		String output="track_"+fi;
		if(dir!=null && !dir.isEmpty())
			output=dir+"/"+"track_"+fi;
		// function to be called, return a list of tracks
		LinkedHashMap<Integer, ArrayList<detection>> data = util_tracker.load_mot(input);
		LinkedHashMap<Integer, track> tracks = track_iou(data, 0.3, 0.75, 0.5, 0.5, fps, output);
		return tracks;
	}

	public static LinkedHashMap<Integer, track> track_iou(String input)
			throws IOException, SAXException, ParserConfigurationException {
		File input_f=new File(input);
		String dir=input_f.getParent();
		String fi=input_f.getName();
		String output="track_"+fi;
		if(dir!=null && !dir.isEmpty())
			output=dir+"/"+"track_"+fi;
		// function to be called, return a list of tracks
		LinkedHashMap<Integer, ArrayList<detection>> data = util_tracker.load_mot(input);
		LinkedHashMap<Integer, track> tracks = track_iou(data, 0.3, 0.75, 0.5, 0.5, 60, output);
		return tracks;
	}
}
