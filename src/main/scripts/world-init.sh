#!/bin/bash

EAST=192.168.1.101

mongo replicationConfig <<EOF
db.replicationConfig.save(
  {
	"_id" : NumberLong(1),
	"replicationSources" : [
		{
			"oplogBaseQuery" : "{ns:'region.flightTrack', 'o.airline':{\$exists:true}}",
			"hostname" : "$EAST",
			"port" : 27017
		}
	],
	"replicationTarget" : {
		"collectionMappings" : [ ],
		"databaseMappings" : [
			{
				"sourceDatabaseName" : "region",
				"destinationDatabaseName" : "world"
			}
		],
		"hostname" : "localhost",
		"port" : 27017
	}
  }
)
EOF
