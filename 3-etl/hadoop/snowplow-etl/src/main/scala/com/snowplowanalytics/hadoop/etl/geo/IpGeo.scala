/*
 * Copyright (c) 2012 SnowPlow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.snowplowanalytics.hadoop.etl.geo

// LRU
import com.twitter.util.LruMap

// MaxMind
import com.maxmind.geoip.{Location, LookupService}

/**
 * IpGeo is a wrapper around MaxMind's own LookupService
 * As well as making LookupService a little more Scala-
 * friendly, IpGeo also introduces a 10k-element LRU
 * cache to reduce lookup frequency.
 *
 * Inspired by https://github.com/jt6211/hadoop-dns-mining/blob/master/src/main/java/io/covert/dns/geo/IpGeo.java
 */
class IpGeo(dbFile: String, options: Int = LookupService.GEOIP_MEMORY_CACHE) {

	// Initialise the cache
	private val lru = new LruMap[String, Location](10000) // Of type mutable.Map[String, Location]

	// Configure the lookup service
	private val maxmind = new LookupService(dbFile, options);

	// Define an empty location
	// TODO: check this works
	private val noLocation = new Location("", "", "", "", "", "", "")

	/**
	 * Returns the MaxMind location for this IP address.
	 * If MaxMind can't find the IP address, then return
	 * an empty location.
	 */
	def getLocation(ip: String): Option[Location] = {

		// First check the LRU cache
		val cached = lru.get(ip)
		if (cached != null) {
			return cached
		}

		// Now try MaxMind
		val location = maxmind.getLocation(ip)
		
		if (location == null) {
			lru.put(ip, noLocation)
			return noLocation
		}

		lru.put(ip, location)
		location
	}
}