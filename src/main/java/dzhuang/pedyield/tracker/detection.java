package dzhuang.pedyield.tracker;

public class detection {
	// detected objects in the current frame
	public int frame;
	public double time;
	// type of objects
	public String type;
	// confidence score
	public double prob;

	public Bbox bbox;
	public Position position;

	public detection(int frame, double x, double y, double w, double h) {
		this.frame = frame;
		this.position = new Position(x, y, w, h);
		toBbox(position);
	}

	public detection(int frame, double time, String type, double prob, double x, double y, double w, double h) {
		this.frame = frame;
		this.time = time;
		this.type = type;
		this.prob = prob;

		this.position = new Position(x, y, w, h);
		toBbox(position);
	}

	public detection(int frame, double time, String type, double prob, int bottom, int left, int top, int right) {
		this.frame = frame;
		this.time = time;
		this.type = type;
		this.prob = prob;

		this.bbox = new Bbox(bottom, left, top, right);
		toPosition(bbox);
	}

	public detection(int frame, double time, String type, double prob, int bottom, int left, int top, int right,
			double x, double y, double w, double h) {
		this.frame = frame;
		this.time = time;
		this.type = type;
		this.prob = prob;

		this.bbox = new Bbox(bottom, left, top, right);
		this.position = new Position(x, y, w, h);
	}

	public detection(int frame, int bottom, int left, int top, int right) {
		this.frame = frame;
		this.bbox = new Bbox(bottom, left, top, right);
		toPosition(bbox);
	}

	public void toBbox(Position position) {
		int xmin = (int) (position.x - (position.w / 2.0));
		int xmax = (int) (position.x + (position.w / 2.0));
		int ymin = (int) (position.y - (position.h / 2.0));
		int ymax = (int) (position.y + (position.h / 2.0));

		int left = xmin;
		int top = ymin;
		int right = xmax;
		int bottom = ymax;

		this.bbox = new Bbox(bottom, left, top, right);
	}

	public void toPosition(Bbox bbox) {
		double xmin = bbox.left;
		double ymin = bbox.top;
		double xmax = bbox.right;
		double ymax = bbox.bottom;

		double x = (xmin + xmax) / 2.0;
		double y = (ymin + ymax) / 2.0;
		double w = xmax - xmin;
		double h = ymax - ymin;

		this.position = new Position(x, y, w, h);
	}
}