#!/bin/bash

WORLD=192.168.1.104

mongo replicationConfig <<EOF
db.replicationConfig.save(
  {
	"_id" : NumberLong(2),
	"replicationSources" : [
		{
			"oplogBaseQuery" : "{ns:'world.flightTrack', 'o.region':{\$ne:'east'}}",
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
