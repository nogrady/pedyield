package dzhuang.pedyield.detector;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class pedyield_detector {
	public static ArrayList<pedestrian> pedestrian_list;
	public static ArrayList<vehicle> vehicle_list;

	public static int thresholdOfNoY = 200;
	public static int thresholdOfY = 265;

	public static int fps = 60;

	public static int p_dangerous2out = (int) (fps * (0.1));
	public static int v_dangerous2out = (int) (fps * (0.05)); // v_dangerous2out << p_dangerous2out
	public static int p_walk_through = (int) (fps * (1.0));

	public static void main(String[] args) throws IOException {
		for (int i = 1; i <= 1; i++) {
			System.out.println("File - " + i);
			if (i < 10)
				pedyield_detector_run("GH0" + i + "0228_2_1920-1080-59_0.5_0.3_ped.csv",
						"GH0" + i + "0228_2_1920-1080-59_0.5_0.3_car.csv");
			else
				pedyield_detector_run("GH" + i + "0228_2_1920-1080-59_0.5_0.3_ped.csv",
						"GH" + i + "0228_2_1920-1080-59_0.5_0.3_car.csv");
		}
	}

	public static void pedyield_detector_run(String pedInput, String vehInput) throws IOException {
		pedestrian_list = new ArrayList<pedestrian>(util_detector.load_pedestrian_list(pedInput));
		vehicle_list = new ArrayList<vehicle>(util_detector.load_vehicle_list(vehInput));
		/*************************************************************************************/
		ArrayList<String> res = new ArrayList<String>();

		for (vehicle vi : vehicle_list) {
			boolean yieldNeeded = false;
			boolean yield = true;

			String minS = "";
			String secS = "";
			for (pedestrian pi : pedestrian_list) {
				double tme = vi.theFirstFrame_in_aoi / 59.84;
				int min = (int) (tme / 60);
				int sec = (int) (tme % 60);

				if (min <= 9)
					minS = 0 + "" + min;
				else
					minS = "" + min;

				if (sec <= 9)
					secS = 0 + "" + sec;
				else
					secS = "" + sec;

				if (!((vi.theFirstFrame > pi.theLastFrame_in_aoi) || (pi.theFirstFrame > vi.theLastFrame_in_aoi))) {

					if (pi.theFirstFrame_in_aoi <= vi.theFirstFrame_in_aoi) {
						// p get in aoi first
						if (vi.theFirstFrame_in_aoi >= pi.theLastFrame_in_aoi - p_dangerous2out
								&& vi.theFirstFrame_in_aoi - pi.theLastFrame_in_aoi <= thresholdOfY) {
							// v get in aoi after p get out, within time duration t - yield
							// p_dangerous2out: the time of p walk from the ``dangerous'' boundary to the
							// aoi boundary
							yieldNeeded = true;
							yield = true;
						} else if (vi.theFirstFrame_in_aoi < pi.theLastFrame_in_aoi - p_dangerous2out
								&& vi.theLastFrame_in_aoi >= pi.theLastFrame_in_aoi - p_dangerous2out) {
							// v get in aoi before p get out, but p get out before v get out - yield
							yieldNeeded = true;
							yield = true;
						} else if (vi.theFirstFrame_in_aoi < pi.theLastFrame_in_aoi - p_dangerous2out
								&& vi.theLastFrame_in_aoi < pi.theLastFrame_in_aoi - p_dangerous2out
								&& pi.theLastFrame_in_aoi - p_dangerous2out - vi.theLastFrame_in_aoi <= thresholdOfNoY
										+ p_walk_through) {
							// v get in aoi before p get out, but v get out before p get out, and p get out
							// after v get out, within time duration t - not yield
							// p_walk_through: the time of p walk through aoi
							yieldNeeded = true;
							yield = false;
						}
					} else if (pi.theFirstFrame_in_aoi > vi.theFirstFrame_in_aoi) {
						// v get in aoi first
						if (pi.theFirstFrame_in_aoi >= vi.theLastFrame_in_aoi - v_dangerous2out
								&& pi.theFirstFrame_in_aoi - vi.theLastFrame_in_aoi <= thresholdOfNoY) {
							// p get in aoi after v get out, within time duration t - not yield
							// v_dangerous2out: the time of v run from the ``dangerous'' boundary to the aoi
							// boundary << timebuffer1
							yieldNeeded = true;
							yield = false;
						} else if (pi.theFirstFrame_in_aoi < vi.theLastFrame_in_aoi - v_dangerous2out
								&& vi.theLastFrame_in_aoi >= pi.theLastFrame_in_aoi - p_dangerous2out) {
							// p get in aoi before v get out, but p get out before v get out - yield
							yieldNeeded = true;
							yield = true;
						} else if (pi.theFirstFrame_in_aoi < vi.theLastFrame_in_aoi - v_dangerous2out
								&& vi.theLastFrame_in_aoi < pi.theLastFrame_in_aoi - p_dangerous2out
								&& pi.theLastFrame_in_aoi - p_dangerous2out - vi.theLastFrame_in_aoi <= thresholdOfNoY
										+ p_walk_through) {
							// p get in aoi before v get out, but v get out before p get out, and p get out
							// after v get out, within time duration t - not yield
							yieldNeeded = true;
							yield = false;
						}
					}

					// 1: do not need; 2: yielded; 3: did not yield
					if (yieldNeeded && yield) {
//						System.out.println("00:"+minS+":"+secS+"\t"+vi.Id+"\t"+2+"\t"+ pi.Id);
						res.add("00:" + minS + ":" + secS + "," + vi.Id + "," + 2 + "," + pi.Id);
					} else if (yieldNeeded && !yield) {
//						System.out.println("00:"+minS+":"+secS+"\t"+vi.Id+"\t"+3+"\t"+ pi.Id);
						res.add("00:" + minS + ":" + secS + "," + vi.Id + "," + 3 + "," + pi.Id);
					}
				}
			}

			if (!yieldNeeded) {
//				System.out.println("00:"+minS+":"+secS+"\t"+vi.Id+"\t"+1);
				res.add("00:" + minS + ":" + secS + "," + vi.Id + "," + 1 + ",");
			}
		}

		PrintWriter pw = new PrintWriter(pedInput.split("_")[0] + "_pedyield_RESULTS.csv");
		pw.println("time" + "," + "v_id" + "," + "event" + "," + "p_id");

		for (String i : res) {
			pw.println(i);
		}

		pw.close();
	}
}
