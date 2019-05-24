package dizhuang.yield;

import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class PedestrianCoordinates{
	public int bottom;
	public int left;
	
	PedestrianCoordinates(){}
	
	PedestrianCoordinates(int bottom, int left){
		this.bottom=bottom;
		this.left=left;
	}
}

class VehicleCoordinates{
	public int bottom;
	public int left;
	public int top;
	public int right;
	public int[] frontPoints; // x, y
	public int[] backPoints;
	
	VehicleCoordinates(){}
	
	VehicleCoordinates(int bottom, int left, int top, int right){
		this.bottom=bottom;
		this.left=left;
		this.top=top;
		this.right=right;
		frontPoints=new int[2];
		frontPoints[0]=right;
		frontPoints[1]=(bottom+top)/2;
		backPoints=new int[2];
		backPoints[0]=left;
		backPoints[1]=(bottom+top)/2;
	}
}

class Vehicle{
	public int id;
	public TreeMap<Integer, VehicleCoordinates> vehicleTrajectory;
	
	public int theFirstFrame;
	public int theLastFrame;
	
	Vehicle(){}
	
	Vehicle(int id){
		this.id=id;
		this.vehicleTrajectory=new TreeMap<Integer, VehicleCoordinates>();
	}
	
	Vehicle(int id, TreeMap<Integer, VehicleCoordinates> vehicleTrajectory){
		this.id=id;
		this.vehicleTrajectory=new TreeMap<Integer, VehicleCoordinates>(vehicleTrajectory);
		
		if(!vehicleTrajectory.isEmpty()) {
			Object[] trajs=vehicleTrajectory.keySet().toArray();
			this.theFirstFrame=(Integer)trajs[0];
			this.theLastFrame=(Integer)trajs[trajs.length -1];
		}
	}
	
	public void addVehicleTrajectory(int frame, VehicleCoordinates pc) {
		if(!vehicleTrajectory.containsKey(frame)) {
			vehicleTrajectory.put(frame, pc);
			
			if(!vehicleTrajectory.isEmpty()) {
				Object[] trajs=vehicleTrajectory.keySet().toArray();
				this.theFirstFrame=(Integer)trajs[0];
				this.theLastFrame=(Integer)trajs[trajs.length -1];
			}
		}
	}
	
	public void replaceVehicleTrajectory(int frame, VehicleCoordinates pc) {
		if(vehicleTrajectory.containsKey(frame)) {
			vehicleTrajectory.replace(frame, pc);
			
			if(!vehicleTrajectory.isEmpty()) {
				Object[] trajs=vehicleTrajectory.keySet().toArray();
				this.theFirstFrame=(Integer)trajs[0];
				this.theLastFrame=(Integer)trajs[trajs.length -1];
			}
		}
	}
}

class Pedestrian{
	public int id;
	public TreeMap<Integer, PedestrianCoordinates> pedestrianTrajectory;
	
	public int theFirstFrame;
	public int theLastFrame;
	
	Pedestrian(){}
	
	Pedestrian(int id){
		this.id=id;
		this.pedestrianTrajectory=new TreeMap<Integer, PedestrianCoordinates>();
	}
	
	Pedestrian(int id, TreeMap<Integer, PedestrianCoordinates> pedestrianTrajectory){
		this.id=id;
		this.pedestrianTrajectory=new TreeMap<Integer, PedestrianCoordinates>(pedestrianTrajectory);
		
		if(!pedestrianTrajectory.isEmpty()) {
			Object[] trajs=pedestrianTrajectory.keySet().toArray();
			this.theFirstFrame=(Integer)trajs[0];
			this.theLastFrame=(Integer)trajs[trajs.length -1];
		}
	}
	
	public void addPedestrianTrajectory(int frame, PedestrianCoordinates pc) {
		if(!pedestrianTrajectory.containsKey(frame)) {
			pedestrianTrajectory.put(frame, pc);
			
			if(!pedestrianTrajectory.isEmpty()) {
				Object[] trajs=pedestrianTrajectory.keySet().toArray();
				this.theFirstFrame=(Integer)trajs[0];
				this.theLastFrame=(Integer)trajs[trajs.length -1];
			}
		}
	}
	
	public void replacePedestrianTrajectory(int frame, PedestrianCoordinates pc) {
		if(pedestrianTrajectory.containsKey(frame)) {
			pedestrianTrajectory.replace(frame, pc);
			
			if(!pedestrianTrajectory.isEmpty()) {
				Object[] trajs=pedestrianTrajectory.keySet().toArray();
				this.theFirstFrame=(Integer)trajs[0];
				this.theLastFrame=(Integer)trajs[trajs.length -1];
			}
		}
	}
}

public class detector {
	public static int thresholdOfNoY=200;
	public static int thresholdOfY=200;
	
	public static ArrayList<Vehicle> vehicleList=new ArrayList<Vehicle>();
	public static ArrayList<Pedestrian> pedestrianList=new ArrayList<Pedestrian>();
	
	public static void LoadData(String pedestrian_track_data, String vehicle_track_data) throws ParseException {
		JSONParser parser_pedestrian = new JSONParser();
		try {
        	// X: Bottom, Y: Left
			Object obj_pedestrian = parser_pedestrian.parse(new FileReader(pedestrian_track_data));
            JSONObject jsonObject_pedestrian =  (JSONObject) obj_pedestrian;            
            for(int i=0;i<jsonObject_pedestrian.keySet().toArray().length;i++) {
            	Pedestrian ped=new Pedestrian(Integer.parseInt((String) jsonObject_pedestrian.keySet().toArray()[i]));
            	JSONObject jsonObject2 =  (JSONObject)jsonObject_pedestrian.get(jsonObject_pedestrian.keySet().toArray()[i]+"");
            	
            	for(int j=0;j<jsonObject2.keySet().toArray().length;j++) {
            		JSONObject jsonObject3 =  (JSONObject)((JSONObject)((JSONObject)(jsonObject_pedestrian.get(jsonObject_pedestrian.keySet().toArray()[i]+""))).get(jsonObject2.keySet().toArray()[j]+"")).get("cord");
            		
            		PedestrianCoordinates pedCoord=new PedestrianCoordinates((int)Double.parseDouble(jsonObject3.get("X").toString()), (int)Double.parseDouble(jsonObject3.get("Y").toString()));
            		int frame=(int)Double.parseDouble(jsonObject2.keySet().toArray()[j].toString());
            		
            		ped.addPedestrianTrajectory(frame, pedCoord);
            	}
//            	System.out.println(ped.id+"\t"+ped.theFirstFrame+"\t"+ped.theLastFrame);
            	
            	pedestrianList.add(ped);
            	//TODO
            	/*TreeMap<Integer, PedestrianCoordinates> pedTrajs=ped.pedestrianTrajectory;
            	Object[] trajs=pedTrajs.keySet().toArray();
    			int theFirstFrame=(Integer)trajs[0];
    			int theLastFrame=(Integer)trajs[trajs.length-1];
            	
    			if(ped.pedestrianTrajectory.get(theFirstFrame).left>900 && ped.pedestrianTrajectory.get(theLastFrame).left>900
    					&& Math.abs(ped.pedestrianTrajectory.get(theLastFrame).bottom-ped.pedestrianTrajectory.get(theFirstFrame).bottom)>250) {
    				pedestrianList.add(ped);
//    				System.out.println(ped.id+"\t"+ped.theFirstFrame+"\t"+ped.theLastFrame);
    			}*/
            	
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//		System.out.println("***********************************************");
		//TODO
//		HashSet<Integer> vids=new HashSet<Integer>();
		/*vids.add(0);
		vids.add(2);
		vids.add(9);
		vids.add(12);
		vids.add(16);
		vids.add(21);
		vids.add(25);
		vids.add(31);
		vids.add(47);
		vids.add(52);*/
		
		JSONParser parser_vehicle = new JSONParser();
		try {
        	// X: Bottom, Y: Left
			Object obj_vehicle = parser_vehicle.parse(new FileReader(vehicle_track_data));
            JSONObject jsonObject_vehicle =  (JSONObject) obj_vehicle;            
            for(int i=0;i<jsonObject_vehicle.keySet().toArray().length;i++) {
            	Vehicle veh=new Vehicle(Integer.parseInt((String) jsonObject_vehicle.keySet().toArray()[i]));
            	JSONObject jsonObject2 =  (JSONObject)jsonObject_vehicle.get(jsonObject_vehicle.keySet().toArray()[i]+"");
            	
            	for(int j=0;j<jsonObject2.keySet().toArray().length;j++) {
            		JSONObject jsonObject3 =  (JSONObject)((JSONObject)((JSONObject)(jsonObject_vehicle.get(jsonObject_vehicle.keySet().toArray()[i]+""))).get(jsonObject2.keySet().toArray()[j]+"")).get("cord");
            		
            		VehicleCoordinates vehCoord=new VehicleCoordinates((int)Double.parseDouble(jsonObject3.get("X").toString()), (int)Double.parseDouble(jsonObject3.get("Y").toString()),
            				(int)Double.parseDouble(jsonObject3.get("top").toString()), (int)Double.parseDouble(jsonObject3.get("right").toString()));
            		int frame=(int)Double.parseDouble(jsonObject2.keySet().toArray()[j].toString());
            		
            		veh.addVehicleTrajectory(frame, vehCoord);
            	}
//            	System.out.println(veh.id+"\t"+veh.theFirstFrame+"\t"+veh.theLastFrame);
            	
//            	if(vids.contains(veh.id) && veh.theLastFrame-veh.theFirstFrame>100) {
            	if(veh.theLastFrame-veh.theFirstFrame>80) {
            		vehicleList.add(veh);
//            		System.out.println(veh.id+"\t"+veh.theFirstFrame+"\t"+veh.theLastFrame);
            	}
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public static void main(String[] args) throws org.json.simple.parser.ParseException {
//		LoadData("pedestrian_track.json", "vehicle_track.json");
//		LoadData("ped_track_j_GOPR4091.json", "car_track_j_GOPR4091.json");
//		LoadData("ped_track_j_GOPR4110.json", "car_track_j_GOPR4110.json");
//		LoadData("ped_track_j_GOPR4109.json", "car_track_j_GOPR4109.json");
//		LoadData("ped_track_j_GOPR4106.json", "car_track_j_GOPR4106.json");

		LoadData("ped_track_j_GH100228.json", "car_track_j_GH100228.json");
		int npoints=4;
		int[] xpoints=new int[npoints];
		int[] ypoints=new int[npoints];
		/*xpoints[0]=1180; ypoints[0]=450;
		xpoints[1]=1030; ypoints[1]=600;
		xpoints[2]=1350; ypoints[2]=600;
		xpoints[3]=1450; ypoints[3]=450;*/
		
		xpoints[0]=1350; ypoints[0]=370;
		xpoints[1]=1100; ypoints[1]=480;
		xpoints[2]=1350; ypoints[2]=580;
		xpoints[3]=1500; ypoints[3]=450;
		
		Polygon AoI_Poly=new Polygon(xpoints, ypoints, npoints);
		
		/*******************************************************************************************/
		HashMap<Integer, Integer> pedID2timeIn=new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> pedID2timeOut=new HashMap<Integer, Integer>();
		
		int cnt=0;
		ArrayList<Integer> tobeDel=new ArrayList<Integer>();
		for(Pedestrian i: pedestrianList) {
			int pedID=i.id;
//			System.out.println(pedID);
			TreeMap<Integer, PedestrianCoordinates> pedtrajs=new TreeMap<Integer, PedestrianCoordinates>(i.pedestrianTrajectory);
			int timeIn=-1;
			int timeOut=-1;
			
			int timeIn_bottom=-1;
			int timeOut_bottom=-1;
			
			for(Entry<Integer, PedestrianCoordinates> entry: pedtrajs.entrySet()) {
				int frame=entry.getKey();
				int x=entry.getValue().left;
				int y=entry.getValue().bottom;
				Point2D pt=new Point2D.Double(x, y);
				if(AoI_Poly.contains(pt)) {
					if(timeIn==-1) {
						timeIn=frame;
						timeIn_bottom=y;
					}
					timeOut=frame;
					timeOut_bottom=y;
//					System.out.println(pedID+"\t"+frame+"\t"+x+"\t"+y);
				}
			}
								
			if(timeIn!=-1 && timeOut!=-1 && 
					(((timeIn_bottom<(ypoints[0]+ypoints[2])/2)&&(timeOut_bottom>(ypoints[0]+ypoints[2])/2)) 
					|| ((timeIn_bottom>(ypoints[0]+ypoints[2])/2)&&(timeOut_bottom<(ypoints[0]+ypoints[2])/2)))) {
				pedID2timeIn.put(pedID, timeIn);
				pedID2timeOut.put(pedID, timeOut+1<=i.theLastFrame?timeOut+1:i.theLastFrame);
				System.out.println(pedID+"\t"+i.theFirstFrame+"\t"+timeIn+"\t"+(timeOut+1<=i.theLastFrame?timeOut+1:i.theLastFrame)+"\t"+i.theLastFrame);
			}
			else {
				tobeDel.add(cnt);
//				System.out.println(cnt);
			}
			cnt++;
		}
//		System.out.println(cnt);
		int mtch=0;
		for(int i: tobeDel) {
//			System.out.println(i-mtch);
			pedestrianList.remove(i-mtch);
			mtch++;
		}
		System.out.println("/*******************************************************************************************/");
		/*******************************************************************************************/
		HashMap<Integer, Integer> vehID2timeIn=new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> vehID2timeOut=new HashMap<Integer, Integer>();
		
		cnt=0;
		tobeDel=new ArrayList<Integer>();
		for(Vehicle i: vehicleList) {
			int vehID=i.id;
			TreeMap<Integer, VehicleCoordinates> vehtrajs=new TreeMap<Integer, VehicleCoordinates>(i.vehicleTrajectory);
			int timeIn=-1;
			int timeOut=-1;
			
			for(Entry<Integer, VehicleCoordinates> entry: vehtrajs.entrySet()) {
				int frame=entry.getKey();
				int x_front=entry.getValue().right;
				int y_front=(entry.getValue().bottom+entry.getValue().top)/2;
				int x_back=entry.getValue().left;
				int y_back=(entry.getValue().bottom+entry.getValue().top)/2;
				
				Point2D pt_front=new Point2D.Double(x_front, y_front);
				Point2D pt_back=new Point2D.Double(x_back, y_front);
				
				// get the "in" frame
				if(AoI_Poly.contains(pt_front) && timeIn==-1) {
					timeIn=frame;
				}
				// get the "out" frame
				if(AoI_Poly.contains(pt_back)) {
					timeOut=frame;
				}
			}
								
			if(timeIn!=-1 && timeOut!=-1) {
				vehID2timeIn.put(vehID, timeIn);
				vehID2timeOut.put(vehID, timeOut+1<=i.theLastFrame?timeOut+1:i.theLastFrame);
				System.out.println(vehID+"\t"+i.theFirstFrame+"\t"+timeIn+"\t"+(timeOut+1<=i.theLastFrame?timeOut+1:i.theLastFrame)+"\t"+i.theLastFrame);
			}
			else {
				tobeDel.add(cnt);
				System.out.println(cnt+" - "+vehID);
			}
			cnt++;
		}
//		System.out.println(cnt);
		mtch=0;
		for(int i: tobeDel) {
//			System.out.println(i-mtch);
			vehicleList.remove(i-mtch);
			mtch++;
		}
		System.out.println("/*******************************************************************************************/");
		/*******************************************************************************************/
		
		for(Vehicle Vi: vehicleList) {
			int v_id=Vi.id;
			TreeMap<Integer, VehicleCoordinates> vehtrajs=new TreeMap<Integer, VehicleCoordinates>(Vi.vehicleTrajectory);
			int V_timeIn=vehID2timeIn.get(v_id);
			int V_timeOut=vehID2timeOut.get(v_id);
			int V_theFirstTime=Vi.theFirstFrame;
			int V_theLastTime=Vi.theLastFrame;
			
			boolean yieldNeeded=false;
			boolean yield=true;
			
			String minS="";
			String secS="";
			for(Pedestrian Pi: pedestrianList) {
				int p_id=Pi.id;
				TreeMap<Integer, PedestrianCoordinates> pedtrajs=new TreeMap<Integer, PedestrianCoordinates>(Pi.pedestrianTrajectory);
				int P_timeIn=pedID2timeIn.get(p_id);
				int P_timeOut=pedID2timeOut.get(p_id);
				int P_theFirstTime=Pi.theFirstFrame;
				int P_theLastTime=Pi.theLastFrame;
				
				double tme=(double) V_timeIn/59.84;
				int min=(int) (tme/60);
				int sec=(int) (tme%60);
				if(min<=9) {
					minS=0+""+min;
				}
				else {
					minS=""+min;
				}
				
				if(sec<=9) {
					secS=0+""+sec;
				}
				else {
					secS=""+sec;
				}
				
				/*
				 * if(v_id==11) { System.out.println(p_id+"\t"+(V_timeIn-P_timeOut)
				 * +"\t"+(P_timeIn-V_timeOut)); }
				 */
				
//				if(!((V_theFirstTime>=P_theLastTime)||(P_theFirstTime>=V_theLastTime))) {
				if(!((V_theFirstTime>=P_timeOut)||(P_theFirstTime>=V_timeOut))
						&& !(V_timeIn-P_timeOut>thresholdOfY) 
						&& !(P_timeIn-V_timeOut>thresholdOfNoY)) {
					yieldNeeded=true;
				//	if(P_timeIn>P_theFirstTime && P_timeOut>V_theFirstTime) {
//TODO
//						System.out.println(v_id+" - "+p_id +"\t"+V_theFirstTime+"\t"+V_timeIn+"\t"+V_timeOut
//								 +"\t"+P_theFirstTime+"\t"+P_timeIn+"\t"+P_timeOut);
				//	}
					
					/*if(P_timeIn-V_timeOut<thresholdOfNoY && P_timeIn>=V_timeOut) { //
						yield=false;
						break;
					}*/
						
					if(P_timeIn>=V_timeOut) { //
						yield=false;
//						break;
					}
					// 1: do not need; 2: yielded; 3: did not yield
					if(yieldNeeded && yield) {
//						System.out.println(v_id+" yield "+ p_id);
						
						System.out.println("00:"+minS+":"+secS+"\t"+v_id+"\t"+2+"\t"+ p_id);
					}
					else if(yieldNeeded && !yield) {
//						System.out.println(v_id+" did not yield "+ p_id);
						System.out.println("00:"+minS+":"+secS+"\t"+v_id+"\t"+3+"\t"+ p_id);
					}
				}
			}
			
			if(yieldNeeded) {
				if(yield) {
//					System.out.println(v_id+" yield!");
				}
				else {
//					System.out.println(v_id+" not yield!");
				}
			}
			else {
//				System.out.println(v_id+" no need yield!");
				System.out.println("00:"+minS+":"+secS+"\t"+v_id+"\t"+1);
			}
		}
		
	}
}
