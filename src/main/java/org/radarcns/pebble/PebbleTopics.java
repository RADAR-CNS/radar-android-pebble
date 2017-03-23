/*
 * Copyright 2017 The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarcns.pebble;

import org.radarcns.android.device.DeviceTopics;
import org.radarcns.key.MeasurementKey;
import org.radarcns.topic.AvroTopic;

/** Topic manager for topics concerning the Empatica E4. */
public class PebbleTopics extends DeviceTopics {
    private final AvroTopic<MeasurementKey, Pebble2Acceleration> accelerationTopic;
    private final AvroTopic<MeasurementKey, Pebble2BatteryLevel> batteryLevelTopic;
    private final AvroTopic<MeasurementKey, Pebble2HeartRate> heartRateTopic;
    private final AvroTopic<MeasurementKey, Pebble2HeartRateFiltered> heartRateFilteredTopic;

    private static final Object syncObject = new Object();
    private static PebbleTopics instance = null;

    public static PebbleTopics getInstance() {
        synchronized (syncObject) {
            if (instance == null) {
                instance = new PebbleTopics();
            }
            return instance;
        }
    }

    private PebbleTopics() {
        accelerationTopic = createTopic("android_pebble2_acceleration",
                Pebble2Acceleration.getClassSchema(),
                Pebble2Acceleration.class);
        batteryLevelTopic = createTopic("android_pebble2_battery_level",
                Pebble2BatteryLevel.getClassSchema(),
                Pebble2BatteryLevel.class);
        heartRateTopic = createTopic("android_pebble2_heart_rate",
                Pebble2HeartRate.getClassSchema(),
                Pebble2HeartRate.class);
        heartRateFilteredTopic = createTopic("android_pebble2_heart_rate_filtered",
                Pebble2HeartRateFiltered.getClassSchema(),
                Pebble2HeartRateFiltered.class);
    }

    public AvroTopic<MeasurementKey, Pebble2Acceleration> getAccelerationTopic() {
        return accelerationTopic;
    }

    public AvroTopic<MeasurementKey, Pebble2BatteryLevel> getBatteryLevelTopic() {
        return batteryLevelTopic;
    }

    public AvroTopic<MeasurementKey, Pebble2HeartRate> getHeartRateTopic() {
        return heartRateTopic;
    }

    public AvroTopic<MeasurementKey, Pebble2HeartRateFiltered> getHeartRateFilteredTopic() {
        return heartRateFilteredTopic;
    }
}
