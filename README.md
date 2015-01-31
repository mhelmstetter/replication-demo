# replication-demo

## Building

Building is easy:
    git clone
    mvn package

This will produce a binary assembly at `target/replication-demo-<version>-bin.tar.gz`.

## Running

The easiest way to run is to build the binary assembly, and unpack the `.tar.gz` file. Then `cd` to the `bin` directory where you will find several scripts. 

### Single Machine Setup
You will need to start 2 MongoDB instances that are configured to run as single member replica sets:

    
    mongod --replSet east --port 27017 --dbpath /data/db2 --smallfiles
    mongod --replSet world --port 37017 --smallfiles
    
After both instances are started, login to each of them via the mongo shell and run `rs.initiate()`.

After you have a local MongoDB instance running as a replica set, initialize your replication configuration. There are scripts provided will use the `mongo` shell to insert the data into the `replicationConfig` database. Note that the replication configuration will always be stored and accessed via localhost:27017.

## Key Classes
###GeoTrackGenerator
Reads flight information from tracks.json and inserts data into the "track" database, "flightTrack" collection on `localhost:27017`.
Each flight in the tracks.json file is a single document, containing an array of flight track information.
The generator "explodes" this such that 1 document is generated for every point along the flight track.

###ReplicationManager
This class reads ReplicationSource data from the database. For each ReplicationSource an OplogTailThread is created.


