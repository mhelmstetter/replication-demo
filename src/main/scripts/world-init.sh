#!/bin/bash

WEST=192.168.2.101

mongo replicationConfig <<EOF
db.replicationConfig.drop()
db.replicationConfig.save(
  {
	"_id" : NumberLong(0),
	"replicationSources" : [
		{
			"oplogBaseQuery" : "{ns:'world.flightTrack'}",
			"hostname" : "localhost",
			"port" : 27017
		}
	]
  }
)
db.replicationConfig.save(
  {
	"_id" : NumberLong(1),
	"replicationSources" : [
		{
			"oplogBaseQuery" : "{ns:'region.flightTrack', 'o.airline':{\$exists:true}}",
			"hostname" : "$WEST",
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
