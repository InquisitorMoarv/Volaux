package com.osmnavigator;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.mapsforge.map.rendertheme.ExternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlFolder;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.bonuspack.kml.KmlPoint;
import org.osmdroid.bonuspack.kml.KmlTrack;
import org.osmdroid.bonuspack.kml.LineStyle;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.bonuspack.location.FlickrPOIProvider;
import org.osmdroid.bonuspack.location.GeoNamesPOIProvider;
//import org.osmdroid.bonuspack.location.GeocoderGraphHopper;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.location.OverpassAPIProvider;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.bonuspack.location.PicasaPOIProvider;
import org.osmdroid.bonuspack.routing.GoogleRoadManager;
import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.OpenRouteServiceRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.mapsforge.MapsForgeTileProvider;
import org.osmdroid.mapsforge.MapsForgeTileSource;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.MapBoxTileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.ManifestUtil;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.NetworkLocationIgnorer;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Marker.OnMarkerDragListener;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import org.osmdroid.views.overlay.mylocation.DirectedLocationOverlay;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// import google.maps.geometry.encoding;

/**
 * Simple and general-purpose map/navigation Android application, including a KML viewer and editor.
 * It is based on osmdroid and OSMBonusPack
 * @see <a href="https://github.com/MKergall/osmbonuspack">OSMBonusPack</a>
 * @author M.Kergall
 *
 */
public class MapActivity extends Activity implements MapEventsReceiver, LocationListener, SensorEventListener, MapView.OnFirstLayoutListener {
	protected MapView map;

	protected GeoPoint startPoint, destinationPoint;
	protected ArrayList<GeoPoint> viaPoints;
	protected static int START_INDEX=-2, DEST_INDEX=-1;
	protected FolderOverlay mItineraryMarkers;
		//for departure, destination and viapoints
	protected Marker markerStart, markerDestination;
	protected ViaPointInfoWindow mViaPointInfoWindow;
	protected DirectedLocationOverlay myLocationOverlay;
	//MyLocationNewOverlay myLocationNewOverlay;
	protected LocationManager mLocationManager;
	//protected SensorManager mSensorManager;
	//protected Sensor mOrientation;

	protected boolean mTrackingMode;
	FloatingActionButton mTrackingModeButton;
	float mAzimuthAngleSpeed = 0.0f;

	protected Polygon mDestinationPolygon; //enclosing polygon of destination location

	public static Road[] mRoads;  //made static to pass between activities
	protected int mSelectedRoad;
	protected Polyline[] mRoadOverlays;
	protected FolderOverlay mRoadNodeMarkers;
	protected static final int ROUTE_REQUEST = 1;
	static final int OSRM=0, GRAPHHOPPER_FASTEST=1, GRAPHHOPPER_BICYCLE=2, GRAPHHOPPER_PEDESTRIAN=3, GOOGLE_FASTEST=4, ORS=5;
	int mWhichRouteProvider;

	public static ArrayList<POI> mPOIs; //made static to pass between activities
	RadiusMarkerClusterer mPoiMarkers;
	AutoCompleteTextView poiTagText;
	protected static final int POIS_REQUEST = 2;

	protected FolderOverlay mKmlOverlay; //root container of overlays from KML reading
	public static KmlDocument mKmlDocument; //made static to pass between activities
	public static Stack<KmlFeature> mKmlStack; //passed between activities, top is the current KmlFeature to edit. 
	public static KmlFolder mKmlClipboard; //passed between activities. Folder for multiple items selection. 

	boolean mIsRecordingTrack;

	FriendsManager mFriendsManager;

	static String SHARED_PREFS_APPKEY = "OSMNavigator";
	static String PREF_LOCATIONS_KEY = "PREF_LOCATIONS";

	OnlineTileSourceBase MAPBOXSATELLITELABELLED;
	boolean mNightMode;

	static final String userAgent = "OsmNavigator/2.4";

	static String openRouteServiceApiKey;
	static String graphHopperApiKey;
	static String flickrApiKey;
	static String geonamesAccount;
	static String mapzenApiKey;

	public int navIndex = 1;
	public boolean navInProgress = false;

	public TextToSpeech mTTS;
	public boolean mTTSSetup = false;

	private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
	public boolean notWheelchair = false;

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Configuration.getInstance().setOsmdroidBasePath(new File(Environment.getExternalStorageDirectory(), "osmdroid"));
		Configuration.getInstance().setOsmdroidTileCache(new File(Environment.getExternalStorageDirectory(), "osmdroid/tiles"));
		Configuration.getInstance().setUserAgentValue(userAgent);

		//Configuration.getInstance().setMapViewHardwareAccelerated(true);
		MapsForgeTileSource.createInstance(getApplication());

		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.main, null);
		setContentView(v);

		SharedPreferences prefs = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE);

		MAPBOXSATELLITELABELLED = new MapBoxTileSource("MapBoxSatelliteLabelled", 1, 19, 256, ".png");
		((MapBoxTileSource) MAPBOXSATELLITELABELLED).retrieveAccessToken(this);
		((MapBoxTileSource) MAPBOXSATELLITELABELLED).retrieveMapBoxMapId(this);
		TileSourceFactory.addTileSource(MAPBOXSATELLITELABELLED);

		graphHopperApiKey = ManifestUtil.retrieveKey(this, "GRAPHHOPPER_API_KEY");
		openRouteServiceApiKey = ManifestUtil.retrieveKey(this, "OPENROUTESERVICE_API_KEY");
		flickrApiKey = ManifestUtil.retrieveKey(this, "FLICKR_API_KEY");
		geonamesAccount = ManifestUtil.retrieveKey(this, "GEONAMES_ACCOUNT");
		mapzenApiKey = ManifestUtil.retrieveKey(this, "MAPZEN_APIKEY");

		map = (MapView) v.findViewById(R.id.map);

		String tileProviderName = prefs.getString("TILE_PROVIDER", "Mapnik");
		mNightMode = prefs.getBoolean("NIGHT_MODE", false);
		if ("rendertheme-v4".equals(tileProviderName)) {
			setMapsForgeTileProvider();
		} else {
			try {
				ITileSource tileSource = TileSourceFactory.getTileSource(tileProviderName);
				map.setTileSource(tileSource);
			} catch (IllegalArgumentException e) {
				map.setTileSource(TileSourceFactory.MAPNIK);
			}
		}
		if (mNightMode)
			map.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);

		map.setTilesScaledToDpi(true);
		map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);
		map.setMultiTouchControls(true);
		map.setMinZoomLevel(1.0);
		map.setMaxZoomLevel(21.0);
		map.setVerticalMapRepetitionEnabled(false);
		map.setScrollableAreaLimitLatitude(TileSystem.MaxLatitude,-TileSystem.MaxLatitude, 0/*map.getHeight()/2*/);
		//Toast.makeText(this, "H="+map.getHeight(), Toast.LENGTH_LONG).show();

		IMapController mapController = map.getController();

		//To use MapEventsReceiver methods, we add a MapEventsOverlay:
		MapEventsOverlay overlay = new MapEventsOverlay(this);
		map.getOverlays().add(overlay);

		mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

		//mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		//mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

		//map prefs:
		mapController.setZoom((double)prefs.getFloat("MAP_ZOOM_LEVEL_F", 5));
		mapController.setCenter(new GeoPoint((double) prefs.getFloat("MAP_CENTER_LAT", 48.5f),
				(double)prefs.getFloat("MAP_CENTER_LON", 2.5f)));

		myLocationOverlay = new DirectedLocationOverlay(this);
		map.getOverlays().add(myLocationOverlay);

		if (savedInstanceState == null){
			Location location = null;
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
				location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (location == null)
					location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}
			if (location != null) {
				//location known:
				onLocationChanged(location);
			} else {
				//no location known: hide myLocationOverlay
				myLocationOverlay.setEnabled(false);
			}
			startPoint = null;
			destinationPoint = null;
			viaPoints = new ArrayList<GeoPoint>();
		} else {
			myLocationOverlay.setLocation((GeoPoint)savedInstanceState.getParcelable("location"));
			//TODO: restore other aspects of myLocationOverlay...
			startPoint = savedInstanceState.getParcelable("start");
			destinationPoint = savedInstanceState.getParcelable("destination");
			viaPoints = savedInstanceState.getParcelableArrayList("viapoints");
		}

		ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(map);
		map.getOverlays().add(scaleBarOverlay);

		// Itinerary markers:
		mItineraryMarkers = new FolderOverlay();
		mItineraryMarkers.setName(getString(R.string.itinerary_markers_title));
		map.getOverlays().add(mItineraryMarkers);
		mViaPointInfoWindow = new ViaPointInfoWindow(R.layout.itinerary_bubble, map);
		updateUIWithItineraryMarkers();

		//Tracking system:
		mTrackingModeButton = findViewById(R.id.buttonTrackingMode);
		mTrackingModeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				mTrackingMode = !mTrackingMode;
				updateUIWithTrackingMode();
			}
		});
		if (savedInstanceState != null){
			mTrackingMode = savedInstanceState.getBoolean("tracking_mode");
			updateUIWithTrackingMode();
		} else
			mTrackingMode = false;

		mIsRecordingTrack = false; //TODO restore state

		AutoCompleteOnPreferences departureText = (AutoCompleteOnPreferences) findViewById(R.id.editDeparture);
		departureText.setPrefKeys(SHARED_PREFS_APPKEY, PREF_LOCATIONS_KEY);

		ImageButton searchDepButton = (ImageButton)findViewById(R.id.buttonSearchDep);
		searchDepButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				handleSearchButton(START_INDEX, R.id.editDeparture);
			}
		});

		AutoCompleteOnPreferences destinationText = (AutoCompleteOnPreferences) findViewById(R.id.editDestination);
		destinationText.setPrefKeys(SHARED_PREFS_APPKEY, PREF_LOCATIONS_KEY);

		ImageButton searchDestButton = findViewById(R.id.buttonSearchDest);
		searchDestButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				handleSearchButton(DEST_INDEX, R.id.editDestination);

			}
		});

		View expander = findViewById(R.id.expander);
		expander.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				View searchPanel = findViewById(R.id.search_panel);
				if (searchPanel.getVisibility() == View.VISIBLE){
					searchPanel.setVisibility(View.GONE);
				} else {
					searchPanel.setVisibility(View.VISIBLE);
				}
			}
		});
		View searchPanel = findViewById(R.id.search_panel);
		searchPanel.setVisibility(prefs.getInt("PANEL_VISIBILITY", View.VISIBLE));

		registerForContextMenu(searchDestButton);
		//context menu for clicking on the map is registered on this button. 
		//(a little bit strange, but if we register it on mapView, it will catch map drag events)

		//Route and Directions   //START PUNKT ECENTULL

		//TODO------------------- WIEDER AUF ORS UMSTELLEN
		mWhichRouteProvider = prefs.getInt("ROUTE_PROVIDER", GRAPHHOPPER_PEDESTRIAN);

		mRoadNodeMarkers = new FolderOverlay();
		mRoadNodeMarkers.setName("Route Steps");
		map.getOverlays().add(mRoadNodeMarkers);

		if (savedInstanceState != null){
			//STATIC mRoad = savedInstanceState.getParcelable("road");
			updateUIWithRoads(mRoads);
		}

		//POIs:
		//POI search interface:
		String[] poiTags = getResources().getStringArray(R.array.poi_tags);
		poiTagText = (AutoCompleteTextView) findViewById(R.id.poiTag);
		ArrayAdapter<String> poiAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, poiTags);
		poiTagText.setAdapter(poiAdapter);
		Button setPOITagButton = (Button) findViewById(R.id.buttonSetPOITag);
		setPOITagButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//Hide the soft keyboard:
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(poiTagText.getWindowToken(), 0);
				//Start search:
				String feature = poiTagText.getText().toString();
				if (!feature.equals(""))
					Toast.makeText(v.getContext(), "Searching:\n"+feature, Toast.LENGTH_LONG).show();
				getPOIAsync(feature);
			}
		});
		//POI markers:
		mPoiMarkers = new RadiusMarkerClusterer(this);
		Bitmap clusterIcon = BonusPackHelper.getBitmapFromVectorDrawable(this, R.drawable.marker_poi_cluster);
		mPoiMarkers.setIcon(clusterIcon);
		mPoiMarkers.mAnchorV = Marker.ANCHOR_BOTTOM;
		mPoiMarkers.mTextAnchorU = 0.70f;
		mPoiMarkers.mTextAnchorV = 0.27f;
		mPoiMarkers.getTextPaint().setTextSize(12 * getResources().getDisplayMetrics().density);
		map.getOverlays().add(mPoiMarkers);
		if (savedInstanceState != null){
			//STATIC - mPOIs = savedInstanceState.getParcelableArrayList("poi");
			updateUIWithPOI(mPOIs, "");
		}

		//KML handling:
		mKmlOverlay = null;
		if (savedInstanceState != null){
			//STATIC - mKmlDocument = savedInstanceState.getParcelable("kml");
			updateUIWithKml();
		} else { //first launch: 
			mKmlDocument = new KmlDocument();
			mKmlStack = new Stack<KmlFeature>();
			mKmlClipboard = new KmlFolder();
			//check if intent has been passed with a kml URI to load (url or file)
			Intent onCreateIntent = getIntent();
			if (onCreateIntent.getAction().equals(Intent.ACTION_VIEW)){
				String uri = onCreateIntent.getDataString();
				openFile(uri, true, false);
			}
		}

		//Sharing
		mFriendsManager = new FriendsManager(this, map);
		mFriendsManager.onCreate(savedInstanceState);

		checkPermissions();

		ImageButton menuButton = (ImageButton) findViewById(R.id.buttonMenu);
		menuButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openOptionsMenu();
			}
		});


		Button routeNavigation1 = findViewById(R.id.navRouteInfoButton1);
		routeNavigation1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openRouteInfo();
			}
		});

		Button routeNavigation2 = findViewById(R.id.navRouteInfoButton2);
		routeNavigation2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openRouteInfo();
			}
		});


		Button endNavigation = findViewById(R.id.navEndButton);
		endNavigation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				endNavigation();
			}
		});

		Button startNavigation = findViewById(R.id.navStartButton);
		startNavigation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startNavigation();
			}
		});

		ImageButton speakButton = findViewById(R.id.buttonSpeak);
		speakButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				listen();
			}
		});


		//Check if Route is present and deactivate Route button if so
		if (mRoads == null  || mRoads[mSelectedRoad].mNodes.size()<=0 ) {
			routeNavigation1.setEnabled(false);
			routeNavigation2.setEnabled(false);
			startNavigation.setEnabled(false);
			//findViewById(R.id.LL).setVisibility(View.GONE);
		}

		mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int i) {
				if (i == TextToSpeech.SUCCESS){
					int result = mTTS.setLanguage(Locale.GERMAN);

					if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
						Log.e("TextToSpeech","Language not supported");
						mTTSSetup = false;
					}else {
						mTTSSetup = true;
					}

				}else {
					Log.e("TextToSpeech","Initializsation failed");
					mTTSSetup = false;
				}
			}
		});


	}

	// Polyline API https://osmdroid.github.io/osmdroid/javadocAll/org/osmdroid/views/overlay/Polyline.html


	public  void  listen(){
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault());
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Stra??e, Hausnummer, Postleitzahl, Ort");

		try{
			startActivityForResult(intent,REQUEST_CODE_SPEECH_INPUT);
		}catch (Exception e){
			Toast.makeText(this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}




	public void startNavigation(){
		findViewById(R.id.search_panel).setVisibility(View.GONE);
		navInProgress = true;
		navIndex =1;
		findViewById(R.id.navbar_navigationNavbar).setVisibility(View.VISIBLE);
		findViewById(R.id.navbar_infoNavbar).setVisibility(View.GONE);
		Log.w("POSITION",String.valueOf(myLocationOverlay.getLocation()));
		startTurnByTurnNavigation();

	}
	public void endNavigation(){
		navInProgress = false;
		navIndex =1;
		findViewById(R.id.navbar_navigationNavbar).setVisibility(View.GONE);
		findViewById(R.id.navbar_infoNavbar).setVisibility(View.VISIBLE);
		findViewById(R.id.search_panel).setVisibility(View.VISIBLE);
	}

	public void speakNavigation(String instruction, double userEntryDistance){
		//mTTS.setPitch();
		//mTTS.setSpeechRate();
		if(mTTSSetup){

			userEntryDistance = userEntryDistance/1000;
			String result;
			if (userEntryDistance >= 100.0) {
				result = "In "+(int)userEntryDistance+" Kilometer ";
			} else if (userEntryDistance >= 1.0) {
				result = "In circa "+(int)userEntryDistance+" Kilometer ";
			} else {
				result = "In "+(int) (userEntryDistance*1000)+" Metern ";
			}
			result = result+instruction;
			mTTS.speak(result,TextToSpeech.QUEUE_FLUSH,null);
		}

	}

	@Override
	protected void onDestroy() {
		if(mTTS!=null){
			mTTS.stop();
			mTTS.shutdown();
		}
		super.onDestroy();
	}

	public void startTurnByTurnNavigation(){
		//Road tbtRoad = mRoads[mSelectedRoad];
		RoadNode entry = mRoads[mSelectedRoad].mNodes.get(navIndex);
		GeoPoint userLocation = myLocationOverlay.getLocation();
		Location loc1 = new Location("");
		loc1.setLatitude(entry.mLocation.getLatitude());
		loc1.setLongitude(entry.mLocation.getLongitude());
		Location loc2 = new Location("");
		loc2.setLatitude(userLocation.getLatitude());
		loc2.setLongitude(userLocation.getLongitude());
		float pointDistance = loc1.distanceTo(loc2);

		calcAndDisplayNodeLength(pointDistance);

		TextView nInstructions = (TextView)findViewById(R.id.navInstructions);
		String instructions = (entry.mInstructions==null ? "" : entry.mInstructions);
		nInstructions.setText(instructions);
		speakNavigation(instructions,pointDistance);


	}

	public void calcDistanceNav(){
		RoadNode entry = mRoads[mSelectedRoad].mNodes.get(navIndex);
		RoadNode nextEntry = null;
		if((navIndex+1)<mRoads[mSelectedRoad].mNodes.size()){
			nextEntry = mRoads[mSelectedRoad].mNodes.get(navIndex+1);
			}

		GeoPoint userLocation = myLocationOverlay.getLocation();

		Location loc1 = new Location("");
		loc1.setLatitude(entry.mLocation.getLatitude());
		loc1.setLongitude(entry.mLocation.getLongitude());
		Location loc2 = new Location("");
		loc2.setLatitude(userLocation.getLatitude());
		loc2.setLongitude(userLocation.getLongitude());
		float userEntryDistance = loc1.distanceTo(loc2);

		if(nextEntry!=null){
			if(userEntryDistance<=5) {
				navIndex++;
				startTurnByTurnNavigation();
			}else{
				Location loc3 = new Location("");
				loc3.setLatitude(nextEntry.mLocation.getLatitude());
				loc3.setLongitude(nextEntry.mLocation.getLongitude());
				float userNextEntryDistance = loc2.distanceTo(loc3);
				float entryToNextEntryDistance = loc1.distanceTo(loc3);
				if(userNextEntryDistance<entryToNextEntryDistance){
					navIndex++;
					startTurnByTurnNavigation();
				}else{
					calcAndDisplayNodeLength(userEntryDistance);
				}
			}
		}else {
			if(userEntryDistance<=5) {
				if(mTTSSetup){
					mTTS.speak("Sie haben ihr Ziel erreicht",TextToSpeech.QUEUE_FLUSH,null);
				}
				endNavigation();
				//ENDE
				//Noch irwie eine weiter nachhicht oder sooooooo?
				//findViewById(R.id.navbar_navigationNavbar).setVisibility(View.GONE);
				//findViewById(R.id.navbar_infoNavbar).setVisibility(View.VISIBLE);
			}else{
				calcAndDisplayNodeLength(userEntryDistance);
			}
		}



/*
		if(userEntryDistance<=5){
			if(navIndex==mRoads[mSelectedRoad].mNodes.size()){
				//ENDE
			}else {
				navIndex++;
				startTurnByTurnNavigation();
			}
		}else{
			userEntryDistance = userEntryDistance/1000;
			String result;
			if (userEntryDistance >= 100.0) {
				result = getString(org.osmdroid.bonuspack.R.string.osmbonuspack_format_distance_kilometers, (int) (userEntryDistance));
			} else if (userEntryDistance >= 1.0) {
				result = getString(org.osmdroid.bonuspack.R.string.osmbonuspack_format_distance_kilometers, Math.round(userEntryDistance * 10) / 10.0);
			} else {
				result = getString(org.osmdroid.bonuspack.R.string.osmbonuspack_format_distance_meters, (int) (userEntryDistance * 1000));
			}

			TextView nDistance = (TextView)findViewById(R.id.navUserDistance);
			nDistance.setText("In "+result);
		}
*/
	}



public void calcAndDisplayNodeLength(double userEntryDistance){
	userEntryDistance = userEntryDistance/1000;
	String result;
	if (userEntryDistance >= 100.0) {
		result = getString(org.osmdroid.bonuspack.R.string.osmbonuspack_format_distance_kilometers, (int) (userEntryDistance));
	} else if (userEntryDistance >= 1.0) {
		result = getString(org.osmdroid.bonuspack.R.string.osmbonuspack_format_distance_kilometers, Math.round(userEntryDistance * 10) / 10.0);
	} else {
		result = getString(org.osmdroid.bonuspack.R.string.osmbonuspack_format_distance_meters, (int) (userEntryDistance * 1000));
	}

	TextView nDistance = (TextView)findViewById(R.id.navUserDistance);
	nDistance.setText("In "+result);
}


public void openRouteInfo(){
	Intent myIntent;
	myIntent = new Intent(this, RouteActivity.class);
	int currentNodeId = getIndexOfBubbledMarker(mRoadNodeMarkers.getItems());
	myIntent.putExtra("SELECTED_ROAD", mSelectedRoad);
	myIntent.putExtra("NODE_ID", currentNodeId);
	startActivityForResult(myIntent, ROUTE_REQUEST);
}


	final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

	void checkPermissions() {
		List<String> permissions = new ArrayList<>();
		String message = "Application permissions:";
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
			message += "\nLocation to show user location.";
		}
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
			message += "\nStorage access to store map tiles.";
		}
		if (!permissions.isEmpty()) {
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
			String[] params = permissions.toArray(new String[permissions.size()]);
			ActivityCompat.requestPermissions(this, params, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
		} // else: We already have permissions, so handle as normal
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
				Map<String, Integer> perms = new HashMap<>();
				// Initial
				perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
				// Fill with results
				for (int i = 0; i < permissions.length; i++)
					perms.put(permissions[i], grantResults[i]);
				// Check for WRITE_EXTERNAL_STORAGE
				Boolean storage = perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
				if (!storage) {
					// Permission Denied
					Toast.makeText(this, "Storage permission is required to store map tiles to reduce data usage and for offline usage.", Toast.LENGTH_LONG).show();
				} // else: permission was granted, yay!
			}
		}
	}

	void setViewOn(BoundingBox bb){
		if (bb != null){
			map.zoomToBoundingBox(bb, true);
		}
	}

	//--- Stuff for setting the mapview on a box at startup:
	BoundingBox mInitialBoundingBox = null;

	void setInitialViewOn(BoundingBox bb) {
		if (map.getScreenRect(null).height() == 0) {
			mInitialBoundingBox = bb;
			map.addOnFirstLayoutListener(this);
		} else
			map.zoomToBoundingBox(bb, false);
	}

	@Override
	public void onFirstLayout(View v, int left, int top, int right, int bottom) {
		if (mInitialBoundingBox != null)
			map.zoomToBoundingBox(mInitialBoundingBox, false);
	}
	//---

	void savePrefs(){
		SharedPreferences prefs = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE);
		SharedPreferences.Editor ed = prefs.edit();
		ed.putFloat("MAP_ZOOM_LEVEL_F", (float)map.getZoomLevelDouble());
		GeoPoint c = (GeoPoint) map.getMapCenter();
		ed.putFloat("MAP_CENTER_LAT", (float)c.getLatitude());
		ed.putFloat("MAP_CENTER_LON", (float)c.getLongitude());
		View searchPanel = findViewById(R.id.search_panel);
		ed.putInt("PANEL_VISIBILITY", searchPanel.getVisibility());
		MapTileProviderBase tileProvider = map.getTileProvider();
		String tileProviderName = tileProvider.getTileSource().name();
		ed.putString("TILE_PROVIDER", tileProviderName);
		ed.putBoolean("NIGHT_MODE", mNightMode);
		ed.putInt("ROUTE_PROVIDER", mWhichRouteProvider);
		ed.apply();
	}

	/**
	 * callback to store activity status before a restart (orientation change for instance)
	 */
	@Override protected void onSaveInstanceState (Bundle outState){
		outState.putParcelable("location", myLocationOverlay.getLocation());
		outState.putBoolean("tracking_mode", mTrackingMode);
		outState.putParcelable("start", startPoint);
		outState.putParcelable("destination", destinationPoint);
		outState.putParcelableArrayList("viapoints", viaPoints);
		//STATIC - outState.putParcelable("road", mRoad);
		//STATIC - outState.putParcelableArrayList("poi", mPOIs);
		//STATIC - outState.putParcelable("kml", mKmlDocument);
		//STATIC - outState.putParcelable("friends", mFriends);
		mFriendsManager.onSaveInstanceState(outState);

		savePrefs();
	}






	@Override protected void onActivityResult (int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
			case FriendsManager.START_SHARING_REQUEST:
			case FriendsManager.FRIENDS_REQUEST:
				mFriendsManager.onActivityResult(requestCode, resultCode, intent);
				break;
			case REQUEST_CODE_SPEECH_INPUT: {
				if (resultCode == RESULT_OK && null != intent) {
					ArrayList<String> result = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
					AutoCompleteOnPreferences destinationText = (AutoCompleteOnPreferences) findViewById(R.id.editDestination);
					destinationText.setText(result.get(0));
					handleSearchButton(DEST_INDEX, R.id.editDestination);
				}
				break;
			}
			case ROUTE_REQUEST:
				if (resultCode == RESULT_OK) {
					int nodeId = intent.getIntExtra("NODE_ID", 0);
					map.getController().setCenter(mRoads[mSelectedRoad].mNodes.get(nodeId).mLocation);
					Marker roadMarker = (Marker) mRoadNodeMarkers.getItems().get(nodeId);
					roadMarker.showInfoWindow();
				}
				break;
			case POIS_REQUEST:
				if (resultCode == RESULT_OK) {
					int id = intent.getIntExtra("ID", 0);
					map.getController().setCenter(mPOIs.get(id).mLocation);
					Marker poiMarker = mPoiMarkers.getItem(id);
					poiMarker.showInfoWindow();
				}
				break;
			case KmlTreeActivity.KML_TREE_REQUEST:
				mKmlStack.pop();
				updateUIWithKml();
				if (intent == null)
					break;
				KmlFeature selectedFeature = intent.getParcelableExtra("KML_FEATURE");
				if (selectedFeature == null)
					break;
				BoundingBox bb = selectedFeature.getBoundingBox();
				setViewOn(bb);
				break;
			case KmlStylesActivity.KML_STYLES_REQUEST:
				updateUIWithKml();
				break;
			default:
				break;
		}
	}

	/* String getBestProvider(){
		String bestProvider = null;
		//bestProvider = locationManager.getBestProvider(new Criteria(), true); // => returns "Network Provider"! 
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			bestProvider = LocationManager.GPS_PROVIDER;
		else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			bestProvider = LocationManager.NETWORK_PROVIDER;
		return bestProvider;
	} */

	boolean startLocationUpdates(){
		boolean result = false;
		for (final String provider : mLocationManager.getProviders(true)) {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
				mLocationManager.requestLocationUpdates(provider, 2 * 1000, 0.0f, this);
				result = true;
			}
		}
		return result;
	}

	@Override protected void onResume() {
		super.onResume();
		boolean isOneProviderEnabled = startLocationUpdates();
		myLocationOverlay.setEnabled(isOneProviderEnabled);
		//TODO: not used currently
		//mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
			//sensor listener is causing a high CPU consumption... naaaa gut
		mFriendsManager.onResume();
	}

	@Override protected void onPause() {
		super.onPause();
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			mLocationManager.removeUpdates(this);
		}
		//TODO: mSensorManager.unregisterListener(this);
		mFriendsManager.onPause();
		savePrefs();
	}

    void updateUIWithTrackingMode(){
		if (mTrackingMode){
			mTrackingModeButton.setImageResource(R.drawable.ic_gps_fixed);

			//mTrackingModeButton.setBackgroundResource(R.drawable.btn_tracking_on);
			if (myLocationOverlay.isEnabled()&& myLocationOverlay.getLocation() != null){
				map.getController().animateTo(myLocationOverlay.getLocation());
			}
			map.setMapOrientation(-mAzimuthAngleSpeed);
			mTrackingModeButton.setKeepScreenOn(true);
		} else {
			mTrackingModeButton.setImageResource(R.drawable.ic_gps_not_fixed);
			//mTrackingModeButton.setBackgroundResource(R.drawable.btn_tracking_off);
			map.setMapOrientation(0.0f);
			mTrackingModeButton.setKeepScreenOn(false);
		}
    }

    //------------- Geocoding and Reverse Geocoding

	/**
	 * Reverse Geocoding
     */
    public String getAddress(GeoPoint p){
		GeocoderNominatim geocoder = new GeocoderNominatim(userAgent);
		//GeocoderGraphHopper geocoder = new GeocoderGraphHopper(Locale.getDefault(), graphHopperApiKey);
		String theAddress;
		try {
			double dLatitude = p.getLatitude();
			double dLongitude = p.getLongitude();
			List<Address> addresses = geocoder.getFromLocation(dLatitude, dLongitude, 1);
			StringBuilder sb = new StringBuilder();
			if (addresses.size() > 0) {
				Address address = addresses.get(0);
				int n = address.getMaxAddressLineIndex();
				for (int i=0; i<=n; i++) {
					if (i!=0)
						sb.append(", ");
					sb.append(address.getAddressLine(i));
				}
				theAddress = sb.toString();
			} else {
				theAddress = null;
			}
		} catch (IOException e) {
			theAddress = null;
		}
		if (theAddress != null) {
			return theAddress;
		} else {
			return "";
		}
    }

	private class GeocodingTask extends AsyncTask<Object, Void, List<Address>> {
		int mIndex;
		protected List<Address> doInBackground(Object... params) {
			String locationAddress = (String)params[0];
			mIndex = (Integer)params[1];
			GeocoderNominatim geocoder = new GeocoderNominatim(userAgent);
			geocoder.setOptions(true); //ask for enclosing polygon (if any)
			//GeocoderGraphHopper geocoder = new GeocoderGraphHopper(Locale.getDefault(), graphHopperApiKey);
			try {
				BoundingBox viewbox = map.getBoundingBox();
				List<Address> foundAdresses = geocoder.getFromLocationName(locationAddress, 1,
						viewbox.getLatSouth(), viewbox.getLonEast(),
						viewbox.getLatNorth(), viewbox.getLonWest(), false);
				return foundAdresses;
			} catch (Exception e) {
				return null;
			}
		}
		protected void onPostExecute(List<Address> foundAdresses) {
			if (foundAdresses == null) {
				Toast.makeText(getApplicationContext(), "Geocoding error", Toast.LENGTH_SHORT).show();
			} else if (foundAdresses.size() == 0) { //if no address found, display an error
				Toast.makeText(getApplicationContext(), "Address not found.", Toast.LENGTH_SHORT).show();
			} else {
				Address address = foundAdresses.get(0); //get first address
				String addressDisplayName = address.getExtras().getString("display_name");
				if (mIndex == START_INDEX){
					startPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
					markerStart = updateItineraryMarker(markerStart, startPoint, START_INDEX,
							R.string.departure, R.drawable.marker_departure, -1, addressDisplayName);
					map.getController().setCenter(startPoint);
				} else if (mIndex == DEST_INDEX){
					destinationPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
					markerDestination = updateItineraryMarker(markerDestination, destinationPoint, DEST_INDEX,
							R.string.destination, R.drawable.marker_destination, -1, addressDisplayName);
					map.getController().setCenter(destinationPoint);
				}
				getRoadAsync();
				//get and display enclosing polygon:
				Bundle extras = address.getExtras();
				if (extras != null && extras.containsKey("polygonpoints")){
					ArrayList<GeoPoint> polygon = extras.getParcelableArrayList("polygonpoints");
					//Log.d("DEBUG", "polygon:"+polygon.size());
					updateUIWithPolygon(polygon, addressDisplayName);
				} else {
					updateUIWithPolygon(null, "");
				}
			}
		}
	}

	/**
     * Geocoding of the departure or destination address
     */
	public void handleSearchButton(int index, int editResId){
		EditText locationEdit = (EditText)findViewById(editResId);
		//Hide the soft keyboard:
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(locationEdit.getWindowToken(), 0);

		String locationAddress = locationEdit.getText().toString();

		if (locationAddress.equals("")){
			removePoint(index);
			map.invalidate();
			return;
		}

		Toast.makeText(this, "Searching:\n"+locationAddress, Toast.LENGTH_LONG).show();
		AutoCompleteOnPreferences.storePreference(this, locationAddress, SHARED_PREFS_APPKEY, PREF_LOCATIONS_KEY);
		new GeocodingTask().execute(locationAddress, index);
	}

	//add or replace the polygon overlay
	public void updateUIWithPolygon(ArrayList<GeoPoint> polygon, String name){
		List<Overlay> mapOverlays = map.getOverlays();
		int location = -1;
		if (mDestinationPolygon != null)
			location = mapOverlays.indexOf(mDestinationPolygon);
		mDestinationPolygon = new Polygon();
		mDestinationPolygon.setFillColor(0x15FF0080);
		mDestinationPolygon.setStrokeColor(0x800000FF);
		mDestinationPolygon.setStrokeWidth(5.0f);
		mDestinationPolygon.setTitle(name);
		BoundingBox bb = null;
		if (polygon != null){
			mDestinationPolygon.setPoints(polygon);
			bb = BoundingBox.fromGeoPoints(polygon);
		}
		if (location != -1)
			mapOverlays.set(location, mDestinationPolygon);
		else
			mapOverlays.add(1, mDestinationPolygon); //insert just above the MapEventsOverlay. 
		setViewOn(bb);
		map.invalidate();
	}

	//Async task to reverse-geocode the marker position in a separate thread:
	private class ReverseGeocodingTask extends AsyncTask<Marker, Void, String> {
		Marker marker;
		protected String doInBackground(Marker... params) {
			marker = params[0];
			return getAddress(marker.getPosition());
		}
		protected void onPostExecute(String result) {
			marker.setSnippet(result);
			marker.showInfoWindow();
		}
	}

	//------------ Itinerary markers

	class OnItineraryMarkerDragListener implements OnMarkerDragListener {
		@Override public void onMarkerDrag(Marker marker) {}
		@Override public void onMarkerDragEnd(Marker marker) {
			int index = (Integer)marker.getRelatedObject();
			if (index == START_INDEX)
				startPoint = marker.getPosition();
			else if (index == DEST_INDEX)
				destinationPoint = marker.getPosition();
			else
				viaPoints.set(index, marker.getPosition());
			//update location:
			new ReverseGeocodingTask().execute(marker);
			//update route:
			getRoadAsync();
		}

		@Override
		public void onMarkerDragStart(Marker marker) {
		}
	}

	final OnItineraryMarkerDragListener mItineraryListener = new OnItineraryMarkerDragListener();

	/** Update (or create if null) a marker in itineraryMarkers. */
    public Marker updateItineraryMarker(Marker marker, GeoPoint p, int index,
    		int titleResId, int markerResId, int imageResId, String address) {
		if (marker == null){
			marker = new Marker(map);
			marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
			marker.setInfoWindow(mViaPointInfoWindow);
			marker.setDraggable(true);
			marker.setOnMarkerDragListener(mItineraryListener);
			mItineraryMarkers.add(marker);
		}
		String title = getResources().getString(titleResId);
		marker.setTitle(title);
		marker.setPosition(p);
		Drawable icon = ResourcesCompat.getDrawable(getResources(), markerResId, null);
		marker.setIcon(icon);
		marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
		if (imageResId != -1)
			marker.setImage(ResourcesCompat.getDrawable(getResources(), imageResId, null));
		marker.setRelatedObject(index);
		map.invalidate();
		if (address != null)
			marker.setSnippet(address);
		else
			//Start geocoding task to get the address and update the Marker description:
			new ReverseGeocodingTask().execute(marker);
		return marker;
	}

	public void addViaPoint(GeoPoint p){
		viaPoints.add(p);
		updateItineraryMarker(null, p, viaPoints.size() - 1,
				R.string.viapoint, R.drawable.marker_via, -1, null);
	}

	public void removePoint(int index){
		if (index == START_INDEX){
			startPoint = null;
			if (markerStart != null){
				markerStart.closeInfoWindow();
				mItineraryMarkers.remove(markerStart);
				markerStart = null;
			}
		} else if (index == DEST_INDEX){
			destinationPoint = null;
			if (markerDestination != null){
				markerDestination.closeInfoWindow();
				mItineraryMarkers.remove(markerDestination);
				markerDestination = null;
			}
		} else {
			viaPoints.remove(index);
			updateUIWithItineraryMarkers();
		}
		getRoadAsync();
	}

	public void updateUIWithItineraryMarkers(){
		mItineraryMarkers.closeAllInfoWindows();
		mItineraryMarkers.getItems().clear();
		//Start marker:
		if (startPoint != null){
			markerStart = updateItineraryMarker(null, startPoint, START_INDEX,
				R.string.departure, R.drawable.marker_departure, -1, null);
		}
		//Via-points markers if any:
		for (int index=0; index<viaPoints.size(); index++){
			updateItineraryMarker(null, viaPoints.get(index), index,
				R.string.viapoint, R.drawable.marker_via, -1, null);
		}
		//Destination marker if any:
		if (destinationPoint != null){
			markerDestination = updateItineraryMarker(null, destinationPoint, DEST_INDEX,
				R.string.destination, R.drawable.marker_destination, -1, null);
		}
	}

	//------------ Route and Directions

	private void putRoadNodes(Road road){
		mRoadNodeMarkers.getItems().clear();
		Drawable icon = ResourcesCompat.getDrawable(getResources(), R.drawable.marker_node, null);
		int n = road.mNodes.size();
		MarkerInfoWindow infoWindow = new MarkerInfoWindow(org.osmdroid.bonuspack.R.layout.bonuspack_bubble, map);
		TypedArray iconIds = getResources().obtainTypedArray(R.array.direction_icons);
    	for (int i=0; i<n; i++){
    		RoadNode node = road.mNodes.get(i);
    		String instructions = (node.mInstructions==null ? "" : node.mInstructions);
    		Marker nodeMarker = new Marker(map);
    		nodeMarker.setTitle(getString(R.string.step)+ " " + (i+1));
    		nodeMarker.setSnippet(instructions);
			nodeMarker.setSubDescription(Road.getLengthDurationText(this, node.mLength, node.mDuration));
			nodeMarker.setPosition(node.mLocation);
    		nodeMarker.setIcon(icon);
			nodeMarker.setInfoWindow(infoWindow); //use a shared infowindow.
			int iconId = iconIds.getResourceId(node.mManeuverType, R.drawable.ic_empty);
    		if (iconId != R.drawable.ic_empty){
				Drawable image = ResourcesCompat.getDrawable(getResources(), iconId, null);
				nodeMarker.setImage(image);
    		}
			nodeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
    		mRoadNodeMarkers.add(nodeMarker);
    	}
    	iconIds.recycle();
	}





	void selectRoad(int roadIndex){
		mSelectedRoad = roadIndex;
		putRoadNodes(mRoads[roadIndex]);
		//Set route info in the text view:
		String navDistance = mRoads[roadIndex].getmLength(this);
		String navDuration = mRoads[roadIndex].getmDuration(this);

		TextView NavDistance = findViewById(R.id.navDistance);
		TextView NavDuration = findViewById(R.id.navDuration);
		NavDistance.setText("Distanz: "+navDistance);
		NavDuration.setText("Dauer: "+navDuration);


		TextView textView =(TextView)findViewById (R.id.routeInfo);
		textView.setText(mRoads[roadIndex].getLengthDurationText(this, -1));
		for (int i=0; i<mRoadOverlays.length; i++){
			Paint p = mRoadOverlays[i].getPaint();
			if (i == roadIndex){
				//p.setColor(0x800000FF); //blue
				p.setColor(Color.RED);
				p.setStrokeWidth(20);}
			else
				p.setColor(0x90666666); //grey
		}
		map.invalidate();
	}

	class RoadOnClickListener implements Polyline.OnClickListener{
		@Override public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos){
			int selectedRoad = (Integer)polyline.getRelatedObject();
			selectRoad(selectedRoad);
			polyline.setInfoWindowLocation(eventPos);
			polyline.showInfoWindow();
			return true;

		}
	}


	//Wenn neue Roads rein kommen.
	void updateUIWithRoads(Road[] roads){

    	endNavigation();

    	if(notWheelchair){
			findViewById(R.id.navWarning).setVisibility(View.VISIBLE);
		}else {
			findViewById(R.id.navWarning).setVisibility(View.GONE);
		}

		mRoadNodeMarkers.getItems().clear();
		TextView textView = (TextView)findViewById(R.id.routeInfo);
		textView.setText("");
		List<Overlay> mapOverlays = map.getOverlays();
		if (mRoadOverlays != null){
			for (int i=0; i<mRoadOverlays.length; i++)
				mapOverlays.remove(mRoadOverlays[i]);
			mRoadOverlays = null;
		}
		if (roads == null)
			return;
		if (roads[0].mStatus == Road.STATUS_TECHNICAL_ISSUE) {
			Toast.makeText(map.getContext(), "Technical issue when getting the route", Toast.LENGTH_SHORT).show();
		}else if (roads[0].mStatus > Road.STATUS_TECHNICAL_ISSUE) //functional issues
			Toast.makeText(map.getContext(), "No possible route here", Toast.LENGTH_SHORT).show();
		mRoadOverlays = new Polyline[roads.length];
		for (int i=0; i<roads.length; i++) {
			Polyline roadPolyline = RoadManager.buildRoadOverlay(roads[i]);
			mRoadOverlays[i] = roadPolyline;
			/*
			if (mWhichRouteProvider == GRAPHHOPPER_BICYCLE || mWhichRouteProvider == GRAPHHOPPER_PEDESTRIAN) {
				Paint p = roadPolyline.getPaint();
				p.setPathEffect(new DashPathEffect(new float[]{10, 5}, 0));
			}*/
			String routeDesc = roads[i].getLengthDurationText(this, -1);
			roadPolyline.setTitle(getString(R.string.route) + " - " + routeDesc);
			roadPolyline.setInfoWindow(new BasicInfoWindow(org.osmdroid.bonuspack.R.layout.bonuspack_bubble, map));
			roadPolyline.setRelatedObject(i);
			roadPolyline.setOnClickListener(new RoadOnClickListener());
			mapOverlays.add(1, roadPolyline);
			//we insert the road overlays at the "bottom", just above the MapEventsOverlay,
			//to avoid covering the other overlays. 
		}
		selectRoad(0);
		if (mRoads != null && mRoads[mSelectedRoad].mNodes.size()>0) {
			findViewById(R.id.navRouteInfoButton1).setEnabled(true);
			findViewById(R.id.navRouteInfoButton2).setEnabled(true);
			findViewById(R.id.navStartButton).setEnabled(true);
		}else{
			//findViewById(R.id.LL).setVisibility(View.VISIBLE);

			/*LinearLayout layout = (LinearLayout)findViewById(R.id.LL);

			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) layout.getLayoutParams();
			lp.height = 0;*/
			//findViewById(R.id.LL).setVisibility(View.GONE);
			findViewById(R.id.navRouteInfoButton1).setEnabled(false);
			findViewById(R.id.navRouteInfoButton2).setEnabled(false);
			findViewById(R.id.navStartButton).setEnabled(false);
		}
    }

	/**
	 * Async task to get the road in a separate thread.
	 */
	private class UpdateRoadTask extends AsyncTask<ArrayList<GeoPoint>, Void, Road[]> {

		private final Context mContext;

		public UpdateRoadTask(Context context) {
			this.mContext = context;
		}

		protected Road[] doInBackground(ArrayList<GeoPoint>... params) {
			ArrayList<GeoPoint> waypoints = params[0];
			RoadManager roadManager;
			Locale locale = Locale.getDefault();
			switch (mWhichRouteProvider){
			case ORS:
					roadManager = new OpenRouteServiceRoadManager(openRouteServiceApiKey, false);
				break;
			case OSRM:
				roadManager = new OSRMRoadManager(mContext, userAgent);
				break;
			case GRAPHHOPPER_FASTEST:
				roadManager = new GraphHopperRoadManager(graphHopperApiKey, false);
				roadManager.addRequestOption("locale="+locale.getLanguage());
				break;
			case GRAPHHOPPER_BICYCLE:
				roadManager = new GraphHopperRoadManager(graphHopperApiKey, false);
				roadManager.addRequestOption("locale="+locale.getLanguage());
				roadManager.addRequestOption("vehicle=bike");
				//((GraphHopperRoadManager)roadManager).setElevation(true);
				break;
			case GRAPHHOPPER_PEDESTRIAN:
				roadManager = new GraphHopperRoadManager(graphHopperApiKey, false);
				roadManager.addRequestOption("locale="+locale.getLanguage());
				roadManager.addRequestOption("vehicle=foot");
				//((GraphHopperRoadManager)roadManager).setElevation(true);
				break;
			case GOOGLE_FASTEST:
				roadManager = new GoogleRoadManager();
				break;
				default:
				return null;
			}

			Road[] roads = roadManager.getRoads(waypoints);
			if(roads[0].mStatus == Road.STATUS_TECHNICAL_ISSUE&& mWhichRouteProvider == ORS){
				roadManager = new GraphHopperRoadManager(graphHopperApiKey, false);
				roadManager.addRequestOption("locale="+locale.getLanguage());
				roadManager.addRequestOption("vehicle=foot");
				notWheelchair = true;
			}else {
				notWheelchair = false;
			}
			return roadManager.getRoads(waypoints);
		}

		protected void onPostExecute(Road[] result) {
			mRoads = result;
			updateUIWithRoads(result);
			getPOIAsync(poiTagText.getText().toString());
		}
	}

	public void getRoadAsync(){
		mRoads = null;
		GeoPoint roadStartPoint = null;
		if (startPoint != null){
			roadStartPoint = startPoint;
		} else if (myLocationOverlay.isEnabled() && myLocationOverlay.getLocation() != null){
			//use my current location as itinerary start point:
			roadStartPoint = myLocationOverlay.getLocation();
		}
		if (roadStartPoint == null || destinationPoint == null){
			updateUIWithRoads(mRoads);
			return;
		}
		ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>(2);
		waypoints.add(roadStartPoint);
		//add intermediate via points:
		for (GeoPoint p:viaPoints){
			waypoints.add(p);
		}
		waypoints.add(destinationPoint);
		new UpdateRoadTask(this).execute(waypoints);
	}

	//----------------- POIs

	void updateUIWithPOI(ArrayList<POI> pois, String featureTag){
		if (pois != null){
			POIInfoWindow poiInfoWindow = new POIInfoWindow(map);
			for (POI poi:pois){
				Marker poiMarker = new Marker(map);
				poiMarker.setTitle(poi.mType);
				poiMarker.setSnippet(poi.mDescription);
				poiMarker.setPosition(poi.mLocation);
				Drawable icon = null;
				if (poi.mServiceId == POI.POI_SERVICE_NOMINATIM || poi.mServiceId == POI.POI_SERVICE_OVERPASS_API){
					icon = ResourcesCompat.getDrawable(getResources(), R.drawable.marker_poi, null);
					poiMarker.setAnchor(Marker.ANCHOR_CENTER, 1.0f);
				} else if (poi.mServiceId == POI.POI_SERVICE_GEONAMES_WIKIPEDIA){
					if (poi.mRank < 90)
						icon = ResourcesCompat.getDrawable(getResources(), R.drawable.marker_poi_wikipedia_16, null);
					else
						icon = ResourcesCompat.getDrawable(getResources(), R.drawable.marker_poi_wikipedia_32, null);
				} else if (poi.mServiceId == POI.POI_SERVICE_FLICKR){
					icon = ResourcesCompat.getDrawable(getResources(), R.drawable.marker_poi_flickr, null);
				} else if (poi.mServiceId == POI.POI_SERVICE_PICASA){
					icon = ResourcesCompat.getDrawable(getResources(), R.drawable.marker_poi_picasa_24, null);
					poiMarker.setSubDescription(poi.mCategory);
				}
				poiMarker.setIcon(icon);
				poiMarker.setRelatedObject(poi);
				poiMarker.setInfoWindow(poiInfoWindow);
				//thumbnail loading moved in async task for better performances. 
				mPoiMarkers.add(poiMarker);
			}
		}
		mPoiMarkers.setName(featureTag);
		mPoiMarkers.invalidate();
		map.invalidate();
	}

	void setMarkerIconAsPhoto(Marker marker, Bitmap thumbnail){
		int borderSize = 2;
		thumbnail = Bitmap.createScaledBitmap(thumbnail, 48, 48, true);
	    Bitmap withBorder = Bitmap.createBitmap(thumbnail.getWidth() + borderSize * 2, thumbnail.getHeight() + borderSize * 2, thumbnail.getConfig());
	    Canvas canvas = new Canvas(withBorder);
	    canvas.drawColor(Color.WHITE);
	    canvas.drawBitmap(thumbnail, borderSize, borderSize, null);
		BitmapDrawable icon = new BitmapDrawable(getResources(), withBorder);
		marker.setIcon(icon);
	}

	ExecutorService mThreadPool = Executors.newFixedThreadPool(3);

	class ThumbnailLoaderTask implements Runnable {
		POI mPoi; Marker mMarker;
		ThumbnailLoaderTask(POI poi, Marker marker){
			mPoi = poi; mMarker = marker;
		}
		@Override public void run(){
			Bitmap thumbnail = mPoi.getThumbnail();
			if (thumbnail != null){
				setMarkerIconAsPhoto(mMarker, thumbnail);
			}
		}
	}

	/** Loads all thumbnails in background */
	void startAsyncThumbnailsLoading(ArrayList<POI> pois){
		if (pois == null)
			return;
		//Try to stop existing threads:
		mThreadPool.shutdownNow();
		mThreadPool = Executors.newFixedThreadPool(3);
		for (int i=0; i<pois.size(); i++){
			final POI poi = pois.get(i);
			final Marker marker = mPoiMarkers.getItem(i);
			mThreadPool.submit(new ThumbnailLoaderTask(poi, marker));
		}
	}

	/**
	 * Convert human readable feature to an OSM tag.
	 * @param humanReadableFeature
	 * @return OSM tag string: "k=v"
	 */
	String getOSMTag(String humanReadableFeature){
		HashMap<String,String> map = BonusPackHelper.parseStringMapResource(getApplicationContext(), R.array.osm_poi_tags);
		return map.get(humanReadableFeature.toLowerCase(Locale.getDefault()));
	}

	private class POILoadingTask extends AsyncTask<String, Void, ArrayList<POI>> {
		String mFeatureTag;
		String message;
		protected ArrayList<POI> doInBackground(String... params) {
			mFeatureTag = params[0];
			BoundingBox bb = map.getBoundingBox();
			if (mFeatureTag == null || mFeatureTag.equals("")){
				return null;
			} else if (mFeatureTag.equals("wikipedia")){
				GeoNamesPOIProvider poiProvider = new GeoNamesPOIProvider(geonamesAccount);
				//Get POI inside the bounding box of the current map view:
				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 30);
				return pois;
			} else if (mFeatureTag.equals("flickr")){
				FlickrPOIProvider poiProvider = new FlickrPOIProvider(flickrApiKey);
				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 30);
				return pois;
			} else if (mFeatureTag.startsWith("picasa")){
				PicasaPOIProvider poiProvider = new PicasaPOIProvider(null);
				//allow to search for keywords among picasa photos:
				String q = mFeatureTag.substring("picasa".length());
				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 50, q);
				return pois;
			} else {
				/*
				NominatimPOIProvider poiProvider = new NominatimPOIProvider();
				ArrayList<POI> pois;
				if (mRoad == null){
					pois = poiProvider.getPOIInside(map.getBoundingBox(), mFeatureTag, 100);
				} else {
					pois = poiProvider.getPOIAlong(mRoad.getRouteLow(), mFeatureTag, 100, 2.0);
				}
				*/
				OverpassAPIProvider overpassProvider = new OverpassAPIProvider();
				String osmTag = getOSMTag(mFeatureTag);
				if (osmTag == null){
					message = mFeatureTag + " is not a valid feature.";
					return null;
				}
				String oUrl = overpassProvider.urlForPOISearch(osmTag, bb, 100, 10);
				ArrayList<POI> pois = overpassProvider.getPOIsFromUrl(oUrl);
				return pois;
			}
		}
		protected void onPostExecute(ArrayList<POI> pois) {
			mPOIs = pois;
			if (mFeatureTag == null || mFeatureTag.equals("")){
				//no search, no message
			} else if (mPOIs == null){
				if (message != null)
					Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
				else
					Toast.makeText(getApplicationContext(), "Technical issue when getting "+mFeatureTag+ " POI.", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), mFeatureTag+ " found:"+mPOIs.size(), Toast.LENGTH_LONG).show();
			}
			updateUIWithPOI(mPOIs, mFeatureTag);
			if (mFeatureTag.equals("flickr")||mFeatureTag.startsWith("picasa")||mFeatureTag.equals("wikipedia"))
				startAsyncThumbnailsLoading(mPOIs);
		}
	}

	void getPOIAsync(String tag){
		mPoiMarkers.getItems().clear();
		new POILoadingTask().execute(tag);
	}

	//------------ KML handling

	boolean mDialogForOpen;

	void openLocalFileDialog(boolean open){
		mDialogForOpen = open;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.file_kml_open));
		builder.setMessage("" + mKmlDocument.getDefaultPathForAndroid(""));
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		String localFileName = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE).getString("KML_LOCAL_FILE", "current.kml");
		input.setText(localFileName);
		builder.setView(input);
		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				String localFileName = input.getText().toString();
				SharedPreferences prefs = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE);
				prefs.edit().putString("KML_LOCAL_FILE", localFileName).apply();
				dialog.cancel();
				if (mDialogForOpen){
					File file = mKmlDocument.getDefaultPathForAndroid(localFileName);
					openFile("file:/"+file.toString(), false, false);
				} else
					saveFile(localFileName);
			}
		});
		builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.show();
	}

	void openUrlDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.url_kml_open));
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		String defaultUri = "https://raw.githubusercontent.com/googlemaps/kml-samples/gh-pages/a/h.kml";
		String uri = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE).getString("KML_URI", defaultUri);
		input.setText(uri);
		builder.setView(input);
		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				String uri = input.getText().toString();
				SharedPreferences prefs = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE);
				prefs.edit().putString("KML_URI", uri).apply();
				dialog.cancel();
				openFile(uri, false, false);
			}
		});
		builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.show();
	}

	void openOverpassAPIWizard(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Overpass API Wizard");
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		String query = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE).getString("OVERPASS_QUERY", "amenity=cinema");
		input.setText(query);
		builder.setView(input);
		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String query = input.getText().toString();
				SharedPreferences prefs = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE);
				prefs.edit().putString("OVERPASS_QUERY", query).apply();
				dialog.cancel();
				openFile(query, false, true);
			}
		});
		builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.show();
	}

	boolean getKMLFromOverpass(String query){
		OverpassAPIProvider overpassProvider = new OverpassAPIProvider();
		String oUrl = overpassProvider.urlForTagSearchKml(query, map.getBoundingBox(), 500, 30);
		return overpassProvider.addInKmlFolder(mKmlDocument.mKmlRoot, oUrl);
	}

	ProgressDialog createSpinningDialog(String title){
		ProgressDialog pd = new ProgressDialog(map.getContext());
		pd.setTitle(title);
		pd.setMessage(getString(R.string.wait));
		pd.setCancelable(false);
		pd.setIndeterminate(true);
		return pd;
	}

	class KmlLoadingTask extends AsyncTask<Object, Void, Boolean>{
		String mUri;
		boolean mOnCreate;
		ProgressDialog mPD;
		String mMessage;
		KmlLoadingTask(String message){
			super();
			mMessage = message;
		}
		@Override protected void onPreExecute() {
			mPD = createSpinningDialog(mMessage);
			mPD.show();
		}
		@Override protected Boolean doInBackground(Object... params) {
			mUri = (String)params[0];
			mOnCreate = (Boolean)params[1];
			boolean isOverpassRequest = (Boolean)params[2];
			mKmlDocument = new KmlDocument();
			boolean ok = false;
			if (isOverpassRequest){
				//mUri contains the query
				ok = getKMLFromOverpass(mUri);
			} else if (mUri.startsWith("file:/")){
				mUri = mUri.substring("file:/".length());
				File file = new File(mUri);
				if (mUri.endsWith(".json"))
					ok = mKmlDocument.parseGeoJSON(file);
				else if (mUri.endsWith(".kmz"))
					ok = mKmlDocument.parseKMZFile(file);
				else //assume KML
					ok = mKmlDocument.parseKMLFile(file);
			} else if (mUri.startsWith("http")) {
				ok = mKmlDocument.parseKMLUrl(mUri);
			}
			return ok;
		}
		@Override protected void onPostExecute(Boolean ok) {
			if (mPD != null)
				mPD.dismiss();
			if (!ok)
				Toast.makeText(getApplicationContext(), "Sorry, unable to read "+mUri, Toast.LENGTH_SHORT).show();
			updateUIWithKml();
			if (ok){
				BoundingBox bb = mKmlDocument.mKmlRoot.getBoundingBox();
				if (bb != null){
					if (!mOnCreate)
						setViewOn(bb);
					else  //KO in onCreate (osmdroid bug) - Workaround:
						setInitialViewOn(bb);
				}
			}
		}
	}

	void openFile(String uri, boolean onCreate, boolean isOverpassRequest){
		//Toast.makeText(this, "Loading "+uri, Toast.LENGTH_SHORT).show();
		new KmlLoadingTask(getString(R.string.loading)+" "+uri).execute(uri, onCreate, isOverpassRequest);
	}

	/** save fileName locally, as KML or GeoJSON depending on the extension */
	void saveFile(String fileName){
		boolean result;
		File file = mKmlDocument.getDefaultPathForAndroid(fileName);
		if (fileName.endsWith(".json"))
			result = mKmlDocument.saveAsGeoJSON(file);
		else
			result = mKmlDocument.saveAsKML(file);
		if (result)
			Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(this, "Unable to save "+fileName, Toast.LENGTH_SHORT).show();
	}

	Style buildDefaultStyle(){
		Drawable defaultKmlMarker = ResourcesCompat.getDrawable(getResources(), R.drawable.marker_kml_point, null);
		Bitmap bitmap = ((BitmapDrawable)defaultKmlMarker).getBitmap();
		return new Style(bitmap, 0x901010AA, 3.0f, 0x20AA1010);

	}

	void updateUIWithKml(){
		if (mKmlOverlay != null){
			mKmlOverlay.closeAllInfoWindows();
			map.getOverlays().remove(mKmlOverlay);
		}
		mKmlOverlay = (FolderOverlay)mKmlDocument.mKmlRoot.buildOverlay(map, buildDefaultStyle(), null, mKmlDocument);
		map.getOverlays().add(mKmlOverlay);
		map.invalidate();
	}

	void insertOverlaysInKml(){
		KmlFolder root = mKmlDocument.mKmlRoot;
		//Insert relevant overlays inside:
		if (mItineraryMarkers.getItems().size()>0)
			root.addOverlay(mItineraryMarkers, mKmlDocument);
		if (mRoadOverlays != null){
			for (int i=0; i<mRoadOverlays.length; i++)
				root.addOverlay(mRoadOverlays[i], mKmlDocument);
		}
		if (mRoadNodeMarkers.getItems().size()>0)
			root.addOverlay(mRoadNodeMarkers, mKmlDocument);
		root.addOverlay(mDestinationPolygon, mKmlDocument);
		if (mPoiMarkers.getItems().size()>0){
			root.addOverlay(mPoiMarkers, mKmlDocument);
		}
	}

	//Async task to reverse-geocode the KML point in a separate thread:
	private class KMLGeocodingTask extends AsyncTask<KmlPlacemark, Void, String> {
		KmlPlacemark kmlPoint;
		protected String doInBackground(KmlPlacemark... params) {
			kmlPoint = params[0];
			return getAddress(((KmlPoint) kmlPoint.mGeometry).getPosition());
		}
		protected void onPostExecute(String result) {
			kmlPoint.mName = result;
			updateUIWithKml();
			// marker.showInfoWindow();
		}
	}

	void addKmlPoint(GeoPoint position){
		KmlFeature kmlPoint = new KmlPlacemark(position);
		mKmlDocument.mKmlRoot.add(kmlPoint);
		new KMLGeocodingTask().execute((KmlPlacemark)kmlPoint);
		updateUIWithKml();
	}

	//------------ MapEventsReceiver implementation

	GeoPoint mClickedGeoPoint; //any other way to pass the position to the menu ???

	@Override public boolean longPressHelper(GeoPoint p) {
		mClickedGeoPoint = p;
		ImageButton searchButton = (ImageButton)findViewById(R.id.buttonSearchDest);
		openContextMenu(searchButton);
		//menu is hooked on the "Search Destination" button, as it must be hooked somewhere.
		return true;
	}

	@Override public boolean singleTapConfirmedHelper(GeoPoint p) {
		InfoWindow.closeAllInfoWindowsOn(map);
		return true;
	}

	//----------- Context Menu when clicking on the map
	@Override public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_menu, menu);
	}

	@Override public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_departure:
			startPoint = new GeoPoint(mClickedGeoPoint);
			markerStart = updateItineraryMarker(markerStart, startPoint, START_INDEX,
				R.string.departure, R.drawable.marker_departure, -1, null);
			getRoadAsync();
			return true;
		case R.id.menu_destination:
			destinationPoint = new GeoPoint(mClickedGeoPoint);
			markerDestination = updateItineraryMarker(markerDestination, destinationPoint, DEST_INDEX,
				R.string.destination, R.drawable.marker_destination, -1, null);
			getRoadAsync();
			return true;
		case R.id.menu_viapoint:
			GeoPoint viaPoint = new GeoPoint(mClickedGeoPoint);
			addViaPoint(viaPoint);
			getRoadAsync();
			return true;
		case R.id.menu_kmlpoint:
			GeoPoint kmlPoint = new GeoPoint(mClickedGeoPoint);
			addKmlPoint(kmlPoint);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	//------------ Option Menu implementation

	@Override public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);

		switch (mWhichRouteProvider){
		case OSRM:
			menu.findItem(R.id.menu_route_osrm).setChecked(true);
			break;
		case ORS:
			menu.findItem(R.id.menu_route_ors).setChecked(true);
			break;
		case GRAPHHOPPER_FASTEST:
			menu.findItem(R.id.menu_route_graphhopper_fastest).setChecked(true);
			break;
		case GRAPHHOPPER_BICYCLE:
			menu.findItem(R.id.menu_route_graphhopper_bicycle).setChecked(true);
			break;
		case GRAPHHOPPER_PEDESTRIAN:
			menu.findItem(R.id.menu_route_graphhopper_pedestrian).setChecked(true);
			break;
		case GOOGLE_FASTEST:
			menu.findItem(R.id.menu_route_google).setChecked(true);
			break;
		}

		if (map.getTileProvider().getTileSource() == TileSourceFactory.MAPNIK) {
			if (!mNightMode)
				menu.findItem(R.id.menu_tile_mapnik).setChecked(true);
			else
				menu.findItem(R.id.menu_tile_mapnik_by_night).setChecked(true);
		}
		else if (map.getTileProvider().getTileSource() == MAPBOXSATELLITELABELLED)
			menu.findItem(R.id.menu_tile_mapbox_satellite).setChecked(true);

		return true;
	}

	@Override public boolean onPrepareOptionsMenu(Menu menu) {
		mFriendsManager.onPrepareOptionsMenu(menu);

		if (mRoads != null && mRoads[mSelectedRoad].mNodes.size()>0)
			menu.findItem(R.id.menu_itinerary).setEnabled(true);
		else
			menu.findItem(R.id.menu_itinerary).setEnabled(false);

		if (mPOIs != null && mPOIs.size()>0)
			menu.findItem(R.id.menu_pois).setEnabled(true);
		else
			menu.findItem(R.id.menu_pois).setEnabled(false);
		return true;
	}

	/** return the index of the first Marker having its bubble opened, -1 if none */
	static int getIndexOfBubbledMarker(List<? extends Overlay> list) {
		for (int i=0; i<list.size(); i++){
			Overlay item = list.get(i);
			if (item instanceof Marker){
				Marker marker = (Marker)item;
				if (marker.isInfoWindowShown())
					return i;
			}
		}
		return -1;
	}

	void setStdTileProvider(){
		if (!(map.getTileProvider() instanceof MapTileProviderBasic)){
			MapTileProviderBasic bitmapProvider = new MapTileProviderBasic(this);
			map.setTileProvider(bitmapProvider);
		}
	}

	boolean setMapsForgeTileProvider(){
		String path = Environment.getExternalStorageDirectory().getPath()+"/mapsforge/";
		Toast.makeText(this, "Loading MapsForge .map files and rendering theme from " + path, Toast.LENGTH_LONG).show();
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		if (listOfFiles == null)
			return false;

		//Build a list with only .map files; get rendering config file if any:
		File renderingFile = null;
		ArrayList<File> listOfMapFiles = new ArrayList<>(listOfFiles.length);
		for (File file:listOfFiles){
			if (file.isFile() && file.getName().endsWith(".map")){
				listOfMapFiles.add(file);
			} else if (file.isFile() && file.getName().endsWith(".xml")) {
				renderingFile = file;
			}
		}
		listOfFiles = new File[listOfMapFiles.size()];
		listOfFiles = listOfMapFiles.toArray(listOfFiles);

		//Use rendering file if any
		XmlRenderTheme theme = null;
		try {
			if (renderingFile != null)
				theme = new ExternalRenderTheme(renderingFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		MapsForgeTileSource source = MapsForgeTileSource.createFromFiles(listOfFiles, theme, "rendertheme-v4");
		MapsForgeTileProvider mfProvider = new MapsForgeTileProvider(new SimpleRegisterReceiver(this), source, null);
		map.setTileProvider(mfProvider);
		/*
		map.getController().setZoom((double)source.getMinimumZoomLevel());
		map.zoomToBoundingBox(source.getBoundsOsmdroid(), true);
		*/
		return true;
	}

	private class CacheClearer extends AsyncTask<Void, Void, Boolean> {
		protected Boolean doInBackground(Void... params) {
			IFilesystemCache tileWriter = map.getTileProvider().getTileWriter();
			if (tileWriter instanceof SqlTileWriter) {
				return ((SqlTileWriter) tileWriter).purgeCache();
			} else
				return false;
		}
		protected void onPostExecute(Boolean result) {
			if (result)
				Toast.makeText(map.getContext(), "Cache Purge successful", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(map.getContext(), "Cache Purge failed", Toast.LENGTH_SHORT).show();
		}
	}


	



	@Override public boolean onOptionsItemSelected(MenuItem item) {
		Intent myIntent;
		switch (item.getItemId()) {
			case R.id.menu_sharing:
				return mFriendsManager.onOptionsItemSelected(item);
		case R.id.menu_itinerary:
			myIntent = new Intent(this, RouteActivity.class);
			int currentNodeId = getIndexOfBubbledMarker(mRoadNodeMarkers.getItems());
			myIntent.putExtra("SELECTED_ROAD", mSelectedRoad);
			myIntent.putExtra("NODE_ID", currentNodeId);
			startActivityForResult(myIntent, ROUTE_REQUEST);
			return true;
		case R.id.menu_pois:
			myIntent = new Intent(this, POIActivity.class);
			myIntent.putExtra("ID", getIndexOfBubbledMarker(mPoiMarkers.getItems()));
			startActivityForResult(myIntent, POIS_REQUEST);
			return true;
		case R.id.menu_kml_url:
			openUrlDialog();
			return true;
		case R.id.menu_open_file:
			openLocalFileDialog(true);
			return true;
		case R.id.menu_overpass_api:
			openOverpassAPIWizard();
			return true;
			case R.id.menu_kml_record_track:
				mIsRecordingTrack = !mIsRecordingTrack;
				mFriendsManager.setTracksRecording(mIsRecordingTrack);
				if (mIsRecordingTrack)
					item.setTitle(R.string.menu_kml_stop_record_tracks);
				else
					item.setTitle(R.string.menu_kml_record_tracks);
				return true;
		case R.id.menu_kml_get_overlays:
			insertOverlaysInKml();
			updateUIWithKml();
			return true;
		case R.id.menu_kml_tree:
			myIntent = new Intent(this, KmlTreeActivity.class);
			//myIntent.putExtra("KML", mKmlDocument.kmlRoot);
			mKmlStack.push(mKmlDocument.mKmlRoot);
			startActivityForResult(myIntent, KmlTreeActivity.KML_TREE_REQUEST);
			return true;
		case R.id.menu_kml_styles:
			myIntent = new Intent(this, KmlStylesActivity.class);
			startActivityForResult(myIntent, KmlStylesActivity.KML_STYLES_REQUEST);
			return true;
		case R.id.menu_save_file:
			openLocalFileDialog(false);
			return true;
		case R.id.menu_kml_clear:
			mKmlDocument = new KmlDocument();
			updateUIWithKml();
			return true;
		case R.id.menu_route_osrm:
			mWhichRouteProvider = OSRM;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case  R.id.menu_route_ors:
			mWhichRouteProvider= ORS;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_route_graphhopper_fastest:
			mWhichRouteProvider = GRAPHHOPPER_FASTEST;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_route_graphhopper_bicycle:
			mWhichRouteProvider = GRAPHHOPPER_BICYCLE;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_route_graphhopper_pedestrian:
			mWhichRouteProvider = GRAPHHOPPER_PEDESTRIAN;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_route_google:
			mWhichRouteProvider = GOOGLE_FASTEST;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_tile_mapnik:
			setStdTileProvider();
			map.setTileSource(TileSourceFactory.MAPNIK);
			map.getOverlayManager().getTilesOverlay().setColorFilter(null);
			mNightMode = false;
			item.setChecked(true);
			return true;
		case R.id.menu_tile_mapnik_by_night:
			setStdTileProvider();
			map.setTileSource(TileSourceFactory.MAPNIK);
			map.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);
			mNightMode = true;
			item.setChecked(true);
			return true;
		case R.id.menu_tile_mapbox_satellite:
			setStdTileProvider();
			map.setTileSource(MAPBOXSATELLITELABELLED);
			map.getOverlayManager().getTilesOverlay().setColorFilter(null);
			item.setChecked(true);
			return true;
		case R.id.menu_tile_mapsforge:
			boolean result = setMapsForgeTileProvider();
			if (result)
				item.setChecked(true);
			else
				Toast.makeText(this, "No MapsForge map found", Toast.LENGTH_SHORT).show();
			return true;
			/*
		case R.id.menu_download_view_area:{
			CacheManager cacheManager = new CacheManager(map);
			int zoomMin = map.getZoomLevel();
			int zoomMax = map.getZoomLevel()+4;
			cacheManager.downloadAreaAsync(this, map.getBoundingBox(), zoomMin, zoomMax);
			return true;
			}
		case R.id.menu_clear_view_area:{
			new CacheClearer().execute();
			return true;
			}
		case R.id.menu_cache_usage:{
			CacheManager cacheManager = new CacheManager(map);
			long cacheUsage = cacheManager.currentCacheUsage()/(1024*1024);
			long cacheCapacity = cacheManager.cacheCapacity()/(1024*1024);
			float percent = 100.0f*cacheUsage/cacheCapacity;
			String message = "Cache usage:\n"+cacheUsage+" Mo / "+cacheCapacity+" Mo = "+(int)percent + "%";
			Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
			return true;
			}*/
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	//------------ LocationListener implementation
	private final NetworkLocationIgnorer mIgnorer = new NetworkLocationIgnorer();
	long mLastTime = 0; // milliseconds
	double mSpeed = 0.0; // km/h
	@Override public void onLocationChanged(final Location pLoc) {
		Log.w("POSITION",String.valueOf(myLocationOverlay.getLocation()));
		//TODO NICE METHODE KANN HIER REIN----------------------------------------------------

		if (mRoads != null && mRoads[mSelectedRoad].mNodes.size()>0&&navInProgress) {
			calcDistanceNav();
		}

		long currentTime = System.currentTimeMillis();
		if (mIgnorer.shouldIgnore(pLoc.getProvider(), currentTime))
            return;
		double dT = currentTime - mLastTime;
		if (dT < 100.0){
			//Toast.makeText(this, pLoc.getProvider()+" dT="+dT, Toast.LENGTH_SHORT).show();
			return;
		}
		mLastTime = currentTime;

		GeoPoint newLocation = new GeoPoint(pLoc);
		if (!myLocationOverlay.isEnabled()){
			//we get the location for the first time:
			myLocationOverlay.setEnabled(true);
			map.getController().animateTo(newLocation);
		}

		GeoPoint prevLocation = myLocationOverlay.getLocation();
		myLocationOverlay.setLocation(newLocation);
		myLocationOverlay.setAccuracy((int)pLoc.getAccuracy());

		if (prevLocation != null && pLoc.getProvider().equals(LocationManager.GPS_PROVIDER)){
			mSpeed = pLoc.getSpeed() * 3.6;
			long speedInt = Math.round(mSpeed);
			TextView speedTxt = (TextView)findViewById(R.id.speed);
			speedTxt.setText(speedInt + " km/h");

			//TODO: check if speed is not too small
			if (mSpeed >= 0.1){
				mAzimuthAngleSpeed = pLoc.getBearing();
				myLocationOverlay.setBearing(mAzimuthAngleSpeed);
			}
		}

		if (mTrackingMode){
			//keep the map view centered on current location:
			map.getController().animateTo(newLocation);
			map.setMapOrientation(-mAzimuthAngleSpeed);
		} else {
			//just redraw the location overlay:
			map.invalidate();
		}

		if (mIsRecordingTrack) {
			recordCurrentLocationInTrack("my_track", "My Track", newLocation);
		}
	}

	static int[] TrackColor = {
		Color.CYAN-0x20000000, Color.BLUE-0x20000000, Color.MAGENTA-0x20000000, Color.RED-0x20000000, Color.YELLOW-0x20000000
	};

	KmlTrack createTrack(String id, String name) {
		KmlTrack t = new KmlTrack();
		KmlPlacemark p = new KmlPlacemark();
		p.mId = id;
		p.mName = name;
		p.mGeometry = t;
		mKmlDocument.mKmlRoot.add(p);
		//set a color to this track by creating a style:
		Style s = new Style();
		int color;
		try {
			color = Integer.parseInt(id);
			color = color % TrackColor.length;
			color = TrackColor[color];
		} catch (NumberFormatException e) {
			color = Color.GREEN-0x20000000;
		}
		s.mLineStyle = new LineStyle(color, 8.0f);
		String styleId = mKmlDocument.addStyle(s);
		p.mStyle = styleId;
		return t;
	}

	void recordCurrentLocationInTrack(String trackId, String trackName, GeoPoint currentLocation) {
		//Find the KML track in the current KML structure - and create it if necessary:
		KmlTrack t;
		KmlFeature f = mKmlDocument.mKmlRoot.findFeatureId(trackId, false);
		if (f == null)
			t = createTrack(trackId, trackName);
		else if (!(f instanceof KmlPlacemark))
			//id already defined but is not a PlaceMark
			return;
		else {
			KmlPlacemark p = (KmlPlacemark)f;
			if (!(p.mGeometry instanceof KmlTrack))
				//id already defined but is not a Track
				return;
			else
				t = (KmlTrack) p.mGeometry;
		}
		//TODO check if current location is really different from last point of the track
		//record in the track the current location at current time:
		t.add(currentLocation, new Date());
		//refresh KML:
		updateUIWithKml();
	}

	@Override public void onProviderDisabled(String provider) {}

	@Override public void onProviderEnabled(String provider) {}

	@Override public void onStatusChanged(String provider, int status, Bundle extras) {}

	//------------ SensorEventListener implementation
	@Override public void onAccuracyChanged(Sensor sensor, int accuracy) {
		myLocationOverlay.setAccuracy(accuracy);
		map.invalidate();
	}

	//static float mAzimuthOrientation = 0.0f;
	@Override public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()){
			case Sensor.TYPE_ORIENTATION:
				if (mSpeed < 0.1){
					/* TODO Filter to implement...
					float azimuth = event.values[0];
					if (Math.abs(azimuth-mAzimuthOrientation)>2.0f){
						mAzimuthOrientation = azimuth;
						myLocationOverlay.setBearing(mAzimuthOrientation);
						if (mTrackingMode)
							map.setMapOrientation(-mAzimuthOrientation);
						else
							map.invalidate();
					}
					*/
				}
				//at higher speed, we use speed vector, not phone orientation. 
				break;
			default:
				break;
		}
	}

}
