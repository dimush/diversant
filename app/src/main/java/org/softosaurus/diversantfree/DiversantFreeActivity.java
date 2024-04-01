package org.softosaurus.diversantfree;

import java.nio.charset.Charset;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DiversantFreeActivity extends Activity {
	private MySurfaceView msv;
	private AdView mAdView;
	static final int DIALOG_HELP = 1;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);        
        setContentView(R.layout.main);
		MobileAds.initialize(this, new OnInitializationCompleteListener() {
			@Override
			public void onInitializationComplete(InitializationStatus initializationStatus) {
			}
		});
        msv = (MySurfaceView)this.findViewById(R.id.mySurfaceView1);
        Thread thr = new Thread(msv);
        thr.start();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        // Create the adView
		mAdView = new AdView(this);
		mAdView.setAdSize(AdSize.SMART_BANNER);
		// Test banner
		//mAdView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
		mAdView.setAdUnitId("ca-app-pub-1665272374483034/2280326108");
        LinearLayout bannerLayout = (LinearLayout)findViewById(R.id.linearLayout1);
        // Add the adView to it
        bannerLayout.addView(mAdView);
        // Initiate a generic request to load it with an ad
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);
        //msv.setPaused(true);
        /*this.showDialog(DIALOG_HELP);
        (new Thread(new Runnable() {
			@Override
			public void run() {
				while(!Thread.interrupted()) {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {}
					if((!msv.isPaused()) && msv.getScore() > 100 && Math.random() > 0.99) {
						msv.setPaused(true);
						msv.post(new Runnable() {
							public void run() {
								showDialog(DIALOG_HELP);
							}
						});						
					}
				}
			}        	
        })).start();*/
    }
    
    @Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {			
			case DIALOG_HELP: {
				Dialog dialog = new Dialog(DiversantFreeActivity.this);
				dialog.setContentView(R.layout.help);
				dialog.setTitle("Help");
				Button ok_button = (Button) dialog.findViewById(R.id.button1);
				ok_button.setOnClickListener(new OnClickListener() {					
					@Override
					public void onClick(View v) {
						removeDialog(DIALOG_HELP);
						msv.setPaused(false);
					}
				});
				Button buy_button = (Button) dialog.findViewById(R.id.button2);
				buy_button.setOnClickListener(new OnClickListener() {					
					@Override
					public void onClick(View v) {
						removeDialog(DIALOG_HELP);
						Intent intent = new Intent(Intent.ACTION_VIEW); 
						intent.setData(Uri.parse("market://details?id=org.softosaurus.diversant")); 
						startActivity(intent);
					}
				});
				TextView help_textView = (TextView) dialog.findViewById(R.id.textView2);
				help_textView.setText(getText(R.string.help_text));
				return dialog;			
			}			
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		// TODO Auto-generated method stub
		super.onPrepareDialog(id, dialog);
	}
}