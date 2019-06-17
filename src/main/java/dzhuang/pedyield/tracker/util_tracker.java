package dzhuang.pedyield.tracker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class util_tracker {

	public static double iou(Bbox bbox1, Bbox bbox2) {
		int x0_1 = bbox1.left;
		int y0_1 = bbox1.top;
		int x1_1 = bbox1.right;
		int y1_1 = bbox1.bottom;

		int x0_2 = bbox2.left;
		int y0_2 = bbox2.top;
		int x1_2 = bbox2.right;
		int y1_2 = bbox2.bottom;

		// get the overlap rectangle
		int overlap_x0 = Math.max(x0_1, x0_2);
		int overlap_y0 = Math.max(y0_1, y0_2);
		int overlap_x1 = Math.min(x1_1, x1_2);
		int overlap_y1 = Math.min(y1_1, y1_2);

		// check if there is an overlap
		if ((overlap_x1 - overlap_x0 <= 0) || (overlap_y1 - overlap_y0 <= 0))
			return 0.0;

		// if yes, calculate the ratio of the overlap to each ROI size and the unified
		// size
		int size_1 = (x1_1 - x0_1) * (y1_1 - y0_1);
		int size_2 = (x1_2 - x0_2) * (y1_2 - y0_2);
		int size_intersection = (overlap_x1 - overlap_x0) * (overlap_y1 - overlap_y0);
		int size_union = size_1 + size_2 - size_intersection;

		return (double) size_intersection / (double) size_union;
	}

	public static LinkedHashMap<Integer, ArrayList<detection>> load_mot(String input) throws IOException {
		LinkedHashMap<Integer, ArrayList<detection>> data = new LinkedHashMap<Integer, ArrayList<detection>>();

		BufferedReader br = new BufferedReader(new FileReader(input));
		String line = "";
		while ((line = br.readLine()) != null) {
			String[] lines = line.split(",");
			if (!lines[0].equals("frame")) {
				int frame = (int) Double.parseDouble(lines[0]);
				detection det = null;
				if (lines.length == 12) {
					det = new detection((int) Double.parseDouble(lines[0]), Double.parseDouble(lines[1]), lines[2],
							Double.parseDouble(lines[3]), Integer.parseInt(lines[4]), Integer.parseInt(lines[5]),
							Integer.parseInt(lines[6]), Integer.parseInt(lines[7]), Double.parseDouble(lines[8]),
							Double.parseDouble(lines[9]), Double.parseDouble(lines[10]), Double.parseDouble(lines[11]));
				} else if (lines.length == 8) {
					if (lines[4].contains(".") || lines[5].contains(".") || lines[6].contains(".")
							|| lines[7].contains(".")) {
						det = new detection((int) Double.parseDouble(lines[0]), Double.parseDouble(lines[1]), lines[2],
								Double.parseDouble(lines[3]), Double.parseDouble(lines[4]),
								Double.parseDouble(lines[5]), Double.parseDouble(lines[6]),
								Double.parseDouble(lines[7]));
					} else {
						det = new detection(Integer.parseInt(lines[0]), Double.parseDouble(lines[1]), lines[2],
								Double.parseDouble(lines[3]), Integer.parseInt(lines[4]), Integer.parseInt(lines[5]),
								Integer.parseInt(lines[6]), Integer.parseInt(lines[7]));
					}
				} else if (lines.length == 5) {
					if (lines[1].contains(".") || lines[2].contains(".") || lines[3].contains(".")
							|| lines[4].contains(".")) {
						det = new detection((int) Double.parseDouble(lines[0]), Double.parseDouble(lines[1]),
								Double.parseDouble(lines[2]), Double.parseDouble(lines[3]),
								Double.parseDouble(lines[4]));
					} else {
						det = new detection((int) Double.parseDouble(lines[0]), Integer.parseInt(lines[1]),
								Integer.parseInt(lines[2]), Integer.parseInt(lines[3]), Integer.parseInt(lines[4]));
					}
				}

				if (data.containsKey(frame)) {
					data.get(frame).add(det);
				} else {
					ArrayList<detection> tmp = new ArrayList<detection>();
					tmp.add(det);
					data.put(frame, tmp);
				}
			}
		}
		br.close();

		return data;
	}

	public static double radius(Position position1, Position position2) {
		double x1 = position1.x;
		double y1 = position1.y;
		double x2 = position2.x;
		double y2 = position2.y;

		double dis = Math.sqrt(Math.pow(x1 - x2, 2.0) + Math.pow(y1 - y2, 2.0));

		return dis;
	}
}