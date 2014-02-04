package com.felight.sandalwood;

import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.felight.sandalwood.pushnotification.ColudRegister;
import com.felight.sandalwood.pushnotification.RegisterTask;
import com.google.android.gms.ads.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.searchboxsdk.android.StartAppSearch;
import com.startapp.android.publish.StartAppAd;

public class Dashboard extends Activity {

	// gcm
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static String SENDER_ID = "682184441049";
	static final String TAG = "GCMDemo";
	public static GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	SharedPreferences prefs;
	Context context;
	public static String regid;

	// startapp
	private static final String MY_AD_UNIT_ID = "a152d92efa3a0de";
	private static final String STARTAPP_APP_ID = "201024664";
	private static final String STARTAPP_APP_DEVELOPER_ID = "101241823";
	private StartAppAd startAppAd;

	private AdView adView;
	public String BASE_URL = "http://sandalwood.oceanxen.com";
	public String DASHBOARD_URL = "http://sandalwood.oceanxen.com";
	public String CONTACT_URL = "https://oceanxen.typeform.com/to/S5fefT";
	public String APP_LINK = "https://play.google.com/store/apps/details?id=com.felight.sandalwood";

	private JavascriptInterface jsInterface;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();

		RegisterTask task = new RegisterTask(getBaseContext());
		task.execute(null, null);

		if (checkPlayServices()) {
			Log.i("nvinnay", "Play service available");

			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getRegistrationId(context);

			if (regid.isEmpty()) {
				registerInBackground();
			}

		} else {
			Log.i("nvinnay", "No Play service available");
		}

		// startapp sdk integration
		startAppAd = new StartAppAd(this);
		StartAppAd.init(this, STARTAPP_APP_DEVELOPER_ID, STARTAPP_APP_ID);
		StartAppSearch.init(this, STARTAPP_APP_DEVELOPER_ID, STARTAPP_APP_ID);

		setContentView(R.layout.dashboard_layout);

		// startapp
		startAppAd.showAd(); // show the ad
		startAppAd.loadAd(); // load the next ad

		// Create the adView.
		adView = new AdView(this);
		adView.setAdUnitId(MY_AD_UNIT_ID);
		adView.setAdSize(AdSize.BANNER);

		// Lookup your LinearLayout assuming it's been given
		// the attribute android:id="@+id/mainLayout".
		LinearLayout layout = (LinearLayout) findViewById(R.id.ad_container);

		// Add the adView to it.
		layout.addView(adView);

		// Initiate a generic request.
		AdRequest adRequest = new AdRequest.Builder().build();

		// Load the adView with the ad request.
		adView.loadAd(adRequest);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		WebView engine = (WebView) findViewById(R.id.web_engine);

		// Progress bar.
		// With full screen app, window progress bar (FEATURE_PROGRESS) doesn't
		// seem to show,
		// so we use an explicitly created one.
		final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressbar);

		engine.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				progressBar.setProgress(progress);
			}
		});

		engine.setWebViewClient(new FixedWebViewClient() {
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// jsInterface.enablePreferencesMenu = false;
				jsInterface.modalIsVisible = false;
				jsInterface.urlForSharing = null;
				progressBar.setVisibility(View.VISIBLE);
			}

			public void onPageFinished(WebView view, String url) {
				progressBar.setVisibility(View.GONE);
			}
		});

		WebSettings webSettings = engine.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setPluginState(WebSettings.PluginState.ON);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

		jsInterface = new JavascriptInterface();
		try {
			ComponentName comp = new ComponentName(this, Dashboard.class);
			PackageInfo pinfo = getPackageManager().getPackageInfo(
					comp.getPackageName(), 0);
			jsInterface.versionCode = pinfo.versionCode;
		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
		}

		engine.addJavascriptInterface(jsInterface, "androidlearnscripture");
		engine.loadUrl(BASE_URL);
		engine.setWebChromeClient(new WebChromeClient());
	}

	private WebView getEngine() {
		return (WebView) findViewById(R.id.web_engine);
	}

	public void onBackPressed() {
		WebView engine = getEngine();
		String url = engine.getUrl();
		if (jsInterface.modalIsVisible) {
			engine.loadUrl("javascript: learnscripture.hideModal();");
		} else if (url != null
				&& (url.equals(BASE_URL) || url.equals(DASHBOARD_URL) || !engine
						.canGoBack())) {
			// exit
			super.onBackPressed();
		} else {
			// go back a page, like normal browser
			engine.goBack();
		}

		super.onBackPressed();
		startAppAd.onBackPressed();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// MenuItem prefs = menu.findItem(R.id.preferences_menuitem);
		// if (prefs != null) {
		// prefs.setVisible(jsInterface.enablePreferencesMenu);
		// }
		super.onPrepareOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.dashboard_menuitem:
			getEngine().loadUrl(DASHBOARD_URL);
			return true;
		case R.id.refresh_menuitem:
			getEngine().reload();
			return true;
		case R.id.contact_menuitem:
			getEngine().loadUrl(CONTACT_URL);
			return true;
		case R.id.rate_this_app_menuitem:
			getEngine().loadUrl(APP_LINK);
			/*
			 * case R.id.share_url_menuitem: final String url =
			 * (jsInterface.urlForSharing != null ? jsInterface.urlForSharing :
			 * getEngine().getUrl()); Intent i = new Intent(Intent.ACTION_SEND);
			 * i.setType("text/plain"); i.putExtra(Intent.EXTRA_SUBJECT,
			 * "LearnScripture URL"); i.putExtra(Intent.EXTRA_TEXT, url);
			 * startActivity(Intent.createChooser(i, "Share URL"));
			 */
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class FixedWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.startsWith(BASE_URL) || url.startsWith("javascript:")) {
				// handle by the WebView
				return false;
			} else if (url.startsWith("mailto:")) {
				MailTo mt = MailTo.parse(url);
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL, new String[] { mt.getTo() });
				i.putExtra(Intent.EXTRA_SUBJECT, mt.getSubject());
				i.putExtra(Intent.EXTRA_CC, mt.getCc());
				i.putExtra(Intent.EXTRA_TEXT, mt.getBody());
				view.getContext().startActivity(i);
				view.reload();
				return true;
			} else {
				// Use external browser for anything not on this site
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				view.getContext().startActivity(i);
				return true;
			}
		}
	}

	// The methods of JavascriptInterface are called from javascript.
	// The attributes are accessed from the Dashboard class.
	// This is deliberately a dumb container class to stop possible
	// security issues of javascript controlling Java app.
	final class JavascriptInterface {
		public boolean enablePreferencesMenu = false;
		public boolean modalIsVisible = false;
		public int versionCode = 0;
		public String urlForSharing = null;

		public void setEnablePreferencesMenu() {
			enablePreferencesMenu = true;
		}

		public void setModalIsVisible(boolean visible) {
			modalIsVisible = visible;
		}

		// This is useful for allowing the web site to be able to detect
		// old app versions and prompt the user to upgrade.
		public int getVersionCode() {
			return versionCode;
		}

		public void setUrlForSharing(String url) {
			urlForSharing = url;
		}
	}

	@Override
	protected void onPause() {
		adView.pause();
		super.onPause();
		startAppAd.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		adView.resume();
		startAppAd.onResume();
	}

	@Override
	protected void onDestroy() {
		adView.destroy();
		super.onDestroy();
	}

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1)
						.show();
			} else {
				finish();
			}
			return false;
		}
		return true;
	}
	
	//gcm

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}
	
	
	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	public SharedPreferences getGCMPreferences(Context context) {
	    // This sample app persists the registration ID in shared preferences, but
	    // how you store the regID in your app is up to you.
	    return getSharedPreferences(Dashboard.class.getSimpleName(),
	            Context.MODE_PRIVATE);
	}
	
	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	public static int getAppVersion(Context context) {
	    try {
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}
	
	
	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		
	}

}
