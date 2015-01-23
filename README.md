# replication-demo


GeoTrackGenerator
Reads flight information from tracks.json and inserts data into the "track" database, "flightTrack" collection.
Each flight in the tracks.json file is a single document, containing an array of flight track information.
The generator "explodes" this such that 1 document is generated for every point along the flight track.