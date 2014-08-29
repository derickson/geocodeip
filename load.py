
from pymongo import MongoClient
import csv

## GEO IP functions


## returns a rough guess at the [lon, lat] for an ip
def getlonlat(gazes, ip):
	print "ip", ip
	ipParts = ip.split(".")
	prefix = (".".join(ipParts[:3])).replace(".","_")
	if(prefix in gazes[ipParts[0]]):
		entry = None
		gazEntries = gazes[ipParts[0]][prefix]
		keys = gazEntries.keys()
		sortedKeys = sorted(keys, key=float)
		if( len(sortedKeys) == 1 or len(ipParts) != 4):
			entry = gazEntries[sortedKeys[0]]
		else:
			for k in sorted(keys, key=float):
				if (k <= ipParts[3]):
					entry = gazEntries[k]
				else:
					break
		return entry[2]
	else:
		return [0,0]

## LOAD THE GAZATEER
## utilty method while loading
def _putGeoGaz(gazes, ip,range,geo):
	ipParts = ip.split(".")
	prefix = (".".join(ipParts[:3])).replace(".","_")
	if( len(ipParts) == 4):
		f = ipParts[3]
		gaz = gazes[ipParts[0]]
		if(prefix in gaz):
			gaz[prefix][f] = [ip,range,geo]
		else:
			gaz[prefix] = {f: [ip,range,geo]}
## LOADER ROUTINE
def loadGaz():
	gazes = {}
	for i in range(0,256) :
		gazes[str(i)] = { "_id": i}
	client = MongoClient()
	gazDB = client.gaz
	gazCache = gazDB.gazCache
	if(gazCache.count() > 0 ):
		for g in gazCache.find():
			gazes[str(g[u'_id'])] = g
	else:
		with open('GeoLite2-City-CSV_20140805/GeoLite2-City-Blocks.csv', 'rb') as csvfile:
			gazreader = csv.reader(csvfile, delimiter=',', quotechar='"')
			next(gazreader, None) 
			for row in gazreader:
				if(row[6] == ''  and row[7] == '' or row[9] == '1'):
					_putGeoGaz(gazes, row[0][7:], row[1], [ 0.0 , 0.0 ])
				else:
					_putGeoGaz(gazes, row[0][7:], row[1], [ float(row[7]), float(row[6]) ])
		for k in gazes.keys():
			gazCache.insert( gazes[k] )
	print "Done Loading Gaz"
	return gazes

gazes = loadGaz()
			
print "12.152.107.1", getlonlat(gazes, "12.152.107.1")
print "12.152.107.66", getlonlat(gazes, "12.152.107.66")
print "96.241.81.50", getlonlat(gazes, "96.241.81.50")
