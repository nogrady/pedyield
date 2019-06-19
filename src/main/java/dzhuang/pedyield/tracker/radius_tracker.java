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

public class radius_tracker { // for pedestrian tracking
	public static String[] pedestrian_type = { "person" };
	
	public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
		/*for (int i = 1; i <= 10; i++) {
			System.out.println("File - " + i);
			if (i < 10)
				track_radius("GH0" + i + "0228_2_1920-1080-59_0.5_0.3_ped.csv", 0.3, 10.0, 10.0, 1.0, 60, 125.0, 0.5, 10, 1.0);
			else
				track_radius("GH" + i + "0228_2_1920-1080-59_0.5_0.3_ped.csv", 0.3, 10.0, 10.0, 1.0, 60, 125.0, 0.5, 10, 1.0);
		}*/
	}

	public static double[] predict_radius_direction(int frame_diff, track track_cur, int watch_back, double ex_b) { // get the possible next direction for each pedestrian object
		// 0:x_1-x_0>0, y_1-y_0>0
		// 1:x_1-x_0>0, y_1-y_0=0
		// 2:x_1-x_0>0, y_1-y_0<0
		// 3:x_1-x_0=0, y_1-y_0>0
		// 4:x_1-x_0=0, y_1-y_0=0
		// 5:x_1-x_0=0, y_1-y_0<0
		// 6:x_1-x_0<0, y_1-y_0>0
		// 7:x_1-x_0<0, y_1-y_0=0
		// 8:x_1-x_0<0, y_1-y_0<0
		double attenuation_rate = 0.9; // between 0.8-1.0, do not need to change much
		double[] direction_prob = new double[9];
		for (int i = 0; i < direction_prob.length; i++) {
			direction_prob[i] = 0.0;
		}

		int total_frame = track_cur.trajs.size();

		// check the number of "watch_back" frames of the historical directions
		if (total_frame - 1 < watch_back) {
			int cnt = 0;
			for (int i = total_frame - 1; i >= 1; i--) {
				double x_1 = track_cur.trajs.get(i).position.x;
				double y_1 = track_cur.trajs.get(i).position.y;
				double x_0 = track_cur.trajs.get(i - 1).position.x;
				double y_0 = track_cur.trajs.get(i - 1).position.y;
				int tim = track_cur.trajs.get(i).frame - track_cur.trajs.get(i - 1).frame;

//				System.out.println(track_cur.trajs.get(i).frame+"\t"+track_cur.trajs.get(i-1).frame);
				cnt += tim;
				if (x_1 - x_0 > 0 && y_1 - y_0 > 0) {
					direction_prob[0] = direction_prob[0] + 1.0 * tim;
				} else if (x_1 - x_0 > 0 && y_1 - y_0 == 0) {
					direction_prob[1] = direction_prob[1] + 1.0 * tim;
				} else if (x_1 - x_0 > 0 && y_1 - y_0 < 0) {
					direction_prob[2] = direction_prob[2] + 1.0 * tim;
				} else if (x_1 - x_0 == 0 && y_1 - y_0 > 0) {
					direction_prob[3] = direction_prob[3] + 1.0 * tim;
				} else if (x_1 - x_0 == 0 && y_1 - y_0 == 0) {
					direction_prob[4] = direction_prob[4] + 1.0 * tim;
				} else if (x_1 - x_0 == 0 && y_1 - y_0 < 0) {
					direction_prob[5] = direction_prob[5] + 1.0 * tim;
				} else if (x_1 - x_0 < 0 && y_1 - y_0 > 0) {
					direction_prob[6] = direction_prob[6] + 1.0 * tim;
				} else if (x_1 - x_0 < 0 && y_1 - y_0 == 0) {
					direction_prob[7] = direction_prob[7] + 1.0 * tim;
				} else if (x_1 - x_0 < 0 && y_1 - y_0 < 0) {
					direction_prob[8] = direction_prob[8] + 1.0 * tim;
				}
			}

			for (int i = 0; i < direction_prob.length; i++) {
				direction_prob[i] = direction_prob[i] / (cnt + 1);
			}

		} else {
			int cnt = 0;
			for (int i = total_frame - 1; i >= total_frame - watch_back; i--) {
				double x_1 = track_cur.trajs.get(i).position.x;
				double y_1 = track_cur.trajs.get(i).position.y;
				double x_0 = track_cur.trajs.get(i - 1).position.x;
				double y_0 = track_cur.trajs.get(i - 1).position.y;
				int tim = track_cur.trajs.get(i).frame - track_cur.trajs.get(i - 1).frame;
				cnt += tim;
				if (x_1 - x_0 > 0 && y_1 - y_0 > 0) {
					direction_prob[0] = direction_prob[0] + 1.0 * tim;
				} else if (x_1 - x_0 > 0 && y_1 - y_0 == 0) {
					direction_prob[1] = direction_prob[1] + 1.0 * tim;
				} else if (x_1 - x_0 > 0 && y_1 - y_0 < 0) {
					direction_prob[2] = direction_prob[2] + 1.0 * tim;
				} else if (x_1 - x_0 == 0 && y_1 - y_0 > 0) {
					direction_prob[3] = direction_prob[3] + 1.0 * tim;
				} else if (x_1 - x_0 == 0 && y_1 - y_0 == 0) {
					direction_prob[4] = direction_prob[4] + 1.0 * tim;
				} else if (x_1 - x_0 == 0 && y_1 - y_0 < 0) {
					direction_prob[5] = direction_prob[5] + 1.0 * tim;
				} else if (x_1 - x_0 < 0 && y_1 - y_0 > 0) {
					direction_prob[6] = direction_prob[6] + 1.0 * tim;
				} else if (x_1 - x_0 < 0 && y_1 - y_0 == 0) {
					direction_prob[7] = direction_prob[7] + 1.0 * tim;
				} else if (x_1 - x_0 < 0 && y_1 - y_0 < 0) {
					direction_prob[8] = direction_prob[8] + 1.0 * tim;
				}
			}

			for (int i = 0; i < direction_prob.length; i++) {
				direction_prob[i] = direction_prob[i] / (cnt + 1); // the probability of each direction of the next move
			}
		}

		return softmax(direction_prob, frame_diff * attenuation_rate + 1, ex_b); // use softmax to normalize the probabilities
	}

	public static double predict_radius_threshold(int frame_diff, track track_cur, double sigma_radius) { // get the distance threshold for each pedestrian object
		int total_frame = track_cur.trajs.size();
		double attenuation_rate = 0.9; // between 0.8-1.0, do not need to change much

		if (frame_diff == 0) {
			if (total_frame == 1) {
				return sigma_radius;
			} else {
				double r = util_tracker.radius(track_cur.trajs.get(track_cur.trajs.size() - 1).position,
						track_cur.trajs.get(track_cur.trajs.size() - 2).position);
				int tim = track_cur.trajs.get(track_cur.trajs.size() - 1).frame
						- track_cur.trajs.get(track_cur.trajs.size() - 2).frame;
				r = r / tim; // moving distance of one previous frame

				return r > sigma_radius ? r : sigma_radius;
			}
		}

		if (total_frame == 1) {
			return sigma_radius * frame_diff * attenuation_rate; // no previous experience
		} else {
			double avg_dis_move = 0.0;
			if (frame_diff >= total_frame - 1) {
				for (int i = total_frame - 1; i >= 1; i--) {
					double radius_one_frame = util_tracker.radius(track_cur.trajs.get(i).position,
							track_cur.trajs.get(i - 1).position);
					int tim = track_cur.trajs.get(i).frame - track_cur.trajs.get(i - 1).frame;
					radius_one_frame = radius_one_frame / tim;
					avg_dis_move += radius_one_frame;
				}
				avg_dis_move = avg_dis_move / (total_frame - 1);
			} else if (frame_diff < total_frame - 1) {
				for (int i = total_frame - 1; i >= total_frame - frame_diff; i--) {
					double radius_one_frame = util_tracker.radius(track_cur.trajs.get(i).position,
							track_cur.trajs.get(i - 1).position);
					int tim = track_cur.trajs.get(i).frame - track_cur.trajs.get(i - 1).frame;
					radius_one_frame = radius_one_frame / tim;
					avg_dis_move += radius_one_frame;
				}
				avg_dis_move = avg_dis_move / (frame_diff);
			}
			double move_one_frame = avg_dis_move > sigma_radius ? avg_dis_move : sigma_radius; // moving distance of one frame by average

			return move_one_frame * frame_diff * attenuation_rate;
		}
	}

	public static double[] softmax(double[] z, double b, double ex_b) { // softmax distance function
		double sum = 0;
		for (int i = 0; i < z.length; i++) {
			sum += Math.exp(ex_b * b * z[i]);
		}

		double[] z_o = new double[z.length];
		for (int i = 0; i < z_o.length; i++) {
			z_o[i] = Math.exp(ex_b * b * z[i]) / sum;
		}

		return z_o;
	}

	public static LinkedHashMap<Integer, track> track_radius(LinkedHashMap<Integer, ArrayList<detection>> data,
			double sigma_l, double radius_pedestrian_remove, double sigma_radius, double t_seconds, int fps, double radius_ttl_limit, double direction_look_back_seconds, int direction_look_back_steps, double ex_b, String output)
			throws ParserConfigurationException, SAXException, IOException { // tracking algorithm
		int TTL = (int) (t_seconds * fps);
		direction_look_back_steps = (int) (direction_look_back_seconds * fps);

		/************************************************************/
/*
		int npoints_pedestrian_valid = 4;
		int[] xpoints_pedestrian_valid = new int[npoints_pedestrian_valid];
		int[] ypoints_pedestrian_valid = new int[npoints_pedestrian_valid];
		xpoints_pedestrian_valid[0] = 1400;
		ypoints_pedestrian_valid[0] = 290;
		xpoints_pedestrian_valid[1] = 1580;
		ypoints_pedestrian_valid[1] = 400;
		xpoints_pedestrian_valid[2] = 1130;
		ypoints_pedestrian_valid[2] = 1080;
		xpoints_pedestrian_valid[3] = 700;
		ypoints_pedestrian_valid[3] = 600;
		Polygon pedestrian_valid_area = new Polygon(xpoints_pedestrian_valid, ypoints_pedestrian_valid,
				npoints_pedestrian_valid);

		int npoints_pedestrian_init_top = 4;
		int[] xpoints_pedestrian_init_top = new int[npoints_pedestrian_init_top];
		int[] ypoints_pedestrian_init_top = new int[npoints_pedestrian_init_top];
		xpoints_pedestrian_init_top[0] = 1400;
		ypoints_pedestrian_init_top[0] = 290;
		xpoints_pedestrian_init_top[1] = 1580;
		ypoints_pedestrian_init_top[1] = 400;
		xpoints_pedestrian_init_top[2] = 1560;
		ypoints_pedestrian_init_top[2] = 500;
		xpoints_pedestrian_init_top[3] = 1300;
		ypoints_pedestrian_init_top[3] = 325;
		Polygon pedestrian_init_top_area = new Polygon(xpoints_pedestrian_init_top, ypoints_pedestrian_init_top,
				npoints_pedestrian_init_top);

		int npoints_pedestrian_init_bottom = 4;
		int[] xpoints_pedestrian_init_bottom = new int[npoints_pedestrian_init_bottom];
		int[] ypoints_pedestrian_init_bottom = new int[npoints_pedestrian_init_bottom];
		xpoints_pedestrian_init_bottom[0] = 1000;
		ypoints_pedestrian_init_bottom[0] = 450;
		xpoints_pedestrian_init_bottom[1] = 1480;
		ypoints_pedestrian_init_bottom[1] = 610;
		xpoints_pedestrian_init_bottom[2] = 1130;
		ypoints_pedestrian_init_bottom[2] = 1080;
		xpoints_pedestrian_init_bottom[3] = 700;
		ypoints_pedestrian_init_bottom[3] = 600;
		Polygon pedestrian_init_bottom_area = new Polygon(xpoints_pedestrian_init_bottom,
				ypoints_pedestrian_init_bottom, npoints_pedestrian_init_bottom);
*/
		
		//read in configure/configure.xml
		File fXmlFile = new File("configure/configure.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
				
		// pedestrian_type
		NodeList nList = doc.getElementsByTagName("pedestrian_type");
		for(int i=0;i<nList.getLength();i++) {
			Node node = nList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node;
				pedestrian_type=new String[eElement.getElementsByTagName("ptype").getLength()];
				for(int j=0;j<eElement.getElementsByTagName("ptype").getLength();j++) {
					pedestrian_type[j]=eElement.getElementsByTagName("ptype").item(j).getTextContent();
				}
			}
		}
						
		// pedestrian_init_top_area
		int npoints_pedestrian_init_top=-1;
		int[] xpoints_pedestrian_init_top=null;
		int[] ypoints_pedestrian_init_top=null;
		nList = doc.getElementsByTagName("pedestrian_init_top_area");
		for(int i=0;i<nList.getLength();i++) {
			Node node = nList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node;
						
				npoints_pedestrian_init_top = Integer.parseInt(eElement.getElementsByTagName("npoints_pedestrian_init_top").item(0).getTextContent());
				xpoints_pedestrian_init_top = new int[npoints_pedestrian_init_top];
				ypoints_pedestrian_init_top = new int[npoints_pedestrian_init_top];
				
				for(int j=0;j<eElement.getElementsByTagName("point").getLength();j++) {
					Node node_point = eElement.getElementsByTagName("point").item(j);
					if (node_point.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement_point = (Element) node_point;
						xpoints_pedestrian_init_top[j]=Integer.parseInt(eElement_point.getElementsByTagName("x").item(0).getTextContent());
						ypoints_pedestrian_init_top[j]=Integer.parseInt(eElement_point.getElementsByTagName("y").item(0).getTextContent());
					}
				}
			}
		}
			
		// pedestrian_init_bottom_area
		int npoints_pedestrian_init_bottom=-1;
		int[] xpoints_pedestrian_init_bottom=null;
		int[] ypoints_pedestrian_init_bottom=null;
		nList = doc.getElementsByTagName("pedestrian_init_bottom_area");
		for(int i=0;i<nList.getLength();i++) {
			Node node = nList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node;
						
				npoints_pedestrian_init_bottom = Integer.parseInt(eElement.getElementsByTagName("npoints_pedestrian_init_bottom").item(0).getTextContent());
				xpoints_pedestrian_init_bottom = new int[npoints_pedestrian_init_bottom];
				ypoints_pedestrian_init_bottom = new int[npoints_pedestrian_init_bottom];
				
				for(int j=0;j<eElement.getElementsByTagName("point").getLength();j++) {
					Node node_point = eElement.getElementsByTagName("point").item(j);
					if (node_point.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement_point = (Element) node_point;
						xpoints_pedestrian_init_bottom[j]=Integer.parseInt(eElement_point.getElementsByTagName("x").item(0).getTextContent());
						ypoints_pedestrian_init_bottom[j]=Integer.parseInt(eElement_point.getElementsByTagName("y").item(0).getTextContent());
					}
				}
			}
		}
				
		// pedestrian_valid_area
		int npoints_pedestrian_valid=-1;
		int[] xpoints_pedestrian_valid=null;
		int[] ypoints_pedestrian_valid=null;
		nList = doc.getElementsByTagName("pedestrian_valid_area");
		for(int i=0;i<nList.getLength();i++) {
			Node node = nList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) node;
				
				npoints_pedestrian_valid=Integer.parseInt(eElement.getElementsByTagName("npoints_pedestrian_valid").item(0).getTextContent());
				xpoints_pedestrian_valid = new int[npoints_pedestrian_valid];
				ypoints_pedestrian_valid = new int[npoints_pedestrian_valid];
				
				for(int j=0;j<eElement.getElementsByTagName("point").getLength();j++) {
					Node node_point = eElement.getElementsByTagName("point").item(j);
					if (node_point.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement_point = (Element) node_point;
						xpoints_pedestrian_valid[j]=Integer.parseInt(eElement_point.getElementsByTagName("x").item(0).getTextContent());
						ypoints_pedestrian_valid[j]=Integer.parseInt(eElement_point.getElementsByTagName("y").item(0).getTextContent());
					}
				}
			}
		}
						
		Polygon pedestrian_valid_area = new Polygon(xpoints_pedestrian_valid, ypoints_pedestrian_valid,
				npoints_pedestrian_valid);
		Polygon pedestrian_init_top_area = new Polygon(xpoints_pedestrian_init_top, ypoints_pedestrian_init_top,
				npoints_pedestrian_init_top);
		Polygon pedestrian_init_bottom_area = new Polygon(xpoints_pedestrian_init_bottom,
				ypoints_pedestrian_init_bottom, npoints_pedestrian_init_bottom);		
		/************************************************************/
		HashSet<String> objs_pedestrian_type = new HashSet<String>(Arrays.asList(pedestrian_type));

		LinkedHashMap<Integer, track> tracks_active = new LinkedHashMap<Integer, track>();
		LinkedHashMap<Integer, track> tracks_finished = new LinkedHashMap<Integer, track>();
		LinkedHashMap<Integer, boolean[]> tracks_active_double_touch = new LinkedHashMap<Integer, boolean[]>();
		LinkedHashMap<Integer, Integer> tracks_active_global_direction = new LinkedHashMap<Integer, Integer>();

		int cnt = 0;
		for (Map.Entry<Integer, ArrayList<detection>> entry : data.entrySet()) {
			int frame_num = entry.getKey();
			ArrayList<detection> dets_org = entry.getValue();
			ArrayList<detection> dets_temporary = new ArrayList<detection>();

			for (detection i : dets_org) {
				if (i.prob >= sigma_l) {
					if (objs_pedestrian_type.contains(i.type)) {
						Point2D pt_x_y = new Point2D.Double(i.position.x, i.position.y);
						if (pedestrian_valid_area.contains(pt_x_y)) {
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
						Position p1 = new Position(dets_temporary.get(i).position);
						double prob1 = dets_temporary.get(i).prob;
						Position p2 = new Position(dets_temporary.get(j).position);
						double prob2 = dets_temporary.get(j).prob;

						double radius_help = util_tracker.radius(p1, p2);

						if (radius_help <= radius_pedestrian_remove
								&& objs_pedestrian_type.contains(dets_temporary.get(i).type)
								&& objs_pedestrian_type.contains(dets_temporary.get(j).type)) {
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
			int watch_back = TTL;
			ArrayList<Integer> track_removed_index = new ArrayList<Integer>();

			for (int i : tracks_active.keySet()) {

				boolean[] bool_double_touch = new boolean[2];
				bool_double_touch[0] = tracks_active_double_touch.get(i)[0];
				bool_double_touch[1] = tracks_active_double_touch.get(i)[1];

				int frame_diff = frame_num - tracks_active.get(i).trajs.get(tracks_active.get(i).trajs.size() - 1).frame
						- 1;
				double[] direction_probs = predict_radius_direction(frame_diff, tracks_active.get(i), watch_back, ex_b);

				if (frame_diff <= TTL) {
					if (dets.size() > 0) {
						double min_radius = Double.MAX_VALUE;
						int min_radius_det_index = -1;

						for (int j = 0; j < dets.size(); j++) {
							double radius = -1.0;
							radius = util_tracker.radius(dets.get(j).position,
									tracks_active.get(i).trajs.get(tracks_active.get(i).trajs.size() - 1).position);
							double x_1 = dets.get(j).position.x;
							double y_1 = dets.get(j).position.y;
							double x_0 = tracks_active.get(i).trajs.get(0).position.x;
							double y_0 = tracks_active.get(i).trajs.get(0).position.y;

							if (direction_look_back_steps < tracks_active.get(i).trajs.size() - 1) {
								x_0 = tracks_active.get(i).trajs.get(
										tracks_active.get(i).trajs.size() - 1 - direction_look_back_steps).position.x;
								y_0 = tracks_active.get(i).trajs.get(
										tracks_active.get(i).trajs.size() - 1 - direction_look_back_steps).position.y;
							}

							int dir = -1;
							if (x_1 - x_0 > 0 && y_1 - y_0 > 0) {
								dir = 0;
							} else if (x_1 - x_0 > 0 && y_1 - y_0 == 0) {
								dir = 1;
							} else if (x_1 - x_0 > 0 && y_1 - y_0 < 0) {
								dir = 2;
							} else if (x_1 - x_0 == 0 && y_1 - y_0 > 0) {
								dir = 3;
							} else if (x_1 - x_0 == 0 && y_1 - y_0 == 0) {
								dir = 4;
							} else if (x_1 - x_0 == 0 && y_1 - y_0 < 0) {
								dir = 5;
							} else if (x_1 - x_0 < 0 && y_1 - y_0 > 0) {
								dir = 6;
							} else if (x_1 - x_0 < 0 && y_1 - y_0 == 0) {
								dir = 7;
							} else if (x_1 - x_0 < 0 && y_1 - y_0 < 0) {
								dir = 8;
							}

							// double side touch
							if (bool_double_touch[0] && bool_double_touch[1]) {
								if (dir == tracks_active_global_direction.get(i)) {
									double radius_and_dir = radius * (1 - direction_probs[dir]);
									if (radius_and_dir < min_radius) {
										min_radius = radius_and_dir;
										min_radius_det_index = j;
									}
								}
							} else {
								double radius_and_dir = radius * (1 - direction_probs[dir]);
								if (radius_and_dir < min_radius) {
									min_radius = radius_and_dir;
									min_radius_det_index = j;
								}
							}
						}

						double radius_threshold = predict_radius_threshold(frame_diff, tracks_active.get(i),
								sigma_radius);

						radius_threshold = radius_ttl_limit < radius_threshold ? radius_ttl_limit : radius_threshold;

						if (min_radius <= radius_threshold) {
							tracks_active.get(i).trajs.add(dets.get(min_radius_det_index));
							Point2D pt_x_y = new Point2D.Double(dets.get(min_radius_det_index).position.x,
									dets.get(min_radius_det_index).position.y);

							if (pedestrian_init_top_area.contains(pt_x_y)) {
								bool_double_touch[0] = true;
								tracks_active_double_touch.replace(i, bool_double_touch);
							} else if (pedestrian_init_bottom_area.contains(pt_x_y)) {
								bool_double_touch[1] = true;
								tracks_active_double_touch.replace(i, bool_double_touch);
							}

							double x_1 = dets.get(min_radius_det_index).position.x;
							double y_1 = dets.get(min_radius_det_index).position.y;
							double x_0 = tracks_active.get(i).trajs.get(0).position.x;
							double y_0 = tracks_active.get(i).trajs.get(0).position.y;
							int dir = -1;
							if (x_1 - x_0 > 0 && y_1 - y_0 > 0) {
								dir = 0;
							} else if (x_1 - x_0 > 0 && y_1 - y_0 == 0) {
								dir = 1;
							} else if (x_1 - x_0 > 0 && y_1 - y_0 < 0) {
								dir = 2;
							} else if (x_1 - x_0 == 0 && y_1 - y_0 > 0) {
								dir = 3;
							} else if (x_1 - x_0 == 0 && y_1 - y_0 == 0) {
								dir = 4;
							} else if (x_1 - x_0 == 0 && y_1 - y_0 < 0) {
								dir = 5;
							} else if (x_1 - x_0 < 0 && y_1 - y_0 > 0) {
								dir = 6;
							} else if (x_1 - x_0 < 0 && y_1 - y_0 == 0) {
								dir = 7;
							} else if (x_1 - x_0 < 0 && y_1 - y_0 < 0) {
								dir = 8;
							}

							if (tracks_active_global_direction.containsKey(i)) {
								tracks_active_global_direction.replace(i, dir);
							} else {
								tracks_active_global_direction.put(i, dir);
							}

							dets.remove(min_radius_det_index);
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
				tracks_active_double_touch.remove(i);
				tracks_active_global_direction.remove(i);
			}

			for (detection k : dets) {
				if (objs_pedestrian_type.contains(k.type)) {
					Point2D pt_x_y = new Point2D.Double(k.position.x, k.position.y);
					if (pedestrian_init_top_area.contains(pt_x_y) || pedestrian_init_bottom_area.contains(pt_x_y)) {
						track new_track = new track(cnt);
						new_track.trajs.add(k);
						tracks_active.put(new_track.Id, new_track);
						if (pedestrian_init_top_area.contains(pt_x_y)) {
							boolean[] bool = new boolean[2];
							bool[0] = true;
							bool[1] = false;
							tracks_active_double_touch.put(new_track.Id, bool);
						} else if (pedestrian_init_bottom_area.contains(pt_x_y)) {
							boolean[] bool = new boolean[2];
							bool[0] = false;
							bool[1] = true;
							tracks_active_double_touch.put(new_track.Id, bool);
						}
						cnt++;
					}
				}
			}
		}

		for (int t : tracks_active.keySet()) {
			tracks_finished.put(t, tracks_active.get(t));
		}

		// output the tracking results
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
	 * @param sigma_l: lowest probability of the object detection results be be considered, [0.3, 0.5]
	 * @param radius_pedestrian_remove: threshold of pedestrian moving distance in the same frame to remove the duplicates [10.0]
	 * @param sigma_radius: the distance threshold for each pedestrian object, [5.0, 20.0]
	 * @param t_seconds: the ttl seconds to handle the missing detections, [0.5, 1.5]
	 * @param fps: the fps of the video
	 * @param radius_ttl_limit: max distance of pedestrian moving during missing detections [125.0]
	 * @param direction_look_back_seconds: seconds to look back to learn the next possible direction [0.5]
	 * @param direction_look_back_steps: frames to look back to learn the next possible direction [10]
	 * @param ex_b: the control the softmax normalization [1.0]
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * 
	 * */
	public static LinkedHashMap<Integer, track> track_radius(String input, double sigma_l, double radius_pedestrian_remove, 
			double sigma_radius, double t_seconds, int fps, double radius_ttl_limit, double direction_look_back_seconds, int direction_look_back_steps, double ex_b) throws ParserConfigurationException, SAXException, IOException { // function to be called, return a list of tracks
		LinkedHashMap<Integer, ArrayList<detection>> data = util_tracker.load_mot(input);
		LinkedHashMap<Integer, track> tracks = track_radius(data, sigma_l, radius_pedestrian_remove, sigma_radius, t_seconds, fps, radius_ttl_limit, direction_look_back_seconds, direction_look_back_steps, ex_b, 
				"track_" + input);
		return tracks;
	}
	
	public static LinkedHashMap<Integer, track> track_radius(String input, double sigma_l, double radius_pedestrian_remove, 
			double sigma_radius, double t_seconds, int fps) throws IOException, ParserConfigurationException, SAXException { // function to be called, return a list of tracks
		LinkedHashMap<Integer, ArrayList<detection>> data = util_tracker.load_mot(input);
		LinkedHashMap<Integer, track> tracks = track_radius(data, sigma_l, radius_pedestrian_remove, sigma_radius, t_seconds, fps, 125.0, 0.5, 10, 1.0, 
				"track_" + input);
		return tracks;
	}
	
	public static LinkedHashMap<Integer, track> track_radius(String input, int fps) throws IOException, ParserConfigurationException, SAXException { // function to be called, return a list of tracks
		LinkedHashMap<Integer, ArrayList<detection>> data = util_tracker.load_mot(input);
		LinkedHashMap<Integer, track> tracks = track_radius(data, 0.3, 10.0, 10.0, 1.0, fps, 125.0, 0.5, 10, 1.0, 
				"track_" + input);
		return tracks;
	}
	
	public static LinkedHashMap<Integer, track> track_radius(String input) throws IOException, ParserConfigurationException, SAXException { // function to be called, return a list of tracks
		LinkedHashMap<Integer, ArrayList<detection>> data = util_tracker.load_mot(input);
		LinkedHashMap<Integer, track> tracks = track_radius(data, 0.3, 10.0, 10.0, 1.0, 60, 125.0, 0.5, 10, 1.0, 
				"track_" + input);
		return tracks;
	}
}
