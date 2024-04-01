package org.softosaurus.diversantfree;

import android.graphics.Rect;

public class Bomber {
	int state = 0;
	float x, y;
	float dx;
	private int w, h;
	float x2, y2;
	int bomber_w, bomber_h, bomber_h_orig;
	int explos = 255;
	int luk = 0;
	public int score = 5;
	
	public Bomber(int w, int h, int bomber_w, int bomber_h) {
		this.w = w;
		this.h = h;	
		this.bomber_w = bomber_w;
		this.bomber_h = bomber_h - 14;
		bomber_h_orig = this.bomber_h;
		if(Math.random() < 0.5) {
			dx = (float) (0.01 * w * (Math.random() + 0.5));
			x = -bomber_w;
		}
		else {
			dx = (float) (-0.01 * w * (Math.random() + 0.5));
			x = w;
		}
		y = (float) (h * (0.4 * Math.random()));
		x2 = x + bomber_w;
		y2 = y + this.bomber_h;
	}
	
	public void move() {
		x += dx;
		x2 = x + bomber_w;
		if(state > 2) dx -= dx * 0.05;
		if(luk > 0 && luk < (14)) {
			luk++;
			bomber_h = bomber_h_orig + luk;
			y2 = y + bomber_h;
		}
	}
	
	public boolean readyLuk() {
		if(luk == 0) luk = 1;
		if(luk == (14)) {
			return true;
		}
		return false;
	}
	
	public Rect getDstRect() {
		return new Rect((int)x, (int)y, (int)x2, (int)y2);
	}
	
	public Rect getSrcRect() {
		return new Rect(0, 0, bomber_w, bomber_h);
	}
}
