package org.softosaurus.diversantfree;

import java.util.Iterator;
import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnTouchListener;

public class MySurfaceView extends android.view.SurfaceView implements SurfaceHolder.Callback, Runnable, OnTouchListener {
	private static int SOUND_ID_HELIC = 0;
	private static int SOUND_ID_BOMBER = 1;
	private static int SOUND_ID_EXPLOSION = 2;
	private static int SOUND_ID_MAX = 3;
	private SurfaceHolder sh;
	private Bitmap bg_image;
	private Bitmap[] helic = new Bitmap[3];
	private Bitmap[] helic_back = new Bitmap[3];
	private Bitmap explosion_bmp;
	private Bitmap parash_bmp;
	private Bitmap paratruper_bmp;
	private Bitmap atom_expl_bmp;
	private Bitmap bomber_bmp, bomber_back_bmp;
	private Bitmap bomb_bmp;
	private Context context;	
	private SoundPool sp;
	private AudioManager audio;
	private int w = 0;
	private int h = 0;
	private Gun gun = null;
	private Vector<Shot> shots = new Vector<Shot>();
	private Vector<Bomb> bombs = new Vector<Bomb>();
	private int addShot = 0;
	private Vector<Helic> helics = new Vector<Helic>();
	private Vector<Bomber> bombers = new Vector<Bomber>();
	private Vector<Parash> parashuts = new Vector<Parash>();
	private long touchStart;
	private int score = 0;
	private int sum_shots = 0;
	private int sum_helics = 0;
	private int sum_bombers = 0;
	private int sum_parash = 0;
	private int sum_bombs = 0;
	private int[] soundID = new int[SOUND_ID_MAX];
	private double go_phase = 0;
	private RectF pauseButtonRect;
	private Paint p = new Paint();		
	private boolean isPaused = false;
	
	public boolean isPaused() {
		return isPaused;
	}

	public void setPaused(boolean isPaused) {
		this.isPaused = isPaused;
	}

	private boolean lastPaused = false;
	
	public MySurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		sh = getHolder();
		sh.addCallback(this);	
		this.context = context;		
		this.setOnTouchListener(this);
		/**
		 * Sounds
		 */
		sp = new SoundPool(4, android.media.AudioManager.STREAM_MUSIC, 0);				
		audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);			
		soundID[SOUND_ID_HELIC] = sp.load(context, R.raw.chopper, 1);		
		soundID[SOUND_ID_BOMBER] = sp.load(context, R.raw.bomber, 1);
		soundID[SOUND_ID_EXPLOSION] = sp.load(context, R.raw.explosion, 1);
		/**
		 * Bitmaps
		 */
		helic[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.helic1);
		helic[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.helic2);
		helic[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.helic3);
		helic_back[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.helic_back1);
		helic_back[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.helic_back2);
		helic_back[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.helic_back3);
		explosion_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.explosion);
		parash_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.parashutist);
		paratruper_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.paratruper);
		atom_expl_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.atom_expl);
		bomber_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.bomber);
		bomber_back_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.bomber_back);
		bomb_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.bomb);		
		p.setARGB(255, 255, 255, 255);
	}
	
	public int getScore() {
		return score;
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		long tmr = System.currentTimeMillis();
		while(true) {
			long new_tmr = System.currentTimeMillis();
			if(new_tmr - tmr > 50) {
				tmr = new_tmr;
				Canvas can = null;
				try {
					can = sh.lockCanvas();
					myDraw(can);
				}
				catch(Exception e) {
					// I don't know why...
				}
				finally {
					if(can != null) {
						sh.unlockCanvasAndPost(can);
					}
				}				
			}
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @return
	 */
	double getMaxHelics() {
		return 1 + Math.sqrt(score)/2;
	}
	
	/**
	 * Scores:  Bombers:
	 * @return
	 */
	double getMaxBombers() {
		return 1 + Math.sqrt(score - 450)/5;
	}
	
	double getParashProbability() {
		return 0.01 + getMaxHelics()*0.001;
	}
	
	double getBombProbability() {
		return 0.03 + getMaxBombers()*0.001;
	}
	
	private void drawBackGround() {
		/*
		 * Prepare BG Canvas
		 */
		Canvas bg_can = new Canvas(bg_image);
		p.setARGB(255, 0, 0, 200);
		bg_can.drawRect(bg_can.getClipBounds(), p);
		p.setARGB(255, 255, 255, 255);
		bg_can.translate(bg_can.getWidth(), 0);
		bg_can.rotate(90f);
		/**
		 * Draw the gun
		 */		
		p.setStrokeWidth(4f);
		bg_can.drawLine(gun.left, gun.top, gun.right, gun.top, p);
		bg_can.drawLine(gun.left, h, gun.left, gun.top, p);
		bg_can.drawLine(gun.right, h, gun.right, gun.top, p);
		bg_can.drawArc(new RectF(gun.cent - gun.radius, gun.top - gun.radius, gun.cent + gun.radius, gun.top + gun.radius), 
				180, 180, true, p);
		p.setStrokeWidth(0);
		/**
		 * Draw "pause" button
		 */					
		p.setTextAlign(Align.CENTER);
		p.setTextSize(w/30);
		pauseButtonRect = new RectF(1, 1, w/10, h/10);
		if(isPaused) {
			p.setARGB(255, 200, 100, 100);
			bg_can.drawRoundRect(pauseButtonRect, 15f, 15f, p);
		}
		p.setStyle(Paint.Style.STROKE);
		p.setARGB(255, 255, 255, 255);
		bg_can.drawRoundRect(pauseButtonRect, 15f, 15f, p);
		p.setStyle(Paint.Style.FILL);		
		bg_can.drawText("pause", w/20, h/15, p);		
	}
		
	public void myDraw(Canvas can) {
		if(can == null) return;				
		if(bg_image == null || can.getWidth() != bg_image.getWidth() || can.getHeight() != bg_image.getHeight()) {			
			bg_image = Bitmap.createBitmap(can.getWidth(), can.getHeight(), Bitmap.Config.ARGB_8888);
			w = can.getHeight();
			h = can.getWidth();
			gun = new Gun(w, h);
			drawBackGround();
			gun.angle = 1.2;
			//score = 501;
		}	
		if(lastPaused != isPaused) {
			drawBackGround();
			lastPaused = isPaused;
		}
		/**
		 * Background
		 */
		can.drawBitmap(bg_image, new Rect(0, 0, bg_image.getWidth(), bg_image.getHeight()), 
				new Rect(0, 0, can.getWidth(), can.getHeight()), p);
		can.translate(can.getWidth(), 0);
		can.rotate(90f);
		/**
		 * Draw the gun barrel
		 */				
		p.setStrokeWidth(4f);
		can.drawLine(gun.cent, gun.top, gun.getBarrelX(0), gun.getBarrelY(0), p);
		p.setStrokeWidth(0);
		/**
		 * Move the Shots
		 */
		Iterator<Shot> it = shots.iterator();
		while(it.hasNext()) {			
			Shot shot = it.next();
			if(!isPaused) {
				shot.x += shot.dx;
				shot.y += shot.dy;
			}
			if(shot.x < 0 || shot.x > w || shot.y < 0) {
				try {
					it.remove();
				} catch(Exception e) {};
			}
			else {
				/**
				 * Draw the Shots
				 */
				//can.drawPoint(shot.x, shot.y, p);
				//can.drawBitmap(snarjad_bmp, shot.x, shot.y, p);
				can.drawCircle(shot.x, shot.y, 3, p);
				/**
				 * Get the Helicopter Target?
				 */
				Iterator<Helic> ith = helics.iterator();
				while(ith.hasNext()) {
					Helic hel = ith.next();
					if(hel.state > 2) continue;
					if(shot.x > hel.x && shot.x < hel.x2 && shot.y > hel.y && shot.y < hel.y2) {
						if(hel.state < 3) {
							score += hel.score;
							hel.state = 3;
							// change "Helicopter" size
							hel.helic_w = explosion_bmp.getWidth();
							hel.y2 = hel.y + explosion_bmp.getHeight();
							sp.play(soundID[SOUND_ID_EXPLOSION], 0.7f, 0.7f, 1, 0, 1f);
						}
						it.remove();
						break;
					}
				}
				/**
				 * Get the Bomber Target?
				 */
				Iterator<Bomber> itb = bombers.iterator();
				while(itb.hasNext()) {
					Bomber bom = itb.next();
					if(bom.state > 2) continue;
					if(shot.x > bom.x && shot.x < bom.x2 && shot.y > bom.y && shot.y < bom.y2) {
						if(bom.state < 3) {
							score += bom.score;
							bom.state = 3;
							// change "Bomber" size
							bom.bomber_w = explosion_bmp.getWidth();
							bom.y -= explosion_bmp.getHeight()/2;
							bom.y2 = bom.y + explosion_bmp.getHeight();
							sp.play(soundID[SOUND_ID_EXPLOSION], 0.7f, 0.7f, 1, 0, 1f);
						}
						it.remove();
						break;
					}
				}
				/**
				 * Get the Parashut Target?
				 */
				Iterator<Parash> itp = parashuts.iterator();
				while(itp.hasNext()) {
					Parash par = itp.next();
					if(shot.x > par.x && shot.x < par.x2 && shot.y > par.y && shot.y < par.y2) {
						score += par.score;
						itp.remove();
						break;
					}
				}
				/**
				 * Get the Bomb Target?
				 */
				Iterator<Bomb> itbm = bombs.iterator();
				while(itbm.hasNext()) {
					Bomb bom = itbm.next();
					if(shot.x > bom.x && shot.x < bom.x2 && shot.y > bom.y && shot.y < bom.y2) {
						score += bom.score;
						itbm.remove();
						break;
					}
				}
			}
		}		
		/**
		 * Fire
		 */
		while(addShot > 0) {
			if(isPaused) break;
			addShot--;
			score -= shots.size();
			if(score < 0) score = 0;
			shots.add(new Shot(gun.getBarrelX(addShot), gun.getBarrelY(addShot), gun.angle, gun.barrel*0.1));
		}
		/**
		 * Add new Helics
		 */
		if(!isPaused) {
			if((gun.state == Gun.STATE_HELICS && bombers.size() == 0) || (gun.state == Gun.STATE_HEL_AND_BOMB)) {
				if(helics.size() < getMaxHelics()) {
					if(Math.random() < 0.05) {				
						helics.add(new Helic(w, h, helic[0].getWidth(), helic[0].getHeight()));		
						sp.play(soundID[SOUND_ID_HELIC], 0.1f, 0.1f, 1, 15, 1f);
					}
				}
			}
		}
		/**
		 * Add new Bombers
		 */
		if(!isPaused) {
			if((gun.state == Gun.STATE_BOMBERS && helics.size() == 0) || (gun.state == Gun.STATE_HEL_AND_BOMB)) {
				if(bombers.size() < getMaxBombers()) {
					if(Math.random() < 0.05) {				
						Bomber bom = new Bomber(w, h, bomber_bmp.getWidth(), bomber_bmp.getHeight());
						bombers.add(bom);
						sp.play(soundID[SOUND_ID_BOMBER], 0.3f, 0.3f, 1, 15, 1f);
						if(Math.random() < 0.33) {
							// open the bomb chamber
							bom.readyLuk();
						}
					}
				}
			}
		}
		/**
		 * Move Helics
		 */		
		Iterator<Helic> ith = helics.iterator();
		while(ith.hasNext()) {			
			Helic hel = ith.next();
			if(!isPaused) hel.move();
			switch(hel.state) {
				case 0:
				case 1:
				case 2: {
					if(hel.dx > 0)
						can.drawBitmap(helic_back[hel.state++], hel.x, hel.y, p);
					else
						can.drawBitmap(helic[hel.state++], hel.x, hel.y, p);
					if(hel.state > 2) hel.state = 0;
					/**
					 * Drop Parashuts
					 */
					if(!isPaused) {
						if(gun.state == Gun.STATE_HELICS || gun.state == Gun.STATE_HEL_AND_BOMB) {
							if(Math.random() < getParashProbability()) {
								// Helicopter still visible
								if(hel.x > 0 && hel.x2 < w) {
									parashuts.add(new Parash(w, h, parash_bmp.getWidth(), parash_bmp.getHeight(),
											(float)((hel.x + hel.x2) / 2), hel.y2, gun));
								}
							}
						}
					}
					break;
				}
				case 3: {
					p.setARGB(hel.explos, 255, 255, 255);
					hel.explos -= hel.explos * 0.1;					
					can.drawBitmap(explosion_bmp, hel.x, hel.y, p);
					if(hel.explos < 5) ith.remove();
					p.setARGB(255, 255, 255, 255);
					/**
					 * Explosion get the Parashut Target?
					 */
					Iterator<Parash> itp = parashuts.iterator();
					while(itp.hasNext()) {
						Parash par = itp.next();
						if(hel.x <= par.x && hel.x2 >= par.x2 && hel.y <= par.y && hel.y2 >= par.y2) {
							score += par.score;
							itp.remove();
							break;
						}
					}
					/**
					 * Explosion get the Helicopter Target?
					 */
					Iterator<Helic> ith2 = helics.iterator();
					while(ith2.hasNext()) {
						Helic hel2 = ith2.next();
						if((hel.x > hel2.x && hel.x < hel2.x2) || (hel.x2 > hel2.x && hel.x2 < hel2.x2)) {
							if((hel.y > hel2.y && hel.y < hel2.y2) || (hel.y2 > hel2.y && hel.y2 < hel2.y2)) {
								if((hel.dx < 0 && hel2.dx > 0) || (hel.dx > 0 && hel2.dx < 0)) {
									if(hel2.state < 3) {
										score += hel2.score;
										hel2.state = 3;
										// change "Helicopter" size
										hel2.helic_w = explosion_bmp.getWidth();
										hel2.y2 = hel2.y + explosion_bmp.getHeight();
										sp.play(soundID[SOUND_ID_EXPLOSION], 0.7f, 0.7f, 1, 0, 1f);
									}
								}
							}							
						}
					}
					break;
				}				
			} // switch
			if(hel.dx > 0) {
				if(hel.x > w) ith.remove();
			}
			else {
				if(hel.x < -helic[0].getWidth()) ith.remove();
			}
		} // while(ith.hasNext())
		/**
		 * Move Bombers
		 */		
		Iterator<Bomber> itb = bombers.iterator();
		while(itb.hasNext()) {			
			Bomber bom = itb.next();
			if(!isPaused) bom.move();
			switch(bom.state) {
				case 0:
				case 1:
				case 2: {
					if(bom.dx > 0)
						can.drawBitmap(bomber_bmp, bom.getSrcRect(), bom.getDstRect(), p);
					else {						
						can.drawBitmap(bomber_back_bmp, bom.getSrcRect(), bom.getDstRect(), p);
					}
					/**
					 * Drop Bomb
					 */
					if(!isPaused) {
						if(gun.state == Gun.STATE_BOMBERS || gun.state == Gun.STATE_HEL_AND_BOMB) {
							if(Math.random() < getBombProbability()) {
								// Bomber still visible
								if(bom.x > 0 && bom.x2 < w) {
									// Luk opened?
									if(bom.readyLuk()) {
										bombs.add(new Bomb((bom.x + bom.x2) / 2, bom.y2, bom.dx, (float) (0.003 * h),
												bomb_bmp.getWidth(), bomb_bmp.getHeight()));
									}
								}
							}
						}
					}
					break;
				}
				case 3: {
					p.setARGB(bom.explos, 255, 255, 255);
					bom.explos -= bom.explos * 0.1;					
					can.drawBitmap(explosion_bmp, bom.x, bom.y, p);
					if(bom.explos < 5) itb.remove();
					p.setARGB(255, 255, 255, 255);							
					break;
				}
			} // switch
			if(bom.dx > 0) {
				if(bom.x > w) itb.remove();
			}
			else {
				if(bom.x < -bomber_bmp.getWidth()) itb.remove();
			}
		} // while(itb.hasNext())				
		/**
		 * Move Parashuts
		 */
		Iterator<Parash> it_parash = parashuts.iterator();
		while(it_parash.hasNext()) {			
			Parash par = it_parash.next();
			if(!isPaused) par.move();
			if(par.state == 0)
				can.drawBitmap(parash_bmp, par.x, par.y, p);
			else {
				can.drawBitmap(paratruper_bmp, par.x, par.y, p);	
			}
		}
		/**
		 * Move Bombs
		 */
		Iterator<Bomb> it_bomb = bombs.iterator();
		while(it_bomb.hasNext()) {			
			Bomb bomb = it_bomb.next();
			if(!isPaused) bomb.move();
			can.drawBitmap(bomb_bmp, bomb.x, bomb.y, p);	
			/**
			 * Get the Gun?
			 */
			if(bomb.x > gun.left && bomb.x < gun.right) {
				if(bomb.y > gun.top) {
					gun.setState(Gun.STATE_SIRENE);
					it_bomb.remove();
				}
			}
			if(bomb.y > h) it_bomb.remove();
		}
		/**
		 * Sirene
		 */
		if(gun.state == Gun.STATE_SIRENE) {
			gun.state = Gun.STATE_PREPARE_ATOM_EXPLOSION;
			MediaPlayer mp = MediaPlayer.create(context, R.raw.sirene);
			mp.start();
			/**
			 * Change the speed of Choppers and Planes.
			 * They must leave the dangerous zone as fast as possible.
			 */
			ith = helics.iterator();
			while(ith.hasNext()) {
				Helic hel = ith.next();
				hel.dx *= 5;
			}
			itb = bombers.iterator();
			while(itb.hasNext()) {
				Bomber bom = itb.next();
				bom.dx *= 5;
			}
		}
		/**
		 * Prepare Atom Explosion
		 */
		if(gun.state == Gun.STATE_PREPARE_ATOM_EXPLOSION) {
			if(helics.size() == 0 && bombers.size() == 0) {
				gun.state = Gun.STATE_EXPLOSION;
				MediaPlayer mp = MediaPlayer.create(context, R.raw.nuke);
				mp.start();
				parashuts.clear();
				/**
				 * Repaint the Background Image
				 */
				Canvas bg_can = new Canvas(bg_image);
				p.setARGB(255, 0, 0, 200);
				bg_can.drawRect(bg_can.getClipBounds(), p);
				p.setARGB(255, 255, 255, 255);
				bg_can.translate(bg_can.getWidth(), 0);
				bg_can.rotate(90f);
				/**
				 * Draw the gun
				 */
				p.setStrokeWidth(4f);
				bg_can.drawLine(gun.left, h, gun.right, gun.top, p);
				bg_can.drawLine(gun.right, h, gun.right, gun.top, p);
				bg_can.drawArc(new RectF(gun.cent - gun.radius, gun.top - gun.radius, gun.cent + gun.radius, gun.top + gun.radius), 
						30, 180, true, p);
				p.setStrokeWidth(0);
			}
		}
		/**
		 * Atom Explosion
		 */
		if(gun.state == Gun.STATE_EXPLOSION) {	
			p.setARGB((int) gun.atom_brightness, 255, 255, 255);
			can.drawBitmap(atom_expl_bmp, new Rect(0, 0, atom_expl_bmp.getWidth() - 1, atom_expl_bmp.getHeight() - 1), 
					gun.getAtomExplosRect(), p);
			//can.drawBitmap(atom_expl_bmp, gun.cent, gun.top, p);
			p.setARGB(255, 255, 255, 255);
			if(gun.atom_brightness < 5) {
				gun.state = Gun.STATE_GAME_OVER;
			}
		}
		if(gun.state == Gun.STATE_GAME_OVER) {
			/**
			 * Draw Game Over
			 */
			p.setTextSize(w/10);
			p.setTextAlign(Align.CENTER);
			can.drawText("Game Over", (int)(w/2 + w * Math.sin(go_phase)/32), (int)(h/2 + h * Math.sin(go_phase * 3.14)/32), p);
			go_phase += Math.PI/100;
		}
		/**
		 * Draw scores
		 */
		p.setTextSize(can.getHeight()/16);
		p.setTextAlign(Align.CENTER);
		can.drawText(String.valueOf(score), gun.cent, h - 2, p);
		/**
		 * Change Game Flow
		 */
		if(gun.state == Gun.STATE_HELICS && score > 500 && score < 550) {
			gun.state = Gun.STATE_BOMBERS;
			//sp.play(soundID[SOUND_ID_BOMBER_APPROACH], 0.5f, 0.5f, 1, 0, 1f);
			MediaPlayer mp = MediaPlayer.create(context, R.raw.planes_approach);
			mp.start();
		}
		if(gun.state == Gun.STATE_BOMBERS && (score > 600 || score < 450)) {
			gun.state = Gun.STATE_HELICS;
		}
		if(gun.state == Gun.STATE_HELICS && score > 1000) {
			gun.state = Gun.STATE_HEL_AND_BOMB;
			//sp.play(soundID[SOUND_ID_BOMBER_APPROACH], 0.5f, 0.5f, 1, 0, 1f);
			MediaPlayer mp = MediaPlayer.create(context, R.raw.planes_approach);
			mp.start();
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {		
		/**
		 * Attention!
		 * Display is not rotated, but the coordinate system is!
		 * So getY() should be used as getX() and vice versa 
		 */
		switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				touchStart = System.currentTimeMillis();
			}
			case MotionEvent.ACTION_MOVE: {
				double dx = event.getY() - gun.cent;
				double dy = event.getX() - gun.h;
				if(Math.abs(dy) < 1) {
					gun.angle = Math.PI/2;
				}
				else {
					gun.angle = Math.atan2(dy, dx);
				}
				if(gun.angle > Math.PI - 4*Math.PI/180) gun.angle = Math.PI - 4*Math.PI/180;
				if(gun.angle < 4*Math.PI/180) gun.angle = 4*Math.PI/180;
				break;
			}
			case MotionEvent.ACTION_UP: {
				if(System.currentTimeMillis() - touchStart < 250) {
					float x = event.getY();
					float y = h - event.getX();
					// pause button?
					if(x >= pauseButtonRect.left && x <= pauseButtonRect.right &&
					   y >= pauseButtonRect.top && y <= pauseButtonRect.bottom) {
						isPaused = !isPaused;						
					}
					else {
						// fire
						addShot++;
						if(gun.state == Gun.STATE_GAME_OVER) {
							reset();
						}
					}
				}
				break;
			}
		}
		/*
		if((event.getX() < gun.h + gun.barrel) && 
				(event.getY() > gun.left && event.getY() < gun.right)) {
			// fire
			addShot++;			
		}
		else {
			double dx = event.getY() - gun.cent;
			double dy = event.getX() - gun.h;
			if(Math.abs(dy) < 1) {
				gun.angle = Math.PI/2;
			}
			else {
				gun.angle = Math.atan2(dy, dx);
			}
			//if(event.getY() < gun.cent) {		
				// rotate gun to the left
			//	gun.angle += Math.PI / 180;
			if(gun.angle > Math.PI - 4*Math.PI/180) gun.angle = Math.PI - 4*Math.PI/180;
			//}
			//else {
				// rotate gun to the right
			//	gun.angle -= Math.PI / 180;
			if(gun.angle < 4*Math.PI/180) gun.angle = 4*Math.PI/180;
			//}
		}
		//Log.i("Diversant", "angle: " + gun.angle);*/
		return true;
	}

	public void reset() {
		score = 0;
		gun.reset();
		parashuts.clear();
		helics.clear();
		bg_image = null;
	}
}


