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

public class detector2 {
	public static int thresholdOfNoY=200;
	public static int thresholdOfY=265;//250;//200;
	
	public static int consecutiveVofY_bool_chk=50;
	public static int ped_after_veh_buffer=4;
	public static int veh_after_ped_buffer=140;//400;
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
            		
            		int x=(int)Double.parseDouble(jsonObject3.get("X").toString());
            		int y=(int)Double.parseDouble(jsonObject3.get("Y").toString());
            		//TODO
            		int npoints=4;
            		int[] xpoints2=new int[npoints];
            		int[] ypoints2=new int[npoints];
            		xpoints2[0]=1400; ypoints2[0]=280;
            		xpoints2[1]=200; ypoints2[1]=800;
            		xpoints2[2]=920; ypoints2[2]=1080;
            		xpoints2[3]=1650; ypoints2[3]=450;
            		
            		/*xpoints2[0]=0; ypoints2[0]=0;
            		xpoints2[1]=0; ypoints2[1]=1080;
            		xpoints2[2]=1920; ypoints2[2]=1080;
            		xpoints2[3]=1920; ypoints2[3]=0;*/
            		Polygon AoI_Poly_Ped=new Polygon(xpoints2, ypoints2, npoints);
            		
            		
            		Point2D pt=new Point2D.Double(y, x);
            		
            		if(AoI_Poly_Ped.contains(pt)) {
//            		if(true) {
            			PedestrianCoordinates pedCoord=new PedestrianCoordinates(x, y);
                		int frame=(int)Double.parseDouble(jsonObject2.keySet().toArray()[j].toString());
                		
                		/*if(ped.id==39) {
                			double tme=(double) frame/59.84;
            				int min=(int) (tme/60);
            				int sec=(int) (tme%60);
            				
            				String minS="";
            				String secS="";
            				
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
                			
            				
            				System.out.println("00:"+minS+":"+secS+"\t"+frame+"\t"+y+"\t"+x);
 //               			System.out.println(frame+"\t"+y+"\t"+x);
                		}*/
                		
                		ped.addPedestrianTrajectory(frame, pedCoord);
                		
//                		System.out.println(ped.id+"\t"+frame+"\t"+x+"\t"+y);
            		}
            		
            		
            	}
//            	System.out.println(ped.id+"\t"+ped.theFirstFrame+"\t"+ped.theLastFrame);
            	if(!ped.pedestrianTrajectory.isEmpty())
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
		
		/*xpoints[0]=1350; ypoints[0]=370;
		xpoints[1]=1100; ypoints[1]=480;
		xpoints[2]=1350; ypoints[2]=580;
		xpoints[3]=1500; ypoints[3]=450;*/
		
		xpoints[0]=1350; ypoints[0]=370;
		xpoints[1]=1100; ypoints[1]=480;
		xpoints[2]=1400; ypoints[2]=580;
		xpoints[3]=1500; ypoints[3]=480;
		
		/*int[] xpoints2=new int[npoints];
		int[] ypoints2=new int[npoints];
		xpoints2[0]=1350; ypoints2[0]=200;
		xpoints2[1]=600; ypoints2[1]=650;
		xpoints2[2]=1100; ypoints2[2]=1080;
		xpoints2[3]=1650; ypoints2[3]=450;
		Polygon AoI_Poly_Ped=new Polygon(xpoints2, ypoints2, npoints);*/
		
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
				
				int x_front2=entry.getValue().right;
				int y_front2=entry.getValue().bottom;
				int x_back2=entry.getValue().left;
				int y_back2=entry.getValue().bottom;
				
				int x_front3=entry.getValue().right;
				int y_front3=entry.getValue().top;
				int x_back3=entry.getValue().left;
				int y_back3=entry.getValue().top;
				
				Point2D pt_front=new Point2D.Double(x_front, y_front);
				Point2D pt_back=new Point2D.Double(x_back, y_front);
				
				Point2D pt_front2=new Point2D.Double(x_front2, y_front2);
				Point2D pt_back2=new Point2D.Double(x_back2, y_front2);
				
				Point2D pt_front3=new Point2D.Double(x_front3, y_front3);
				Point2D pt_back3=new Point2D.Double(x_back3, y_front3);
				
				// get the "in" frame
				if((AoI_Poly.contains(pt_front) || AoI_Poly.contains(pt_front2) || AoI_Poly.contains(pt_front3)) && timeIn==-1) {
					timeIn=frame;
				}
				// get the "out" frame
				if(AoI_Poly.contains(pt_back) || AoI_Poly.contains(pt_back2) || AoI_Poly.contains(pt_back3)) {
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
			
			boolean VsInBetween=true;
			/*int VsInB=VsInBetween(V_timeIn, V_timeOut, vehID2timeIn, vehID2timeOut);
			if(VsInB>0)
				VsInBetween=false;*/
			
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
				
				boolean consecutiveVofY_bool=true;
//				if((P_timeOut+consecutiveVofY_bool_chk<=V_timeIn || P_timeIn<=V_timeIn)) {
				if((P_timeOut+consecutiveVofY_bool_chk<=V_timeIn)) {
					int conse=consecutiveVofY(P_timeIn, V_timeIn, vehID2timeIn, vehID2timeOut);
					/*if(p_id==357 && v_id==122) {
						System.out.println("GGGGGGGGGGGGGG - "+""+conse);
						System.out.println(P_timeIn+"\t"+P_timeOut+"\t"+V_timeIn);
					}*/
					if(conse>0)
						consecutiveVofY_bool=false;
				}
				
				
				
				/*if(p_id==448 && v_id==174) {
					System.out.println(P_timeIn+"\t"+P_timeOut+"\t"+V_timeIn+"\t"+V_timeOut);
				}
				
				if(p_id==449 && v_id==174) {
					System.out.println(P_timeIn+"\t"+P_timeOut+"\t"+V_timeIn+"\t"+V_timeOut);
				}
				
				if(p_id==448 && v_id==187) {
					System.out.println(187+"\t"+P_timeIn+"\t"+P_timeOut+"\t"+V_timeIn+"\t"+V_timeOut);
				}
				
				if(p_id==449 && v_id==187) {
					System.out.println(187+"\t"+P_timeIn+"\t"+P_timeOut+"\t"+V_timeIn+"\t"+V_timeOut);
				}*/
				
				/*if(p_id==681 && v_id==230) {
					System.out.println(230+"\t"+P_timeIn+"\t"+P_timeOut+"\t"+V_timeIn+"\t"+V_timeOut);
				}
*/
				
				/*if(p_id==462 && v_id==182) {
					System.out.println(182+"\t"+P_timeIn+"\t"+P_timeOut+"\t"+V_timeIn+"\t"+V_timeOut);
				}
				
				if(v_id==182)
				System.out.println(182+"\t"+p_id+"\t"+P_timeIn+"\t"+P_timeOut+"\t"+V_timeIn+"\t"+V_timeOut+"\t"+(V_timeIn-P_timeOut));
				
				if(p_id==488 && v_id==182) {
					System.out.println(182+"\t"+P_timeIn+"\t"+P_timeOut+"\t"+V_timeIn+"\t"+V_timeOut);
				}*/
				//TODO
				/*if(v_id==182 && p_id==526) {
					int conse2=consecutiveVofY(P_timeIn, V_timeIn, vehID2timeIn, vehID2timeOut);
					System.out.println(consecutiveVofY_bool+"\t"+182+"\t"+p_id+"\t"+P_timeIn+"\t"+P_timeOut+"\t"+V_timeIn+"\t"+V_timeOut+"\t"+(V_timeIn-P_timeOut)
							+"\t"+(P_timeOut-P_timeIn)
							+"\t"+(!(V_timeIn-P_timeOut>thresholdOfY))
							+"\t"+(!(P_timeIn-V_timeOut>thresholdOfNoY))
							+"\t"+(P_timeOut-P_timeIn<=4000)
							+"\t"+(V_timeOut-V_timeIn<=200)
							+"\t"+
							((!(V_timeIn-P_timeOut>thresholdOfY))
									&& (!(P_timeIn-V_timeOut>thresholdOfNoY))
									&& (P_timeOut-P_timeIn<=4000)
									&& (V_timeOut-V_timeIn<=200)
									));
					}*/
				
/*//				if(v_id==267 && p_id==355) {
				if(v_id==267) {
					System.out.println(p_id+"\t"+V_timeIn+"\t"+V_timeOut
							+"\t"+P_timeIn
							+"\t"+P_timeOut
							+"\t"+(!(V_timeIn-P_timeOut>thresholdOfY))
							+"\t"+(!(P_timeIn-V_timeOut>thresholdOfNoY))
							+"\t"+consecutiveVofY_bool
							+"\t"+VsInBetween);
				}*/
				
//				if(!((V_theFirstTime>=P_theLastTime)||(P_theFirstTime>=V_theLastTime))) {
				if(!((V_theFirstTime>=P_timeOut)||(P_theFirstTime>=V_timeOut))
//						&& (V_timeOut-P_timeOut>=0 || P_timeIn-V_timeIn>=0)
						&& (!(V_timeIn-P_timeOut>thresholdOfY))
						&& (!(P_timeIn-V_timeOut>thresholdOfNoY))
						&& (P_timeOut-P_timeIn<=1300) // (P_timeOut-P_timeIn<=2700) // (P_timeOut-P_timeIn<=4000)
						&& (V_timeOut-V_timeIn<=350)
						&& consecutiveVofY_bool
						&& VsInBetween
						) {
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
						
					if((P_timeIn>=V_timeOut-ped_after_veh_buffer) || 
							(P_timeIn<V_timeIn && P_timeOut-V_timeOut>veh_after_ped_buffer)) { //
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
	
	public static int consecutiveVofY(int P_timeIn, int V_timeIn, HashMap<Integer, Integer> vehID2timeIn, HashMap<Integer, Integer> vehID2timeOut) {
		int cnt=0;
		for(Vehicle Vi: vehicleList) {
			int v_id=Vi.id;
			TreeMap<Integer, VehicleCoordinates> vehtrajs=new TreeMap<Integer, VehicleCoordinates>(Vi.vehicleTrajectory);
			int V_timeIn2=vehID2timeIn.get(v_id);
			int V_timeOut2=vehID2timeOut.get(v_id);
			
			if((V_timeIn2>P_timeIn && V_timeIn2<V_timeIn) || (V_timeOut2>P_timeIn && V_timeOut2<V_timeIn)) {
				cnt++;	
			}
		}
		
		return cnt;
	}
	
	public static int VsInBetween(int V_timeIn, int V_timeOut, HashMap<Integer, Integer> vehID2timeIn, HashMap<Integer, Integer> vehID2timeOut) {
		int cnt=0;
		for(Vehicle Vi: vehicleList) {
			int v_id=Vi.id;
			TreeMap<Integer, VehicleCoordinates> vehtrajs=new TreeMap<Integer, VehicleCoordinates>(Vi.vehicleTrajectory);
			int V_timeIn2=vehID2timeIn.get(v_id);
			int V_timeOut2=vehID2timeOut.get(v_id);
			
			if((V_timeIn2>V_timeIn && V_timeIn2<V_timeOut) || (V_timeOut2>V_timeIn && V_timeOut2<V_timeOut)) {
				cnt++;	
			}
		}
		
		return cnt;
	}
}
