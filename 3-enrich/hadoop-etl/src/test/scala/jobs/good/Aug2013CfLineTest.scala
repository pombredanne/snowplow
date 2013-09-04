/*
 * Copyright (c) 2012-2013 SnowPlow Analytics Ltd. All rights reserved.
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
package com.snowplowanalytics.snowplow.enrich.hadoop
package jobs
package good

// Scala
import scala.collection.mutable.Buffer

// Specs2
import org.specs2.mutable.Specification

// Scalding
import com.twitter.scalding._

// Cascading
import cascading.tuple.TupleEntry

// This project
import JobTestHelpers._

/**
 * Holds the input and expected data
 * for the test.
 */
object Aug2013CfLineTest {

  val lines = Lines(
    "2013-08-29	00:18:48	LAX3	830	173.51.104.164	GET	d3v6ndkyapxc2w.cloudfront.net	/i	200	http://snowplowanalytics.com/analytics/index.html	Mozilla/5.0%20(Windows%20NT%205.1;%20rv:23.0)%20Gecko/20100101%20Firefox/23.0	e=pv&page=Introduction%20-%20Snowplow%20Analytics&dtm=1377735557970&tid=567074&vp=1024x635&ds=1024x635&vid=1&duid=7969620089de36eb&p=web&tv=js-0.12.0&fp=308909339&aid=snowplowweb&lang=en-US&cs=UTF-8&tz=America%2FLos_Angeles&refr=http%3A%2F%2Fwww.metacrawler.com%2Fsearch%2Fweb%3Ffcoid%3D417%26fcop%3Dtopnav%26fpid%3D27%26q%3Dsnowplow%2Banalytics%26ql%3D&f_pdf=1&f_qt=1&f_realp=0&f_wma=1&f_dir=0&f_fla=1&f_java=1&f_gears=0&f_ag=0&res=1024x768&cd=24&cookie=1&url=http%3A%2F%2Fsnowplowanalytics.com%2Fanalytics%2Findex.html	-	Hit	wQ1OBZtQlGgfM_tPEJ-lIQLsdra0U-lXgmfJfwja2KAV_SfTdT3lZg=="
    )

  val expected = List(
    "snowplowweb",
    "web",
    "2013-08-29 00:18:48.000",
    "2013-08-29 00:19:17.970",
    "page_view",
    "com.snowplowanalytics",
    null, // We can't predict the event_id
    "567074",
    "js-0.12.0",
    "cloudfront",
    EtlVersion,
    null, // No user_id set
    "255.255.255.255",
    "1640945579",
    "132e226e3359a9cd",
    "1",
    null, // No network_userid set
    null, // No geo-location for this IP address
    null,
    null,
    null,
    null,
    null,
    // Raw page URL is discarded 
    "Tarot cards - Psychic Bazaar",
    // Raw referer URL is discarded
    "http",
    "www.psychicbazaar.com",
    "80",
    "/2-tarot-cards/genre/all/type/all",
    "utm_source=google&utm_medium=cpc&utm_term=buy+tarot&utm_campaign=spring_sale",
    null,
    "http",
    "www.psychicbazaar.com",
    "80",
    "/2-tarot-cards/genre/all/type/all",
    "n=48",
    null,
    "internal", // Internal referer
    null,
    null,
    "cpc",
    "google",
    "buy tarot",
    null,
    "spring_sale",
    null, // Event fields empty
    null, //
    null, //
    null, //
    null, //
    null, // Transaction fields empty 
    null, //
    null, //
    null, //
    null, //
    null, //
    null, //
    null, //
    null, // Transaction item fields empty
    null, //
    null, //
    null, //
    null, //
    null, //
    "21",  // Page ping fields are set
    "214", //
    "251", //
    "517", //
    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.172 Safari/537.22",
    "Chrome",
    "Chrome",
    "25.0.1364.172", // Yech. We need to upgrade our UA library
    "Browser",
    "WEBKIT",
    "pt-BR",
    "1",
    "1",
    "1",
    "0",
    "0",
    "1",
    "0",
    "0",
    "1",
    "1",
    "32",
    "1366",
    "630",
    "Windows",
    "Windows",
    "Microsoft Corporation",
    "America/Sao_Paulo",
    "Computer",
    "0",
    "1366",
    "768",
    "UTF-8",
    "1349",
    "3787"
    )
}

/**
 * Integration test for the EtlJob:
 *
 * Check that all tuples in a page view in the
 * CloudFront format changed in August 2013
 * are successfully extracted.
 *
 * For details:
 * https://forums.aws.amazon.com/thread.jspa?threadID=134017&tstart=0#
 */
class Aug2013CfLineTest extends Specification with TupleConversions {

  "A job which processes a CloudFront file containing 1 valid page ping" should {
    EtlJobTest.
      source(MultipleTextLineFiles("inputFolder"), Aug2013CfLineTest.lines).
      sink[TupleEntry](Tsv("outputFolder")){ buf : Buffer[TupleEntry] =>
        "correctly output 1 page ping" in {
          buf.size must_== 1
          val actual = buf.head
          for (idx <- Aug2013CfLineTest.expected.indices) {
            if (idx != 6) { // We can't predict the event_id
              actual.getString(idx) must_== Aug2013CfLineTest.expected(idx)
            }
          }
        }
      }.
      sink[TupleEntry](Tsv("exceptionsFolder")){ trap =>
        "not trap any exceptions" in {
          trap must beEmpty
        }
      }.
      sink[String](JsonLine("badFolder")){ error =>
        "not write any bad rows" in {
          error must beEmpty
        }
      }.
      run.
      finish
  }
}