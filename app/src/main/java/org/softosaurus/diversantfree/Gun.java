package org.softosaurus.diversantfree;

import android.graphics.Rect;
import android.graphics.RectF;

public class Gun {
	public static final int STATE_HELICS = 0;
	public static final int STATE_BOMBERS = 1;
	public static final int STATE_HEL_AND_BOMB = 2;
	public static final int STATE_READY_TO_DIE = 3;
	public static final int STATE_SIRENE = 4;
	public static final int STATE_PREPARE_ATOM_EXPLOSION = 5;
	public static final int STATE_EXPLOSION = 6;
	public static final int STATE_GAME_OVER = 7;
	int w, h;
	int cent, top, bottom;
	int left, right;
	int radius, barrel;
	double angle = 0;
	int state = STATE_HELICS;
	int parash_left = 0;
	int parash_right = 0;
	float atom_width = 5, atom_height = 5, atom_brightness = 255;
	
	public Gun(int dispWidth, int dispHight) {
		h = dispHight / 10;
		w = 3 * h;
		left = dispWidth/2 - w/2;
		right = dispWidth/2 + w/2;
		cent = dispWidth/2;
		top = dispHight - h;
		bottom = dispHight;
		radius = w/5;
		barrel = radius * 2;
	}
	
	float getBarrelX(int offset) {
		return (float) ((barrel - offset) * Math.cos(angle) + cent);
	}
	
	public void setState(int new_state) {
		if(state < new_state) state = new_state;
	}
	
	float getBarrelY(int offset) {
		return (float) (top - (barrel - offset) * Math.sin(angle));
	}

	public Rect getAtomExplosRect() {
		atom_width += (2*w - atom_width) / 20; 
		atom_height = (float) (atom_width * 1.5);
		if(atom_width > 1.9 * w) {
			if(atom_brightness > 0) atom_brightness -= atom_brightness / 10;
		}
		return new Rect((int)(cent - atom_width/2), (int)(top - atom_height), (int)(cent + atom_width/2), bottom);		
	}

	public void reset() {
		state = STATE_HELICS;
		parash_left = 0;
		parash_right = 0;
		atom_width = 5;
		atom_height = 5;
		atom_brightness = 255;
	}
}
