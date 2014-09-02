package com.derickson.geocodeip;

import java.net.UnknownHostException;
import java.util.ArrayList;
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
		System.out.println("Coding: "+ip);
		String[] ipParts = ip.split("\\.");
		if(ipParts.length != 4) return zeroZero;
		
		Integer gazIndex = new Integer(ipParts[1]);
		String prefix = String.format("%i_%i_%i",ipParts[0],ipParts[1],ipParts[2]);
		DBObject gaz = gazes.get(gazIndex);
		
		if( gaz.containsField(prefix)){
			BasicDBList entry = null;
			DBObject gazEntries = (DBObject) gaz.get(prefix);
			
			Set<String> keySet = gazEntries.keySet();
			int keySetSize = keySet.size();
			
			if(keySetSize == 0){
				entry = (BasicDBList) gazEntries.get(  keySet.iterator().next() );
			}
			
			int[] keySetInt = new int[keySetSize];
			int i = 0;
			for(String key : keySet){
				keySetInt[i] = Integer.parseInt(key);
				++i;
			}
			
			
			
		}
		
		return zeroZero;
	}

}
