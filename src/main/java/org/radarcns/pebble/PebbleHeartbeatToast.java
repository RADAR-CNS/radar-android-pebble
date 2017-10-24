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

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import org.radarcns.android.device.DeviceServiceConnection;
import org.radarcns.android.util.Boast;
import org.radarcns.data.Record;
import org.radarcns.kafka.ObservationKey;
import org.radarcns.passive.pebble.Pebble2HeartRateFiltered;
import org.radarcns.topic.AvroTopic;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Shows recently collected heartbeats in a Toast.
 */
public class PebbleHeartbeatToast extends AsyncTask<Void, Void, String> {
    private final Context context;
    private final DeviceServiceConnection<PebbleDeviceStatus> connection;
    private static final DecimalFormat singleDecimal = new DecimalFormat("0.0");

    public PebbleHeartbeatToast(Context context, DeviceServiceConnection<PebbleDeviceStatus> connection) {
        this.context = context;
        this.connection = connection;
    }

    @Override
    protected final String doInBackground(Void... params) {
        AvroTopic<ObservationKey, Pebble2HeartRateFiltered> topic = PebbleDeviceManager.getHeartRateFilteredTopic();

        if (topic != null) {
            try {
                List<Record<ObservationKey, Pebble2HeartRateFiltered>> measurements = connection.getRecords(topic, 25);
                if (!measurements.isEmpty()) {
                    StringBuilder sb = new StringBuilder(3200); // <32 chars * 100 measurements
                    for (Record<ObservationKey, Pebble2HeartRateFiltered> measurement : measurements) {
                        long diffTimeMillis = System.currentTimeMillis() - (long) (1000d * measurement.value.getTimeReceived());
                        sb.append(singleDecimal.format(diffTimeMillis / 1000d));
                        sb.append(" sec. ago: ");
                        sb.append(singleDecimal.format(measurement.value.getHeartRate()));
                        sb.append(" bpm\n");
                    }
                    return sb.toString();
                }
            } catch (IOException ignore) {
            }
        }

        return "No heart rate collected yet.";
    }

    @Override
    protected void onPostExecute(String result) {
        Boast.makeText(context, result, Toast.LENGTH_LONG).show();
    }
}
