package org.softosaurus.diversantfree;

public class Shot {
	float x, y, dx, dy;
	public Shot(float x, float y, double angle, double speed) {
		this.x = x;
		this.y = y;
		dx = (float) (Math.cos(angle) * speed);
		dy = (float) (-Math.sin(angle) * speed);
	}
}
