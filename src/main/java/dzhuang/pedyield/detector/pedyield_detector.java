package dzhuang.pedyield.detector;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class pedyield_detector {
	public static ArrayList<pedestrian> pedestrian_list;
	public static ArrayList<vehicle> vehicle_list;

	public static int thresholdOfNoY = 200;
	public static int thresholdOfY = 265;
	public static double p_dangerous2out_buffer_threshold = 0.1;
	public static double v_dangerous2out_buffer_threshold = 0.05;
	// v_dangerous2out_buffer_threshold << p_dangerous2out_buffer_threshold
	public static double p_walk_through_buffer_threshold = 1.0;

	public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
		Options options = new Options();
		Option input_vehicle = new Option("ip", "ped_input", true, "input file path of the pedestrian data");
		input_vehicle.setRequired(true);
		options.addOption(input_vehicle);

		Option input_pedestrian = new Option("iv", "veh_output", true, "input file path of the vehicle data");
		input_pedestrian.setRequired(true);
		options.addOption(input_pedestrian);

		Option threshold_not_yield = new Option("t1", "threshold_not_yield", true, "the threshold of not yield");
		threshold_not_yield.setRequired(false);
		options.addOption(threshold_not_yield);

		Option threshold_yield = new Option("t2", "threshold_yield", true, "the threshold of yield");
		threshold_yield.setRequired(false);
		options.addOption(threshold_yield);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("utility-name", options);

			// just for testing
			pedyield_detector_run("GH0" + 1 + "0228_2_1920-1080-59_0.5_0.3_ped.csv",
					"GH0" + 1 + "0228_2_1920-1080-59_0.5_0.3_car.csv");

			System.exit(1);
		}

		String inputFilePath_p = cmd.getOptionValue("ped_input");
		String inputFilePath_v = cmd.getOptionValue("veh_output");

		if (cmd.hasOption("threshold_not_yield") && cmd.hasOption("threshold_yield")) {
			int t1 = Integer.parseInt(cmd.getOptionValue("threshold_not_yield"));
			int t2 = Integer.parseInt(cmd.getOptionValue("threshold_yield"));
			pedyield_detector_run(inputFilePath_p, inputFilePath_v, t1, t2);
		} else if (cmd.hasOption("threshold_not_yield") && !cmd.hasOption("threshold_yield")) {
			System.out.println("Please provide both thresholds!");
		} else if (!cmd.hasOption("threshold_not_yield") && cmd.hasOption("threshold_yield")) {
			System.out.println("Please provide both thresholds!");
		} else {
			pedyield_detector_run(inputFilePath_p, inputFilePath_v);
		}

//		pedyield_detector_run("GH0" + 1 + "0228_2_1920-1080-59_0.5_0.3_ped.csv", "GH0" + 1 + "0228_2_1920-1080-59_0.5_0.3_car.csv");

	}

	public static void pedyield_detector_run(String pedInput, String vehInput)
			throws IOException, SAXException, ParserConfigurationException {
		pedyield_detector_run(pedInput, vehInput, thresholdOfNoY, thresholdOfY, p_dangerous2out_buffer_threshold,
				v_dangerous2out_buffer_threshold, p_walk_through_buffer_threshold);
	}

	public static void pedyield_detector_run(String pedInput, String vehInput, int thresholdOfNoY_, int thresholdOfY_)
			throws IOException, SAXException, ParserConfigurationException {
		pedyield_detector_run(pedInput, vehInput, thresholdOfNoY_, thresholdOfY_, p_dangerous2out_buffer_threshold,
				v_dangerous2out_buffer_threshold, p_walk_through_buffer_threshold);
	}

	public static void pedyield_detector_run(String pedInput, String vehInput, int thresholdOfNoY_, int thresholdOfY_,
			double p_dangerous2out_buffer_threshold_, double v_dangerous2out_buffer_threshold_,
			double p_walk_through_buffer_threshold_) throws IOException, SAXException, ParserConfigurationException {
		thresholdOfNoY = thresholdOfNoY_;
		thresholdOfY = thresholdOfY_;
		p_dangerous2out_buffer_threshold = p_dangerous2out_buffer_threshold_;
		v_dangerous2out_buffer_threshold = v_dangerous2out_buffer_threshold_;
		p_walk_through_buffer_threshold = p_walk_through_buffer_threshold_;

		double fps_double = 0.0;
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
				fps_double = Double.parseDouble(eElement.getElementsByTagName("fps_double").item(0).getTextContent());
			}
		}

		int p_dangerous2out = (int) (fps * (p_dangerous2out_buffer_threshold));
		int v_dangerous2out = (int) (fps * (v_dangerous2out_buffer_threshold)); // v_dangerous2out << p_dangerous2out
		int p_walk_through = (int) (fps * (p_walk_through_buffer_threshold));

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
				double tme = vi.theFirstFrame_in_aoi / fps_double;
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
