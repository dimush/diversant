package org.softosaurus.diversantfree;

public class Bomb {
	float x, y, x2, y2, dx, dy;
	public int score = 15;
	int w, h;
	public Bomb(float x, float y, float dx, float dy, int w, int h) {
		this.x = x;
		this.y = y;		
		this.dx = dx;
		this.dy = dy;
		this.w = w;
		this.h = h;		
		x2 = x + w;
		y2 = y + h;
	}
	
	void move() {
		x += dx;
		dx -= 0.03 * dx;
		y += dy;
		dy += 0.02 * dy;
		x2 = x + w;
		y2 = y + h;
	}
}
