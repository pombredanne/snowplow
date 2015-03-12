-- Copyright (c) 2013-2015 Snowplow Analytics Ltd. All rights reserved.
--
-- This program is licensed to you under the Apache License Version 2.0,
-- and you may not use this file except in compliance with the Apache License Version 2.0.
-- You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the Apache License Version 2.0 is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
--
-- Authors: Yali Sassoon, Christophe Bogaert
-- Copyright: Copyright (c) 2013-2015 Snowplow Analytics Ltd
-- License: Apache License Version 2.0

-- Move all rows processed in this batch to atomic.events

BEGIN;
  INSERT INTO atomic.events (
    SELECT
      *
    FROM snowplow_landing.events
    WHERE etl_tstamp IN (SELECT etl_tstamp FROM snowplow_intermediary.distinct_etl_tstamps)
  );

  DELETE FROM snowplow_landing.events
  WHERE etl_tstamp IN (SELECT etl_tstamp FROM snowplow_intermediary.distinct_etl_tstamps);
COMMIT;
