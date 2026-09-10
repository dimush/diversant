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
	private final AtomicBoolean bannerCreated = new AtomicBoolean(false);
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

        msv = (MySurfaceView)this.findViewById(R.id.mySurfaceView1);
        msv.setGameStateListener(this::onGameplayActiveChanged);
        // Keep the score above whatever the banner actually occupies: an unfilled
        // banner measures 0 high, so the score must not be pushed up for nothing.
        bannerLayout.addOnLayoutChangeListener(
                (v, l, t, r, b, ol, ot, or_, ob) -> msv.setBannerInset(
                        v.getVisibility() == View.VISIBLE ? b - t : 0));
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        new Thread(msv).start();

        // GDPR / US-states consent via User Messaging Platform, then ads.
        ConsentRequestParameters params = new ConsentRequestParameters.Builder().build();
        consentInformation = UserMessagingPlatform.getConsentInformation(this);
        consentInformation.requestConsentInfoUpdate(this, params,
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(this, formError -> onConsentResolved()),
                requestError -> {
                    // Consent info unavailable (e.g. offline); ads may still be allowed.
                    onConsentResolved();
                });
        onConsentResolved();

    }

	/** Consent flow finished (or was not needed): initialise the SDK and, if the
	 *  banner is already on screen, create and load it right away. */
	private void onConsentResolved() {
		if (!consentInformation.canRequestAds()) return;
		initializeMobileAds();
		if (bannerLayout.getVisibility() == View.VISIBLE) {
			createAndLoadBannerIfNeeded();
		}
	}

	private void initializeMobileAds() {
		if (!adsInitialized.compareAndSet(false, true)) return;
		MobileAds.initialize(this, initializationStatus -> { });
	}

	/**
	 * Creates the AdView and issues the first request. Only ever called while the
	 * banner container is visible, so no impression is served off screen.
	 */
	private void createAndLoadBannerIfNeeded() {
		if (!adsInitialized.get()) return;
		if (!bannerCreated.compareAndSet(false, true)) return;
		mAdView = new AdView(this);
		mAdView.setAdUnitId("ca-app-pub-1665272374483034/2280326108");
		mAdView.setAdSize(getAdaptiveBannerSize());
		bannerLayout.addView(mAdView);
		mAdView.loadAd(new AdRequest.Builder().build());
	}

	/**
	 * The banner is overlaid on the bottom of the playfield and shown only while the
	 * game is paused or over; during play it is GONE and the AdView is paused, so it
	 * neither refreshes nor records impressions.
	 */
	private void onGameplayActiveChanged(boolean active) {
		if (active) {
			if (mAdView != null) mAdView.pause();
			bannerLayout.setVisibility(View.GONE);
			msv.setBannerInset(0);
		} else {
			bannerLayout.setVisibility(View.VISIBLE);
			createAndLoadBannerIfNeeded();
			if (mAdView != null) mAdView.resume();
		}
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
		// Only resume (and thus refresh) the banner when it is actually on screen.
		if (mAdView != null && bannerLayout.getVisibility() == View.VISIBLE) mAdView.resume();
	}

	@Override
	protected void onDestroy() {
		if (mAdView != null) mAdView.destroy();
		super.onDestroy();
	}
}
