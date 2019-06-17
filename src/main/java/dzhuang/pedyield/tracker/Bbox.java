package dzhuang.pedyield.tracker;

public class Bbox {
	public int bottom;
	public int left;
	public int top;
	public int right;
	
	public Bbox(int bottom, int left, int top, int right) {
		this.bottom=bottom;
		this.left=left;
		this.top=top;
		this.right=right;
	}
	
	public Bbox(Bbox bbox) {
		this.bottom=bbox.bottom;
		this.left=bbox.left;
		this.top=bbox.top;
		this.right=bbox.right;
	}
}
