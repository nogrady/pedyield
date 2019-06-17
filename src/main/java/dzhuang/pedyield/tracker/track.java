package dzhuang.pedyield.tracker;

import java.util.ArrayList;

public class track {

	public int Id;
	public ArrayList<detection> trajs;
	
	public track(int Id, ArrayList<detection> trajs) {
		this.Id=Id;
		this.trajs=new ArrayList<detection>(trajs);
	}
	
	public track(int Id) {
		this.Id=Id;
		this.trajs=new ArrayList<detection>();
	}
	
	public track(track t) {
		this.Id=t.Id;
		this.trajs=new ArrayList<detection>(t.trajs);
	}
}

