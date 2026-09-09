package org.softosaurus.diversantfree;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.atomic.AtomicBoolean;

public class DiversantFreeActivity extends Activity {
	private MySurfaceView msv;
	private AdView mAdView;
	private LinearLayout bannerLayout;
	private ConsentInformation consentInformation;
	private final AtomicBoolean adsInitialized = new AtomicBoolean(false);
	static final int DIALOG_HELP = 1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);

        // Android 15+ (target 35+) draws edge-to-edge: keep content out of the system bars.
        View root = findViewById(R.id.rootLayout);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        bannerLayout = findViewById(R.id.linearLayout1);

        // GDPR / US-states consent via User Messaging Platform, then ads.
        ConsentRequestParameters params = new ConsentRequestParameters.Builder().build();
        consentInformation = UserMessagingPlatform.getConsentInformation(this);
        consentInformation.requestConsentInfoUpdate(this, params,
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(this, formError -> {
                    if (consentInformation.canRequestAds()) {
                        initializeAds();
                    }
                }),
                requestError -> {
                    // Consent info unavailable (e.g. offline); ads may still be allowed.
                    if (consentInformation.canRequestAds()) {
                        initializeAds();
                    }
                });
        if (consentInformation.canRequestAds()) {
            initializeAds();
        }

        msv = (MySurfaceView)this.findViewById(R.id.mySurfaceView1);
        Thread thr = new Thread(msv);
        thr.start();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

	private void initializeAds() {
		if (!adsInitialized.compareAndSet(false, true)) return;
		MobileAds.initialize(this, initializationStatus -> { });
		mAdView = new AdView(this);
		mAdView.setAdUnitId("ca-app-pub-1665272374483034/2280326108");
		mAdView.setAdSize(getAdaptiveBannerSize());
		bannerLayout.addView(mAdView);
		mAdView.loadAd(new AdRequest.Builder().build());
	}

	private AdSize getAdaptiveBannerSize() {
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int adWidth = (int) (metrics.widthPixels / metrics.density);
		return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
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
	protected void onPause() {
		super.onPause();
		if (mAdView != null) mAdView.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mAdView != null) mAdView.resume();
	}

	@Override
	protected void onDestroy() {
		if (mAdView != null) mAdView.destroy();
		super.onDestroy();
	}
}
