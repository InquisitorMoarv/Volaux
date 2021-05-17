package org.osmdroid.bonuspack.routing;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.bonuspack.utils.PolylineEncoder;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/** get a route between a start and a destination point, going through a list of waypoints.
 * It uses GraphHopper, an open source routing service based on OpenSteetMap data. <br>
 * 
 * It requests by default the GraphHopper demo site. 
 * Use setService() to request another (for instance your own) GraphHopper-compliant service. <br> 
 * 
 * @see <a href="https://github.com/graphhopper/web-api/blob/master/docs-routing.md">GraphHopper</a>
 * @author M.Kergall
 */
public class OpenRouteServiceRoadManager extends RoadManager {

	protected static final String SERVICE = "https://api.openrouteservice.org/v2/directions/wheelchair?";
	//public static final int STATUS_NO_ROUTE = Road.STATUS_TECHNICAL_ISSUE+1;

	protected String mServiceUrl;
	protected String mKey;
	protected boolean mWithElevation;
	protected boolean mAlternateAvailable;

	/** mapping from GraphHopper directions to MapQuest maneuver IDs:

	 MAPQUEST IDs:
	 * NONE	0	No maneuver occurs here.
	 * STRAIGHT			1	Continue straight.
	 * BECOMES			2	No maneuver occurs here; road name changes.
	 * SLIGHT_LEFT		3	Make a slight left.
	 * LEFT				4	Turn left.
	 * SHARP_LEFT		5	Make a sharp left.
	 * SLIGHT_RIGHT		6	Make a slight right.
	 * RIGHT			7	Turn right.
	 * SHARP_RIGHT		8	Make a sharp right.
	 * STAY_LEFT		9	Stay left.
	 * STAY_RIGHT		10	Stay right.
	 * STAY_STRAIGHT	11	Stay straight.
	 * UTURN			12	Make a U-turn.
	 * UTURN_LEFT		13	Make a left U-turn.
	 * UTURN_RIGHT		14	Make a right U-turn.
	 * EXIT_LEFT		15	Exit left.
	 * EXIT_RIGHT		16	Exit right.
	 * RAMP_LEFT		17	Take the ramp on the left.
	 * RAMP_RIGHT		18	Take the ramp on the right.
	 * RAMP_STRAIGHT	19	Take the ramp straight ahead.
	 * MERGE_LEFT		20	Merge left.
	 * MERGE_RIGHT		21	Merge right.
	 * MERGE_STRAIGHT	22	Merge.
	 * ENTERING			23	Enter state/province.
	 * DESTINATION		24	Arrive at your destination.
	 * DESTINATION_LEFT	25	Arrive at your destination on the left.
	 * DESTINATION_RIGHT	26	Arrive at your destination on the right.
	 * ROUNDABOUT1		27	Enter the roundabout and take the 1st exit.
	 * ROUNDABOUT2		28	Enter the roundabout and take the 2nd exit.
	 * ROUNDABOUT3		29	Enter the roundabout and take the 3rd exit.
	 * ROUNDABOUT4		30	Enter the roundabout and take the 4th exit.
	 * ROUNDABOUT5		31	Enter the roundabout and take the 5th exit.
	 * ROUNDABOUT6		32	Enter the roundabout and take the 6th exit.
	 * ROUNDABOUT7		33	Enter the roundabout and take the 7th exit.
	 * ROUNDABOUT8		34	Enter the roundabout and take the 8th exit.
	 * TRANSIT_TAKE		35	Take a public transit bus or rail line.
	 * TRANSIT_TRANSFER	36	Transfer to a public transit bus or rail line.
	 * TRANSIT_ENTER	37	Enter a public transit bus or rail station.
	 * TRANSIT_EXIT		38	Exit a public transit bus or rail station.
	 * TRANSIT_REMAIN_ON	39	Remain on the current bus/rail car.
	 *
	  ORS Instruction Types
	 Value	Encoding
		 0	Left
		 1	Right
		 2	Sharp left
		 3	Sharp right
		 4	Slight left
		 5	Slight right
		 6	Straight
		 7	Enter roundabout
		 8	Exit roundabout
		 9	U-turn
		 10	Goal
		 11	Depart
		 12	Keep left
	 	13	Keep right
	 * */

	//TODO Map the shit from ORS to graphopper to aaaaaaahm Mapquest
	static final HashMap<Integer, Integer> MANEUVERS;
	static {
		MANEUVERS = new HashMap<Integer, Integer>();
		MANEUVERS.put(0, 4);//Left
		MANEUVERS.put(1, 7);//Right
		MANEUVERS.put(2, 5);//Sharp left
		MANEUVERS.put(3, 8);//Sharp right
		MANEUVERS.put(4, 3);//Slight left
		MANEUVERS.put(5, 6);//Slight right
		MANEUVERS.put(6, 1);//Straight
		MANEUVERS.put(7, 1);//Enter roundabout
		MANEUVERS.put(8, 1);//Exit roundabout
		MANEUVERS.put(9, 12);//U-turn
		MANEUVERS.put(10, 24);//Goal
		MANEUVERS.put(11, 1);//Depart //Nicht ganz klar.. gibt kein bei Mapquest https://developer.mapquest.com/documentation/open/guidance-api/v1/route/get/
		MANEUVERS.put(12, 20);//Keep left  //Auch nicht ganz klar..  Eventuell nur für Autos relevant
		MANEUVERS.put(13, 21);//Keep right
	}

	/**
	 * @param apiKey GraphHopper API key, mandatory to use the public GraphHopper service.
	 * @see <a href="http://graphhopper.com/#enterprise">GraphHopper</a> to obtain an API key.
	 */
	public OpenRouteServiceRoadManager(String apiKey, boolean alternateAvailable) {
		super();
		mServiceUrl = SERVICE;
		mKey = apiKey;
		mWithElevation = false;
		mAlternateAvailable = alternateAvailable;
	}
	
	/** allows to request on an other site than GraphHopper demo site */
	public void setService(String serviceUrl){
		mServiceUrl = serviceUrl;
	}
	
	/** set if altitude of every route point should be requested or not. Default is false. */
	public void setElevation(boolean withElevation){
		mWithElevation = withElevation;
	}

	//TODO:
	// NEEDS TO BE A JSON LATER
	protected String getUrl(ArrayList<GeoPoint> waypoints) {
		StringBuilder urlString = new StringBuilder(mServiceUrl);
		urlString.append("api_key="+mKey);
		urlString.append("&start="+waypoints.get(0).getLongitude()+","+waypoints.get(0).getLatitude());
		urlString.append("&end="+waypoints.get(1).getLongitude()+","+waypoints.get(1).getLatitude());

		//for (int i=0; i<waypoints.size(); i++){
		//	GeoPoint p = waypoints.get(i);
		//	urlString.append("&point="+geoPointAsString(p));
		//}
		//urlString.append("&instructions=true"); already set by default
		//urlString.append("&elevation="+(mWithElevation?"true":"false"));
		//if (getAlternate && mAlternateAvailable)
			//urlString.append("&ch.disable=true&algorithm=alternative_route");
		//urlString.append(mOptions);
		return urlString.toString();
	}

	protected Road[] defaultRoad(ArrayList<GeoPoint> waypoints) {
		Road[] roads = new Road[1];
		roads[0] = new Road(waypoints);
		return roads;
	}


// Wandelt ORS API respons in Mapquest format um
	public Road[] getRoads(ArrayList<GeoPoint> waypoints) {
		String url = getUrl(waypoints);
		Log.d(BonusPackHelper.LOG_TAG, "ORS.getRoads:" + url);
		//TODO hier auf pedestrian umstellen wenn das erste nicht geht
		//{"error":{"code":2009,"message":"Route could not be found - Unable to find a route between points 1 (8.8785778 53.0994245) and 2 (8.8805703 53.0974846)."},"info":{"engine":{"version":"6.4.3","build_date":"2021-05-04T07:46:55Z"},"timestamp":1620730017496}}
		String jString = BonusPackHelper.requestStringFromUrl(url);
		if (jString == null) {
			return defaultRoad(waypoints);
		}
		try {
			JSONObject jRoot = new JSONObject(jString);
			JSONArray jPaths = jRoot.optJSONArray("features"); //paths
			if (jPaths == null || jPaths.length() == 0){
				return defaultRoad(waypoints);
				/*
				road = new Road(waypoints);
				road.mStatus = STATUS_NO_ROUTE;
				return road;
				*/
			}
			Road[] roads = new Road[jPaths.length()];
			for (int r = 0; r < jPaths.length(); r++) {
				//Hier wieder ein Objekt drauß machen
				JSONObject jPath = jPaths.getJSONObject(r); //loop kann eigentlich dur 0 ersetzt werden
				//String route_geometry = jPath.getJSONObject("geometry").getString("type");//Points
				ArrayList<GeoPoint> route_geometry = new ArrayList<>();

				JSONArray cor = jPath.getJSONObject("geometry").getJSONArray("coordinates");//Points
				for (int i=0; i < cor.length(); i++) {
					String t = cor.get(i).toString().replaceAll("[\\[\\](){}]","");//[8.853937,53.109558]
					List<String> geopoints = Arrays.asList(t.split(","));
					GeoPoint a = new GeoPoint(Double.parseDouble(geopoints.get(1)),Double.parseDouble(geopoints.get(0))) ;
					route_geometry.add(a);
				}
				Road road = new Road();
				roads[r] = road;
				road.mRouteHigh = route_geometry;
				//road.mRouteHigh = PolylineEncoder.encode(route_geometry, 10);
				//road.mRouteHigh = PolylineEncoder.decode(PolylineEncoder.encode(route_geometry, 10);, 10, mWithElevation);
				//Todo FIX TAHT SHIT
				JSONArray jInstructions = jPath.getJSONObject("properties").getJSONArray("segments").getJSONObject(0).getJSONArray("steps");
				//int asd = 2;
				//JSONArray jInstructions = jPath.getJSONObject("properties").getJSONObject("segments").getJSONArray("steps");//Instructions  FEHLER
				int n = jInstructions.length();
				for (int i = 0; i < n; i++) {
					JSONObject jInstruction = jInstructions.getJSONObject(i);
					RoadNode node = new RoadNode();
					JSONArray jInterval = jInstruction.getJSONArray("way_points");//interval
					int positionIndex = jInterval.getInt(0);
					node.mLocation = road.mRouteHigh.get(positionIndex);
					node.mLength = jInstruction.getDouble("distance") / 1000.0;//Distance , ändert sich nicht
					node.mDuration = jInstruction.getInt("duration");// / 1000.0; //Segment duration in seconds. //Time
					int direction = jInstruction.getInt("type");//sign
					node.mManeuverType = getManeuverCode(direction);
					node.mInstructions = jInstruction.getString("instruction");//text
					road.mNodes.add(node);
				}
				road.mLength =  jPath.getJSONObject("properties").getJSONArray("segments").getJSONObject(0).getDouble("distance") / 1000.0;
				road.mDuration =  jPath.getJSONObject("properties").getJSONArray("segments").getJSONObject(0).getDouble("duration");// / 1000.0; //Time
				JSONArray jBBox = jPath.getJSONArray("bbox");
				road.mBoundingBox = new BoundingBox(jBBox.getDouble(3), jBBox.getDouble(2),
						jBBox.getDouble(1), jBBox.getDouble(0));
				road.mStatus = Road.STATUS_OK;
				road.buildLegs(waypoints);
				Log.d(BonusPackHelper.LOG_TAG, "GraphHopper.getRoads - finished");
			}
			return roads;
		} catch (JSONException e) {
			e.printStackTrace();
			return defaultRoad(waypoints);
		}
	}

	//@Override public Road[] getRoads(ArrayList<GeoPoint> waypoints) {
	//	return getRoads(waypoints);
	//}

	@Override
	public Road getRoad(ArrayList<GeoPoint> waypoints) {
		Road[] roads = getRoads(waypoints);
		return roads[0];
	}

	protected int getManeuverCode(int direction){
		Integer code = MANEUVERS.get(direction);
		if (code != null)
			return code;
		else 
			return 0;
	}

}
