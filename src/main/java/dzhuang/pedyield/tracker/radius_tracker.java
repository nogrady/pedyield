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

public class radius_tracker {
	public static String[] pedestrian_type = {"person"};
	public static double radius_pedestrian=10.0;
	public static double radius_ttl_limit=125.0;
	public static double direction_look_back_seconds=0.5; // default
	public static int direction_look_back_steps=10; // default
	
	public static void main(String[] args) throws IOException {		
		track_radius("GH010228_2_1920-1080-59_0.5_0.3_ped.csv", 0.3, 0.5, 10.0, 1.0, 60);
		
		for(int i=1;i<=10;i++) {
			System.out.println("File - "+i);
			if(i<10)
				track_radius("GH0"+i+"0228_2_1920-1080-59_0.5_0.3_ped.csv", 0.3, 0.5, 10.0, 1.0, 60);
			else
				track_radius("GH"+i+"0228_2_1920-1080-59_0.5_0.3_ped.csv", 0.3, 0.5, 10.0, 1.0, 60);
		}
	}
	
	public static LinkedHashMap<Integer, track> track_radius(String input,
			double sigma_l, double sigma_h, double sigma_radius, double t_seconds, int fps) throws IOException {
		LinkedHashMap<Integer, ArrayList<detection>> data=util_tracker.load_mot(input);		
		LinkedHashMap<Integer, track> tracks=track_radius(data, sigma_l, sigma_h, sigma_radius, t_seconds, fps, "track_"+input);
		return tracks;
	}
	
	public static LinkedHashMap<Integer, track> track_radius(LinkedHashMap<Integer, ArrayList<detection>> data,
			double sigma_l, double sigma_h, double sigma_radius, double t_seconds, int fps, String output) throws FileNotFoundException {
		int TTL=(int)(t_seconds*fps);
		direction_look_back_steps=(int)(direction_look_back_seconds*fps);
		
		/************************************************************/
		// TODO
		int npoints_pedestrian_valid=4;
		int[] xpoints_pedestrian_valid=new int[npoints_pedestrian_valid];
		int[] ypoints_pedestrian_valid=new int[npoints_pedestrian_valid];
		xpoints_pedestrian_valid[0]=1400; ypoints_pedestrian_valid[0]=290;
		xpoints_pedestrian_valid[1]=1580; ypoints_pedestrian_valid[1]=400;
		xpoints_pedestrian_valid[2]=1130; ypoints_pedestrian_valid[2]=1080;
		xpoints_pedestrian_valid[3]=700; ypoints_pedestrian_valid[3]=600;
		Polygon pedestrian_valid_area=new Polygon(xpoints_pedestrian_valid, ypoints_pedestrian_valid, npoints_pedestrian_valid);
		
		int npoints_pedestrian_init_top=4;
		int[] xpoints_pedestrian_init_top=new int[npoints_pedestrian_init_top];
		int[] ypoints_pedestrian_init_top=new int[npoints_pedestrian_init_top];
		xpoints_pedestrian_init_top[0]=1400; ypoints_pedestrian_init_top[0]=290;
		xpoints_pedestrian_init_top[1]=1580; ypoints_pedestrian_init_top[1]=400;
		xpoints_pedestrian_init_top[2]=1560; ypoints_pedestrian_init_top[2]=500;
		xpoints_pedestrian_init_top[3]=1300; ypoints_pedestrian_init_top[3]=325;
		Polygon pedestrian_init_top_area=new Polygon(xpoints_pedestrian_init_top, ypoints_pedestrian_init_top, npoints_pedestrian_init_top);
		
		int npoints_pedestrian_init_bottom=4;
		int[] xpoints_pedestrian_init_bottom=new int[npoints_pedestrian_init_bottom];
		int[] ypoints_pedestrian_init_bottom=new int[npoints_pedestrian_init_bottom];
		xpoints_pedestrian_init_bottom[0]=1000; ypoints_pedestrian_init_bottom[0]=450;
		xpoints_pedestrian_init_bottom[1]=1480; ypoints_pedestrian_init_bottom[1]=610;
		xpoints_pedestrian_init_bottom[2]=1130; ypoints_pedestrian_init_bottom[2]=1080;
		xpoints_pedestrian_init_bottom[3]=700; ypoints_pedestrian_init_bottom[3]=600;
		Polygon pedestrian_init_bottom_area=new Polygon(xpoints_pedestrian_init_bottom, ypoints_pedestrian_init_bottom, npoints_pedestrian_init_bottom);
		/************************************************************/
		HashSet<String> objs_pedestrian_type=new HashSet<String>(Arrays.asList(pedestrian_type));
		
		LinkedHashMap<Integer, track> tracks_active=new LinkedHashMap<Integer, track>();
		LinkedHashMap<Integer, track> tracks_finished=new LinkedHashMap<Integer, track>();
		LinkedHashMap<Integer, boolean[]> tracks_active_double_touch=new LinkedHashMap<Integer, boolean[]>();
		LinkedHashMap<Integer, Integer> tracks_active_global_direction=new LinkedHashMap<Integer, Integer>();
		
		int cnt=0;
		for(Map.Entry<Integer, ArrayList<detection>> entry: data.entrySet()) {
			int frame_num=entry.getKey();
			ArrayList<detection> dets_org=entry.getValue();
			ArrayList<detection> dets_temporary=new ArrayList<detection>();
			
			for(detection i:dets_org) {
				if(i.prob>=sigma_l) {
					if(objs_pedestrian_type.contains(i.type)) {
						Point2D pt_x_y=new Point2D.Double(i.position.x, i.position.y);
						if(pedestrian_valid_area.contains(pt_x_y)) {
							dets_temporary.add(i);
						}
					}				
				}
			}
			
			boolean flag=true;
			while(flag) {
				flag=false;
				ArrayList<Integer> dets_removed_index=new ArrayList<Integer>();
				for(int i=0;i<dets_temporary.size();i++) {
					for(int j=i+1;j<dets_temporary.size();j++) {
						Position p1=new Position(dets_temporary.get(i).position);
						double prob1=dets_temporary.get(i).prob;
						Position p2=new Position(dets_temporary.get(j).position);
						double prob2=dets_temporary.get(j).prob;
						
						double radius_help=util_tracker.radius(p1, p2);
						
						if(radius_help<=radius_pedestrian && objs_pedestrian_type.contains(dets_temporary.get(i).type)
								&& objs_pedestrian_type.contains(dets_temporary.get(j).type)) {
							flag=true;
							if(prob1>prob2) 
								dets_removed_index.add(j);
							else
								dets_removed_index.add(i);
						}
					}
				}
				
				Collections.sort(dets_removed_index, Collections.reverseOrder());
				for(int i: dets_removed_index) {
					dets_temporary.remove(i);
				}
			}
			
			ArrayList<detection> dets=new ArrayList<detection>();
			for(detection i:dets_temporary) {
				if(i.prob>=sigma_l) {
					dets.add(i);
				}
			}
			int watch_back=TTL;
			ArrayList<Integer> track_removed_index=new ArrayList<Integer>();
						
			
			
			for(int i: tracks_active.keySet()) {
//				System.out.println(1+"\t"+tracks_active_double_touch.get(i));
				
				boolean[] bool_double_touch=new boolean[2];
				bool_double_touch[0]=tracks_active_double_touch.get(i)[0];
				bool_double_touch[1]=tracks_active_double_touch.get(i)[1];
				
				int frame_diff=frame_num-tracks_active.get(i).trajs.get(tracks_active.get(i).trajs.size()-1).frame-1;
				double[] direction_probs=predict_radius_direction(frame_diff, tracks_active.get(i), watch_back);
				
				if(frame_diff<=TTL) {
					if(dets.size()>0) {
						double min_radius=Double.MAX_VALUE;
						// TODO
						double min_radius_nod=Double.MAX_VALUE;
						int min_radius_det_index_nod=-1;
						int min_radius_det_index=-1;
						
						for(int j=0;j<dets.size();j++) {
							double radius=-1.0;
							radius=util_tracker.radius(dets.get(j).position, tracks_active.get(i).trajs.get(tracks_active.get(i).trajs.size()-1).position);
							double x_1=dets.get(j).position.x;
							double y_1=dets.get(j).position.y;
//							double x_0=tracks_active.get(i).trajs.get(tracks_active.get(i).trajs.size()-1).position.x;
//							double y_0=tracks_active.get(i).trajs.get(tracks_active.get(i).trajs.size()-1).position.y;
							double x_0=tracks_active.get(i).trajs.get(0).position.x;
							double y_0=tracks_active.get(i).trajs.get(0).position.y;
							
							if(direction_look_back_steps<tracks_active.get(i).trajs.size()-1) {
								x_0=tracks_active.get(i).trajs.get(tracks_active.get(i).trajs.size()-1-direction_look_back_steps).position.x;
								y_0=tracks_active.get(i).trajs.get(tracks_active.get(i).trajs.size()-1-direction_look_back_steps).position.y;
							}
							
							
							int dir=-1;
							if(x_1-x_0>0 && y_1-y_0>0) {
								dir=0;
							}
							else if(x_1-x_0>0 && y_1-y_0==0) {
								dir=1;
							}
							else if(x_1-x_0>0 && y_1-y_0<0) {
								dir=2;
							}
							else if(x_1-x_0==0 && y_1-y_0>0) {
								dir=3;
							}
							else if(x_1-x_0==0 && y_1-y_0==0) {
								dir=4;
							}
							else if(x_1-x_0==0 && y_1-y_0<0) {
								dir=5;
							}
							else if(x_1-x_0<0 && y_1-y_0>0) {
								dir=6;
							}
							else if(x_1-x_0<0 && y_1-y_0==0) {
								dir=7;
							}
							else if(x_1-x_0<0 && y_1-y_0<0) {
								dir=8;
							}
							// TODO
							// double side touch
							if(bool_double_touch[0] && bool_double_touch[1]) {
								if(dir==tracks_active_global_direction.get(i)) {
									double radius_and_dir=radius*(1-direction_probs[dir]);
//									System.out.println(radius_and_dir);
									if(radius_and_dir<min_radius) {
										min_radius=radius_and_dir;
										min_radius_det_index=j;
									}
									
									if(radius<min_radius_nod) {
										min_radius_nod=radius;
										min_radius_det_index_nod=j;
									}
								}
							}
							else {
								double radius_and_dir=radius*(1-direction_probs[dir]);
//								System.out.println(radius_and_dir);
								if(radius_and_dir<min_radius) {
									min_radius=radius_and_dir;
									min_radius_det_index=j;
								}
								
								if(radius<min_radius_nod) {
									min_radius_nod=radius;
									min_radius_det_index_nod=j;
								}
							}
						}
						
						
						// TODO
						double radius_threshold=predict_radius_threshold(frame_diff, tracks_active.get(i), sigma_radius);
						
						radius_threshold=radius_ttl_limit < radius_threshold ? radius_ttl_limit : radius_threshold;
						
//						System.out.println(min_radius+"\t"+min_radius_nod+"\t"+radius_threshold+"\t"+frame_diff+"\t"+min_radius_det_index+"\t"+min_radius_det_index_nod);
						if(min_radius<=radius_threshold) {
							tracks_active.get(i).trajs.add(dets.get(min_radius_det_index));
							Point2D pt_x_y=new Point2D.Double(dets.get(min_radius_det_index).position.x, dets.get(min_radius_det_index).position.y);
							
							if(pedestrian_init_top_area.contains(pt_x_y)) {
								bool_double_touch[0]=true;
								tracks_active_double_touch.replace(i, bool_double_touch);
							}
							else if(pedestrian_init_bottom_area.contains(pt_x_y)) {
								bool_double_touch[1]=true;
								tracks_active_double_touch.replace(i, bool_double_touch);
							}
							
							double x_1=dets.get(min_radius_det_index).position.x;
							double y_1=dets.get(min_radius_det_index).position.y;
							double x_0=tracks_active.get(i).trajs.get(0).position.x;
							double y_0=tracks_active.get(i).trajs.get(0).position.y;
							int dir=-1;
							if(x_1-x_0>0 && y_1-y_0>0) {
								dir=0;
							}
							else if(x_1-x_0>0 && y_1-y_0==0) {
								dir=1;
							}
							else if(x_1-x_0>0 && y_1-y_0<0) {
								dir=2;
							}
							else if(x_1-x_0==0 && y_1-y_0>0) {
								dir=3;
							}
							else if(x_1-x_0==0 && y_1-y_0==0) {
								dir=4;
							}
							else if(x_1-x_0==0 && y_1-y_0<0) {
								dir=5;
							}
							else if(x_1-x_0<0 && y_1-y_0>0) {
								dir=6;
							}
							else if(x_1-x_0<0 && y_1-y_0==0) {
								dir=7;
							}
							else if(x_1-x_0<0 && y_1-y_0<0) {
								dir=8;
							}
							
							if(tracks_active_global_direction.containsKey(i)) {
								tracks_active_global_direction.replace(i, dir);
							}
							else {
								tracks_active_global_direction.put(i, dir);
							}
							
							dets.remove(min_radius_det_index);
						}
					}
				}
				else {
					tracks_finished.put(i, tracks_active.get(i));
					track_removed_index.add(i);
				}
			}
			
			Collections.sort(track_removed_index, Collections.reverseOrder());
			for(int i: track_removed_index) {
				tracks_active.remove(i);
				tracks_active_double_touch.remove(i);
				tracks_active_global_direction.remove(i);
			}
			
			for(detection k:dets) {
				if(objs_pedestrian_type.contains(k.type)) {
					Point2D pt_x_y=new Point2D.Double(k.position.x, k.position.y);
					// TODO
					if(pedestrian_init_top_area.contains(pt_x_y) || pedestrian_init_bottom_area.contains(pt_x_y)) {
						track new_track=new track(cnt);
						new_track.trajs.add(k);
						tracks_active.put(new_track.Id, new_track);
						if(pedestrian_init_top_area.contains(pt_x_y)) {
							boolean[] bool=new boolean[2];
							bool[0]=true;
							bool[1]=false;
							tracks_active_double_touch.put(new_track.Id, bool);
						}
						else if(pedestrian_init_bottom_area.contains(pt_x_y)) {
							boolean[] bool=new boolean[2];
							bool[0]=false;
							bool[1]=true;
							tracks_active_double_touch.put(new_track.Id, bool);
						}
//						System.out.println(new_track.Id+"\t"+tracks_active_double_touch.get(new_track.Id));
						cnt++;
					}
				}
			}
		}
				
		for(int t:tracks_active.keySet()) {
			tracks_finished.put(t, tracks_active.get(t));
		}
		
		PrintWriter pw= new PrintWriter(output);
		
		for(Map.Entry<Integer, track> entry:tracks_finished.entrySet()) {
			int key=entry.getKey();
			track tr=entry.getValue();
			for(detection d: tr.trajs) {
				pw.println(d.frame+","+tr.Id+","+d.position.x+","+d.position.y+","+d.position.w+","+d.position.h);
			}
		}
		pw.close();
		return tracks_finished;
	}
	
	public static double ex_b=1.0;
	
	public static double[] softmax(double[] z, double b) {
		double sum=0;
		for(int i=0;i<z.length;i++) {
			sum+=Math.exp(ex_b*b*z[i]);
		}
		
		double[] z_o=new double[z.length];
		for(int i=0;i<z_o.length;i++) {
			z_o[i]=Math.exp(ex_b*b*z[i])/sum;
		}
		
		return z_o;
	} 
	
	public static double[] predict_radius_direction(int frame_diff, track track_cur, int watch_back) {
		// 0:x_1-x_0>0, y_1-y_0>0
		// 1:x_1-x_0>0, y_1-y_0=0
		// 2:x_1-x_0>0, y_1-y_0<0
		// 3:x_1-x_0=0, y_1-y_0>0
		// 4:x_1-x_0=0, y_1-y_0=0
		// 5:x_1-x_0=0, y_1-y_0<0
		// 6:x_1-x_0<0, y_1-y_0>0
		// 7:x_1-x_0<0, y_1-y_0=0
		// 8:x_1-x_0<0, y_1-y_0<0
		double attenuation_rate=0.9;
		double[] direction_prob=new double[9];
		for(int i=0;i<direction_prob.length;i++) {
			direction_prob[i]=0.0;
		}

		
		int total_frame=track_cur.trajs.size();
		
		if(total_frame-1<watch_back) {
			int cnt=0;
			for(int i=total_frame-1;i>=1;i--) {
				double x_1=track_cur.trajs.get(i).position.x;
				double y_1=track_cur.trajs.get(i).position.y;
				double x_0=track_cur.trajs.get(i-1).position.x;
				double y_0=track_cur.trajs.get(i-1).position.y;
				int tim=track_cur.trajs.get(i).frame-track_cur.trajs.get(i-1).frame;
				
//				System.out.println(track_cur.trajs.get(i).frame+"\t"+track_cur.trajs.get(i-1).frame);
				cnt+=tim;
				if(x_1-x_0>0 && y_1-y_0>0) {
					direction_prob[0]=direction_prob[0]+1.0*tim;
				}
				else if(x_1-x_0>0 && y_1-y_0==0) {
					direction_prob[1]=direction_prob[1]+1.0*tim;
				}
				else if(x_1-x_0>0 && y_1-y_0<0) {
					direction_prob[2]=direction_prob[2]+1.0*tim;
				}
				else if(x_1-x_0==0 && y_1-y_0>0) {
					direction_prob[3]=direction_prob[3]+1.0*tim;
				}
				else if(x_1-x_0==0 && y_1-y_0==0) {
					direction_prob[4]=direction_prob[4]+1.0*tim;
				}
				else if(x_1-x_0==0 && y_1-y_0<0) {
					direction_prob[5]=direction_prob[5]+1.0*tim;
				}
				else if(x_1-x_0<0 && y_1-y_0>0) {
					direction_prob[6]=direction_prob[6]+1.0*tim;
				}
				else if(x_1-x_0<0 && y_1-y_0==0) {
					direction_prob[7]=direction_prob[7]+1.0*tim;
				}
				else if(x_1-x_0<0 && y_1-y_0<0) {
					direction_prob[8]=direction_prob[8]+1.0*tim;
				}
			}
			
			for(int i=0;i<direction_prob.length;i++) {
				direction_prob[i]=direction_prob[i]/(double)(cnt+1);
			}
			
		}
		else {
			int cnt=0;
			for(int i=total_frame-1;i>=total_frame-watch_back;i--) {
				double x_1=track_cur.trajs.get(i).position.x;
				double y_1=track_cur.trajs.get(i).position.y;
				double x_0=track_cur.trajs.get(i-1).position.x;
				double y_0=track_cur.trajs.get(i-1).position.y;
				int tim=track_cur.trajs.get(i).frame-track_cur.trajs.get(i-1).frame;
				cnt+=tim;
				if(x_1-x_0>0 && y_1-y_0>0) {
					direction_prob[0]=direction_prob[0]+1.0*tim;
				}
				else if(x_1-x_0>0 && y_1-y_0==0) {
					direction_prob[1]=direction_prob[1]+1.0*tim;
				}
				else if(x_1-x_0>0 && y_1-y_0<0) {
					direction_prob[2]=direction_prob[2]+1.0*tim;
				}
				else if(x_1-x_0==0 && y_1-y_0>0) {
					direction_prob[3]=direction_prob[3]+1.0*tim;
				}
				else if(x_1-x_0==0 && y_1-y_0==0) {
					direction_prob[4]=direction_prob[4]+1.0*tim;
				}
				else if(x_1-x_0==0 && y_1-y_0<0) {
					direction_prob[5]=direction_prob[5]+1.0*tim;
				}
				else if(x_1-x_0<0 && y_1-y_0>0) {
					direction_prob[6]=direction_prob[6]+1.0*tim;
				}
				else if(x_1-x_0<0 && y_1-y_0==0) {
					direction_prob[7]=direction_prob[7]+1.0*tim;
				}
				else if(x_1-x_0<0 && y_1-y_0<0) {
					direction_prob[8]=direction_prob[8]+1.0*tim;
				}
			}
			
			for(int i=0;i<direction_prob.length;i++) {
				direction_prob[i]=direction_prob[i]/(double)(cnt+1);
			}
		}
		
		/*for(int i=0;i<direction_prob.length;i++) {
			System.out.print(direction_prob[i]+"\t");
		}
		System.out.println();*/
		
		return softmax(direction_prob, (double)(frame_diff*attenuation_rate+1));
	}
	
	public static double predict_radius_threshold(int frame_diff, track track_cur, double sigma_radius) {
		int total_frame=track_cur.trajs.size();
		double attenuation_rate=0.9;
		
		if(frame_diff==0) {
			if(total_frame==1) {
				return sigma_radius;
			}
			else {
				double r=util_tracker.radius(track_cur.trajs.get(track_cur.trajs.size()-1).position, track_cur.trajs.get(track_cur.trajs.size()-2).position);
				int tim=track_cur.trajs.get(track_cur.trajs.size()-1).frame-track_cur.trajs.get(track_cur.trajs.size()-2).frame;
				r=r/(double)tim;
				
				return r > sigma_radius ? r : sigma_radius;
			}
		}
		
		if(total_frame==1) {
			return sigma_radius*frame_diff*attenuation_rate;
		}
		else {
			double avg_dis_move=0.0;
			if(frame_diff>=total_frame-1) {
				for(int i=total_frame-1;i>=1;i--) {
					double radius_one_frame=util_tracker.radius(track_cur.trajs.get(i).position, track_cur.trajs.get(i-1).position);
					int tim=track_cur.trajs.get(i).frame-track_cur.trajs.get(i-1).frame;
					radius_one_frame=radius_one_frame/(double)tim;
					avg_dis_move+=radius_one_frame;
				}
				avg_dis_move=avg_dis_move/(double)(total_frame-1);
			}
			else if(frame_diff<total_frame-1) {			
				for(int i=total_frame-1;i>=total_frame-frame_diff;i--) {
					double radius_one_frame=util_tracker.radius(track_cur.trajs.get(i).position, track_cur.trajs.get(i-1).position);
					int tim=track_cur.trajs.get(i).frame-track_cur.trajs.get(i-1).frame;
					radius_one_frame=radius_one_frame/(double)tim;
					avg_dis_move+=radius_one_frame;
				}
				avg_dis_move=avg_dis_move/(double)(frame_diff);
			}
			double move_one_frame=avg_dis_move > sigma_radius ? avg_dis_move : sigma_radius;
			
			return move_one_frame*frame_diff*attenuation_rate;
		}
	}
}
