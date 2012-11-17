package com.dotview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Contacts.People;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amap.mapapi.core.AMapException;
import com.amap.mapapi.core.GeoPoint;
import com.amap.mapapi.geocoder.Geocoder;
import com.amap.mapapi.location.LocationManagerProxy;
import com.amap.mapapi.location.LocationProviderProxy;
import com.amap.mapapi.map.MapActivity;
import com.amap.mapapi.map.MapController;
import com.amap.mapapi.map.MapView;
import com.dotview.Common.CrashHandler;
import com.dotview.Common.DBHelper;
import com.dotview.Common.MapContacts;
import com.dotview.Common.Marker;
import com.dotview.Common.User;
import com.dotview.CustomBalloon.CustomItemizedOverlay;
import com.dotview.CustomBalloon.CustomOverlayItem;
import com.dotview.mapviewutil.GeoItem;
import com.dotview.mapviewutil.markerclusterer.GeoClusterer;
import com.dotview.mapviewutil.markerclusterer.MarkerBitmap;
import com.dotview.AboutUsActivity;

public class MainActivity extends MapActivity {
	private MapView mMapView;
	private MapController mMapController;
	private GeoPoint point;
	private Context context;

	private GeoClusterer clusterer;
	private int markerId = 0;
	private int contactsCount = 0;

	private Thread mBackground1;
	private Thread mBackground2;
	private String TAG = "FriendsOnMapLocation";
	private ProgressBar progressBar;

	private List<Marker> markerList;

	protected static final int GUI_COMPLETE = 0x1110;
	protected static final int GUI_THREADING = 0x122;

	float screenDensity = 0;
	int timer = 0;
	private int showType = 1;
	ProgressDialog dialog;

	CustomItemizedOverlay<CustomOverlayItem> itemizedOverlay;
	
	private LocationManagerProxy locationManager = null;
	private static final long mLocationUpdateMinTime = 0;
	private static final float mLocationUpdateMinDistance = 0;
	Handler myhandler;
	// marker icons
	private List<MarkerBitmap> markerIconBmps_ = new ArrayList<MarkerBitmap>();

	@Override
	/** 
	 *ÏÔÊ¾Õ¤¸ñµØÍ¼£¬ÆôÓÃÄÚÖÃËõ·Å¿Ø¼þ£¬²¢ÓÃMapController ¿ØÖÆµØÍ¼µÄÖÐÐÄµã¼°Zoom ¼¶±ð  
	 */
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		context = this;

		CrashHandler crashHandler = CrashHandler.getInstance();
		// 注册crashHandler
		crashHandler.init(getApplicationContext());

		initMap();
		initIcons();

		locationManager = LocationManagerProxy.getInstance(context);
		// progressBar = (ProgressBar) findViewById(R.id.progress1);

		myhandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case GUI_COMPLETE:
					dialog.dismiss();
					Thread.currentThread().interrupt();
					break;
				case GUI_THREADING:
					dialog.setProgress((Integer) msg.obj);
					break;
				}

				// dialog.setProgress((Integer) msg.obj);
				super.handleMessage(msg);
			}
		};

		if (mBackground1 == null) {
			showLoading();
			mBackground1 = new Thread(new MyThread(1));
			mBackground1.start();
		}

 
		Drawable drawable = getResources().getDrawable(R.drawable.marker2);
		
		itemizedOverlay = new CustomItemizedOverlay<CustomOverlayItem>(
				drawable, mMapView);
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
	}

	private void initMap() {
		mMapView = (MapView) findViewById(R.id.mapView);
		mMapView.setBuiltInZoomControls(true);
		mMapController = mMapView.getController();
		point = new GeoPoint((int) (29.840 * 1E6), (int) (116.696 * 1E6));
		mMapController.setCenter(point);
		mMapController.setZoom(5);
	}

	private void initIcons() {
		// prepare for marker icons.
		markerIconBmps_.add(new MarkerBitmap(BitmapFactory.decodeResource(
				getResources(), R.drawable.marker2), BitmapFactory
				.decodeResource(getResources(), R.drawable.marker2), new Point(
				16, 16), 14, 2));

		// small icon for maximum 10 items
		markerIconBmps_.add(new MarkerBitmap(BitmapFactory.decodeResource(
				getResources(), R.drawable.balloon_s_n), BitmapFactory
				.decodeResource(getResources(), R.drawable.balloon_s_n),
				new Point(16, 16), 14, 10));
		// large icon. 100 will be ignored.
		markerIconBmps_.add(new MarkerBitmap(BitmapFactory.decodeResource(
				getResources(), R.drawable.balloon_l_n), BitmapFactory
				.decodeResource(getResources(), R.drawable.balloon_l_n),
				new Point(24, 24), 16, 100));
	}

	/**
	 * onCreateOptionsMenu handler
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem menu_Revert = menu.add(0, 1, 0, "加载联系人");
		menu_Revert.setIcon(R.drawable.load);

		MenuItem test = menu.add(0, 2, 0, "聚合显示");
		test.setIcon(R.drawable.cluster);

		MenuItem about = menu.add(0, 3, 0, "关于");
		about.setIcon(R.drawable.about);
		return true;
	}
	// sample geo items
	private final GeoItem[] geoItems_ = {
		new GeoItem(0,(int)(37.448743*1E6),(int)(-122.171938*1E6),"aa","bb"),
		new GeoItem(1,(int)(37.427205999999998*1E6),(int)(-122.16911399999999*1E6),"aa","bb"),
		new GeoItem(2,(int)(37.45919*1E6),(int)(-122.105645*1E6),"aa","bb"),
		new GeoItem(3,(int)(37.447453000000003*1E6),(int)(-122.104304*1E6),"aa","bb"),
		new GeoItem(4,(int)(37.414738*1E6),(int)(-122.18315*1E6),"aa","bb"),
		new GeoItem(5,(int)(37.429670000000002*1E6),(int)(-122.173258*1E6),"aa","bb"),
		new GeoItem(6,(int)(37.427536000000003*1E6),(int)(-122.16689599999999*1E6),"aa","bb"),
		new GeoItem(7,(int)(37.423411999999999*1E6),(int)(-122.169127*1E6),"aa","bb"),
	};
	/**
	 * onOptionsItemSelected handler since clustering need MapView to be created
	 * and visible, this sample do clustering here.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1: {
			if (mBackground2 == null) {
				showLoading();
				mBackground2 = new Thread(new MyThread());
				mBackground2.start();
			}
			// new getDataTask().execute();
			break;
		}
		case 2: {
			clear();
			if (item.getTitle() == "聚合显示") {
				showMakerCluster();
				item.setTitle("分散显示");// change title
			} else {
				showMakerList();
				item.setTitle("聚合显示");
			}
			break;
		}
		case 3: {
			 Intent intent = new Intent(this, AboutUsActivity.class);
			 startActivity(intent);
			break;
		}
		case 4: {
			DBHelper db = new DBHelper(context);

			ArrayList<Marker> mlist = db.getAllLocations();
			String cnts = "";
			for (Marker o : mlist) {

				cnts += "address=" + o.address + ",latitude=" + o.latitude
						+ ",longtitude=" + o.longtitude + "\n";
			}
			Toast.makeText(context, cnts, Toast.LENGTH_LONG).show();
			break;
		}
		case 5:{
			// create clusterer instance
			float screenDensity = this.getResources().getDisplayMetrics().density;
			GeoClusterer clusterer = new GeoClusterer(mMapView,markerIconBmps_,getResources()
					.getDrawable(R.drawable.marker),screenDensity);
			// add geoitems for clustering
			for(int i=0; i<geoItems_.length; i++) {
				clusterer.addItem(geoItems_[i]);
			}
			// now redraw the cluster. it will create markers.
			clusterer.redraw();
			mMapView.invalidate();
			// now you can see items clustered on the map.
			// zoom in/out to see how icons change.
			break;
		}
		}
		return true;
	}

	// 该线程将会在单独的线程中运行
	class MyThread implements Runnable {
		private int type;

		public MyThread() {

		}

		public MyThread(int type_) {
			type = type_;
		}

		// Setup the run() method that is called when the background thread
		// is started.
		@Override
		public void run() {
			Looper.prepare();
			// Get a message object to be sent to our handler.
			Message myMsg = new Message();

			myMsg.what = GUI_THREADING;
			setonMap(type, myMsg);

			// Set the data into our handler message.
			myMsg.what = GUI_COMPLETE;
			// Send the handler message to the UI thread.
			myhandler.sendMessage(myMsg);
			// Looper.loop();
		}

	}

	private void showLoading() {
		dialog = new ProgressDialog(context);
		dialog.setTitle("通讯录地图");
		dialog.setMessage("正在加载通讯录...");
		dialog.setIndeterminate(false);
		dialog.setMax(100);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.show();

		// progressBar.setVisibility(View.VISIBLE);
	}

	private void setonMap(int type, Message msg) {
		getMarkers(type, msg);
	}

	private void getMarkers(int type, Message myMsg) {
		clear();

		markerList = new ArrayList<Marker>();
		MapContacts mc = new MapContacts(context);

		ArrayList<User> ulist = mc.getAllContacts(type);
		final int contactsCount = ulist.size();
		myhandler.post(new Runnable() {
			@Override
			public void run() {
				dialog.setMax(contactsCount);
			}
		});

		for (int i = 0; i < contactsCount; i++) {
			String phone = ulist.get(i).Phone;
			phone = phone.replaceAll("\\+86", "").replaceAll("-", "");

			String Name = ulist.get(i).Name;
			// String Address = ulist.get(i).Address;

			Marker m = mc.getLocatingFromPhone(phone, Name);
			if (m != null) {
				markerList.add(m);
				createMarker(m);
			}
			final int pval = i;
			myhandler.post(new Runnable() {
				@Override
				public void run() {
					dialog.setProgress(pval);
				}
			});
		}
	}

	private void clear() {
		markerId = 0;
		mMapView.getOverlays().clear();
		if(itemizedOverlay!=null){
			itemizedOverlay.hideAllBalloons();
		}
	}

	private void createMarker(Marker m) {
		String title = m.title;
		String content = m.content;
		double latitude = m.latitude;
		double longtitude = m.longtitude;

		createMarker(title, content, latitude, longtitude);

	}

	private void showMakerList() {
		for (int i = 0; i < markerList.size(); i++) {
			Marker m = markerList.get(i);
			createMarker(m);
		}

	}

	private void showMakerCluster() {
		screenDensity = this.getResources().getDisplayMetrics().density;

		clusterer = new GeoClusterer(mMapView, markerIconBmps_, getResources()
				.getDrawable(R.drawable.marker2), screenDensity);

		for (int i = 0; i < markerList.size(); i++) {
			Marker m = markerList.get(i);
			GeoItem geoitem = new GeoItem(markerId, (int) (m.latitude * 1E6),
					(int) (m.longtitude * 1E6), m.title, m.content);
			clusterer.addItem(geoitem);
		}
		clusterer.redraw();
		mMapView.invalidate();
	}

	private void createMarker(String title, String content, double latitude,
			double longitude) {
 
		GeoPoint point = new GeoPoint((int) (latitude * 1E6),
				(int) (longitude * 1E6));

		CustomOverlayItem overlayItem = new CustomOverlayItem(point, title,
				content, "", "");
		itemizedOverlay.addOverlay(overlayItem);

		mMapView.post(new Runnable() {
			public void run() {
				mMapView.getOverlays().add(itemizedOverlay);
				mMapView.invalidate();
			}
		});

	}

	@SuppressWarnings("unused")
	private boolean checkNetwork() {
		ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo net = conn.getActiveNetworkInfo();
		if (net != null && net.isConnected()) {
			return true;
		}
		return false;
	}
}
