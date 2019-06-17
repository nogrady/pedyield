package dzhuang.pedyield.tracker;

import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class iou_tracker {

	public static String[] vehicle_type = { "bus", "car", "truck" };
	public static String[] pedestrian_type = { "person" };

	public static double iou_vehicle = 0.75;
	public static double iou_pedestrian = 0.85;

	public static void main(String[] args) throws IOException {
		for (int i = 1; i <= 10; i++) {
			System.out.println("File - " + i);
			if (i < 10)
				track_iou("GH0" + i + "0228_2_1920-1080-59_0.5_0.3_car.csv", 0.3, 0.5, 0.5, 0.5, 60);
			else
				track_iou("GH" + i + "0228_2_1920-1080-59_0.5_0.3_car.csv", 0.3, 0.5, 0.5, 0.5, 60);
		}
	}

	public static Bbox predictBbox(int frame_diff, track track_cur) {
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
			double sigma_l, double sigma_h, double sigma_iou, double t_seconds, int fps, String output)
			throws FileNotFoundException {
		int TTL = (int) (t_seconds * fps);
		/************************************************************/
		int npoints_vehicle_init = 5;
		int[] xpoints_vehicle_init = new int[npoints_vehicle_init];
		int[] ypoints_vehicle_init = new int[npoints_vehicle_init];
		xpoints_vehicle_init[0] = 0;
		ypoints_vehicle_init[0] = 420;
		xpoints_vehicle_init[1] = 450;
		ypoints_vehicle_init[1] = 330;
		xpoints_vehicle_init[2] = 710;
		ypoints_vehicle_init[2] = 330;
		xpoints_vehicle_init[3] = 770;
		ypoints_vehicle_init[3] = 430;
		xpoints_vehicle_init[4] = 770;
		ypoints_vehicle_init[4] = 1080;
		Polygon vehicle_init_area = new Polygon(xpoints_vehicle_init, ypoints_vehicle_init, npoints_vehicle_init);

		int npoints_vehicle_valid = 5;
		int[] xpoints_vehicle_valid = new int[npoints_vehicle_valid];
		int[] ypoints_vehicle_valid = new int[npoints_vehicle_valid];
		xpoints_vehicle_valid[0] = 0;
		ypoints_vehicle_valid[0] = 420;
		xpoints_vehicle_valid[1] = 450;
		ypoints_vehicle_valid[1] = 330;
		xpoints_vehicle_valid[2] = 1470;
		ypoints_vehicle_valid[2] = 330;
		xpoints_vehicle_valid[3] = 1575;
		ypoints_vehicle_valid[3] = 415;
		xpoints_vehicle_valid[4] = 1130;
		ypoints_vehicle_valid[4] = 1080;
		Polygon vehicle_valid_area = new Polygon(xpoints_vehicle_valid, ypoints_vehicle_valid, npoints_vehicle_valid);
		/************************************************************/

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
		/************************************************************/

		HashSet<String> objs_vehicle_type = new HashSet<String>(Arrays.asList(vehicle_type));
		HashSet<String> objs_pedestrian_type = new HashSet<String>(Arrays.asList(pedestrian_type));

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
					} else if (objs_pedestrian_type.contains(i.type)) {
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
						Bbox b1 = new Bbox(dets_temporary.get(i).bbox);
						double prob1 = dets_temporary.get(i).prob;
						Bbox b2 = new Bbox(dets_temporary.get(j).bbox);
						double prob2 = dets_temporary.get(j).prob;

						double iou_help = util_tracker.iou(b1, b2);

						if (iou_help >= iou_vehicle && objs_vehicle_type.contains(dets_temporary.get(i).type)
								&& objs_vehicle_type.contains(dets_temporary.get(j).type)) {
							flag = true;
							if (prob1 > prob2)
								dets_removed_index.add(j);
							else
								dets_removed_index.add(i);
						} else if (iou_help >= iou_pedestrian
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
				} else if (objs_pedestrian_type.contains(k.type)) {
					Point2D pt_x_y = new Point2D.Double(k.position.x, k.position.y);
//					if(pedestrian_init_top_area.contains(pt_x_y) || pedestrian_init_bottom_area.contains(pt_x_y)) {
					track new_track = new track(cnt);
					new_track.trajs.add(k);
					tracks_active.put(new_track.Id, new_track);
					cnt++;
//					}
				}

			}
		}

		for (int t : tracks_active.keySet()) {
			tracks_finished.put(t, tracks_active.get(t));
		}

		PrintWriter pw = new PrintWriter(output);

		for (Map.Entry<Integer, track> entry : tracks_finished.entrySet()) {
			int key = entry.getKey();
			track tr = entry.getValue();
			for (detection d : tr.trajs) {
				pw.println(d.frame + "," + tr.Id + "," + d.position.x + "," + d.position.y + "," + d.position.w + ","
						+ d.position.h);
			}
		}
		pw.close();
		return tracks_finished;
	}

	public static LinkedHashMap<Integer, track> track_iou(String input, double sigma_l, double sigma_h,
			double sigma_iou, double t_seconds, int fps) throws IOException {
		LinkedHashMap<Integer, ArrayList<detection>> data = util_tracker.load_mot(input);
		LinkedHashMap<Integer, track> tracks = track_iou(data, sigma_l, sigma_h, sigma_iou, t_seconds, fps,
				"track_" + input);
		return tracks;
	}
}
