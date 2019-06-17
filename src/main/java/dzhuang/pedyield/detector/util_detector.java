package dzhuang.pedyield.detector;

import java.awt.Polygon;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import dzhuang.pedyield.tracker.iou_tracker;
import dzhuang.pedyield.tracker.radius_tracker;
import dzhuang.pedyield.tracker.track;

public class util_detector {

	public static Polygon AoI() {
		int npoints=4;
		int[] xpoints=new int[npoints];
		int[] ypoints=new int[npoints];
		
		xpoints[0]=1350; ypoints[0]=370;
		xpoints[1]=1520; ypoints[1]=485;
		
		xpoints[2]=1420; ypoints[2]=585;
		xpoints[3]=1100; ypoints[3]=487;
		
		Polygon aoi=new Polygon(xpoints, ypoints, npoints);
		return aoi;
	}
	
	public static ArrayList<pedestrian> load_pedestrian_list(String input) throws IOException {
		LinkedHashMap<Integer, track> pedestrian_tracks=radius_tracker.track_radius(input, 0.3, 0.5, 10.0, 1.0, 60);
		ArrayList<pedestrian> pedestrian_list=new ArrayList<pedestrian>();
		for(Map.Entry<Integer, track> t: pedestrian_tracks.entrySet()) {
			pedestrian p=new pedestrian(t.getValue());
			if(p.valid) {
				pedestrian_list.add(p);
			}
		}
		
		return pedestrian_list;
	}
	
	public static ArrayList<vehicle> load_vehicle_list(String input) throws IOException {
		LinkedHashMap<Integer, track> vehicle_tracks=iou_tracker.track_iou(input, 0.3, 0.5, 0.5, 0.5, 60);
		ArrayList<vehicle> vehicle_list=new ArrayList<vehicle>();
		for(Map.Entry<Integer, track> t: vehicle_tracks.entrySet()) {
			vehicle v=new vehicle(t.getValue());
			if(v.valid) {
				vehicle_list.add(v);
			}
		}
		
		return vehicle_list;
	}
}
