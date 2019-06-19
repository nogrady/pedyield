package dzhuang.pedyield.tracker;

public class Position {
	// objects position
	public double x; // x of the middle point
	public double y; // y of the middle point
	public double w;
	public double h;

	public Position(double x, double y, double w, double h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public Position(Position position) {
		this.x = position.x;
		this.y = position.y;
		this.w = position.w;
		this.h = position.h;
	}
}
