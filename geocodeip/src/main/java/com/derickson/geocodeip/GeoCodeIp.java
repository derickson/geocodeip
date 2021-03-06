package com.derickson.geocodeip;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class GeoCodeIp {

	private boolean isInitialized = false;
	private HashMap<Integer,DBObject> gazes = new HashMap<Integer,DBObject>(256);
	private List<Double> zeroZero = new ArrayList<Double>(2);
	
	public GeoCodeIp() {
		zeroZero.add(new Double(0.0));
		zeroZero.add(new Double(0.0));
		load();
	}
	
	private void load(){
		System.out.println("Loading IP to Geo Gazateer");
		MongoClient client = null;
		try {
			client = new MongoClient("localhost", 27017);
			DB gazDB = client.getDB("gaz");
			DBCollection gazCache = gazDB.getCollection("gazCache");
			
			if(gazCache.count() > 0){
				DBCursor cursor = gazCache.find();
				//System.out.print("Loading: ");
				//int i=0;
				while(cursor.hasNext()){
					DBObject obj = cursor.next();
					Integer objId = (Integer) obj.get("_id");
					//System.out.print(objId+", " );
					//if(i % 10 == 0) System.out.println("");
					gazes.put( objId , obj);
					//++i;
				}
				//System.out.println("");
			} else {
				//TODO load from the CSV file
			}			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} finally {
			if(client != null) client.close();
			//System.out.println("Done Loading");
			if(gazes.size() == 256) this.isInitialized = true;
		}
	}
	
	public List<Double> getLonLat(final String ip){
		String[] ipParts = ip.split("\\.");
		if(ipParts.length != 4) return zeroZero;
		
		Integer gazIndex = new Integer(ipParts[0]);
		int fourth = Integer.parseInt(ipParts[3]);
		String prefix = ipParts[0]+"_"+ipParts[1]+"_"+ipParts[2];
		DBObject gaz = gazes.get(gazIndex);
		
		if( gaz.containsField(prefix)){
			BasicDBList entry = null;
			DBObject gazEntries = (DBObject) gaz.get(prefix);
			
			Set<String> keySet = gazEntries.keySet();
			int keySetSize = keySet.size();
			
			if(keySetSize == 1){
				entry = (BasicDBList) gazEntries.get(  keySet.iterator().next() );
			} else {
				int[] keySetInt = new int[keySetSize];
				int i = 0;
				for(String key : keySet){
					keySetInt[i] = Integer.parseInt(key);
					++i;
				}
				Arrays.sort(keySetInt);
				
				for(i=0; i<keySetSize; ++i){
					int key = keySetInt[i];
					if(key <= fourth){
						entry = (BasicDBList) gazEntries.get( Integer.toString(key) );
					} else {
						break;
					}
				}
			}

			return (List<Double>) entry.get(2);
		}
		
		return zeroZero;
	}

}
