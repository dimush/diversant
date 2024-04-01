package org.softosaurus.diversantfree;

public class Parash {
	int state = 0;
	float x, y;
	float dy, dx, i_reg;
	private int w, h;
	float x2, y2;
	private int parash_w, parash_h;
	public int score = 2;
	Gun gun;
	
	public Parash(int w, int h, int parash_w, int parash_h, float x, float y, Gun gun) {
		this.gun = gun;
		this.w = w;
		this.h = h;	
		this.parash_w = parash_w;
		this.parash_h = parash_h;
		this.x = x;
		this.y = y;
		x2 = x + parash_w;
		y2 = y + parash_h;
		dy = (float) (0.002 * h);
	}
	
	public void move() {
		y += dy;		
		y2 = y + parash_h;
		if(x2 >= gun.left && x <= gun.right) {
			if(y2 > gun.top) {
				y2 = gun.top;
				y = gun.top - parash_h;
				if(state != 1) {
					state = 1;
					gun.setState(Gun.STATE_READY_TO_DIE);
				}
			}			
		}
		else {
			if(y2 > h) {
				y2 = h;
				y = h - parash_h;
				if(state != 1) {
					state = 1;
					if(x < gun.cent) {
						gun.parash_left++;
						if(gun.parash_left >= 4)
							gun.setState(Gun.STATE_READY_TO_DIE);
					} else {
						gun.parash_right++;
						if(gun.parash_right >= 4)
							gun.setState(Gun.STATE_READY_TO_DIE);
					}
				}
			}
		}
		if(state == 1 && gun.state >= Gun.STATE_READY_TO_DIE) {
			i_reg += gun.cent - x;
			dx = (i_reg / 100) + (gun.cent - x)/10;
			x += dx;
			x2 += dx;
			if(Math.abs(x - gun.cent) < 6) gun.setState(Gun.STATE_SIRENE);
		}
	}
}
