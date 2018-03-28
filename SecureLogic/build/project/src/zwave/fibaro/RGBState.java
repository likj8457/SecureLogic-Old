package zwave.fibaro;

public class RGBState {
	private int R;
	private int G;
	private int B;
	private int W;
	
	private int level;
	
	public RGBState (int r, int g, int b, int w, int level) {
		this.R = r;
		this.G = g;
		this.B = b;
		this.W = w;
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getR() {
		return R;
	}

	public void setR(int r) {
		R = r;
	}

	public int getG() {
		return G;
	}

	public void setG(int g) {
		G = g;
	}

	public int getB() {
		return B;
	}

	public void setB(int b) {
		B = b;
	}

	public int getW() {
		return W;
	}

	public void setW(int w) {
		W = w;
	}
}
