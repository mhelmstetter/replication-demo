#!/bin/bash

WORLD=192.168.2.100

mongo replicationConfig <<EOF
db.replicationConfig.drop()
db.replicationConfig.save(
  {
	"_id" : NumberLong(2),
	"replicationSources" : [
		{
			"oplogBaseQuery" : "{ns:'region.flightTrack'}",
			"hostname" : "localhost",
			"port" : 27017
		}
	]
  }
)
db.replicationConfig.save(
  {
	"_id" : NumberLong(3),
	"replicationSources" : [
		{
			"oplogBaseQuery" : "{ns:'world.flightTrack', 'o.region':{\$ne:'west'}}",
			"hostname" : "$WORLD",
			"port" : 27017
		}
	],
	"replicationTarget" : {
		"collectionMappings" : [ ],
		"databaseMappings" : [ ],
		"hostname" : "localhost",
		"port" : 27017
	}
  }
)
EOF
