package com.example.android.pedometer;

public class StatCube {

	public final String tag;
	public Interval xg0, xg12, xlm12, yg0, zg0, x5dn, x5up, z5dn, z5up, avd;

	public class Interval {
		private static final float INF = 100;
		private float min, max;

		public Interval() {
			min = INF;
			max = -INF;
		}

		public void put(float v) {
			min = Math.min(min, v);
			max = Math.max(max, v);
		}

		public boolean contains(float v) {
			return min <= v && v <= max;
		}

		public String toString(String hdr) {
			String main;
			if (min == max)
				main = "" + min;
			else
				main = min + "..." + max;
			return hdr + main + "<br/>";
		}

		public void stretchToInfinity() {
			min = -INF;
			max = INF;
		}
	}
	
	public StatCube (String tag) {
		this.tag = tag;
		xg0 = new Interval();
		xg12  = new Interval();
		xlm12  = new Interval(); 
		yg0 = new Interval(); 
		zg0 = new Interval();
		
		x5dn = new Interval(); 
		x5up = new Interval(); 
		z5dn = new Interval();
		z5up = new Interval();

		avd = new Interval();
	}
	
	public void put(
			float _xg0, float _xg12, float _xlm12, float _yg0, float _zg0,
			float _x5dn, float _x5up, float _z5dn, float _z5up, float _avd) {
		xg0.put(_xg0);
		xg12.put(_xg12);
		xlm12.put(_xlm12);
		yg0.put(_yg0);
		zg0.put(_zg0);
		x5dn.put(_x5dn);
		x5up.put(_x5up);
		z5dn.put(_z5dn);
		z5up.put(_z5up);
		avd.put(_avd);
	}

	public boolean contains(
			float _xg0, float _xg12, float _xlm12, float _yg0, float _zg0,
			float _x5dn, float _x5up, float _z5dn, float _z5up, float _avd) {
		return
				xg0.contains(_xg0) &&
				xg12.contains(_xg12) &&
				xlm12.contains(_xlm12) &&
				yg0.contains(_yg0) &&
				zg0.contains(_zg0) &&
				x5dn.contains(_x5dn) &&
				x5up.contains(_x5up) &&
				z5dn.contains(_z5dn) &&
				z5up.contains(_z5up) &&
				avd.contains(_avd);
	}
	
	@Override
	public String toString() {
		return "<b>" + tag + "</b><br/>" +
				xg0.toString("x > 0: ") +
				xg12.toString("x > 12: ") +
				xlm12.toString("x < -12: ") +
				yg0.toString("y > 0: ") +
				zg0.toString("z > 0: ") +
				x5dn.toString("x 5% dn: ") +
				x5up.toString("x 5% up: ") +
				z5dn.toString("z 5% dn: ") +
				z5up.toString("z 5% up: ") +
				avd.toString("avd: ");
	}
}
