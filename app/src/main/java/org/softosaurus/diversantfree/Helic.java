package org.softosaurus.diversantfree;

public class Helic {
	int state = 0;
	float x, y;
	float dx;
	private int w, h;
	float x2, y2;
	int helic_w;
	int explos = 255;
	public int score = 3;
	
	public Helic(int w, int h, int helic_w, int helic_h) {
		this.w = w;
		this.h = h;	
		this.helic_w = helic_w;
		if(Math.random() < 0.5) {
			dx = (float) (0.01 * w * (Math.random() + 0.1));
			x = -helic_w;
		}
		else {
			dx = (float)(-0.01 * w * (Math.random() + 0.1));
			x = w;
		}
		y = (float) (h * (0.4 * Math.random()));
		x2 = x + helic_w;
		y2 = y + helic_h;
	}
	
	public void move() {
		x += dx;
		x2 = x + helic_w;
		if(state > 2) dx -= dx * 0.05;
	}
}
