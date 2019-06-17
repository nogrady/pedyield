package dzhuang.pedyield.detector;

import java.awt.Polygon;

import dzhuang.pedyield.tracker.detection;
import dzhuang.pedyield.tracker.track;

public class vehicle extends track {
	public static int fps = 60;
	public static int seconds_in_aoi = 6;

	public int theFirstFrame;
	public int theLastFrame;
	public int theFirstFrame_in_aoi;
	public int theLastFrame_in_aoi;
	public boolean valid;

	public vehicle(track t) {
		super(t);
		this.valid = false;
		if (!t.trajs.isEmpty()) {
			this.theFirstFrame = t.trajs.get(0).frame;
			this.theLastFrame = t.trajs.get(t.trajs.size() - 1).frame;

			int npoints = util_detector.AoI().npoints;
			int[] xpoints = util_detector.AoI().xpoints;
			int[] ypoints = util_detector.AoI().ypoints;
			Polygon aoi = new Polygon(xpoints, ypoints, npoints);
			this.theFirstFrame_in_aoi = -1;
			this.theLastFrame_in_aoi = -1;

			for (detection i : t.trajs) {
				if (aoi.intersects(i.bbox.left, i.bbox.top, i.position.w, i.position.h)) {
					if (this.theFirstFrame_in_aoi == -1) {
						this.theFirstFrame_in_aoi = i.frame;
					}
					this.theLastFrame_in_aoi = i.frame;
				}
			}

			if (this.theFirstFrame_in_aoi != -1 && this.theLastFrame_in_aoi != -1) {
				this.theLastFrame_in_aoi = this.theLastFrame_in_aoi + 1 <= this.theLastFrame
						? this.theLastFrame_in_aoi + 1
						: this.theLastFrame;
				if (this.theLastFrame_in_aoi - this.theFirstFrame_in_aoi <= fps * seconds_in_aoi)
					this.valid = true;
			}
		}
	}
}
