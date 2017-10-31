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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import android.util.ArrayMap;
import com.getpebble.android.kit.PebbleKit;

import org.radarcns.android.device.AbstractDeviceManager;
import org.radarcns.android.device.DeviceStatusListener;
import org.radarcns.android.util.BundleSerialization;
import org.radarcns.kafka.ObservationKey;
import org.radarcns.passive.pebble.Pebble2Acceleration;
import org.radarcns.passive.pebble.Pebble2BatteryLevel;
import org.radarcns.passive.pebble.Pebble2HeartRate;
import org.radarcns.passive.pebble.Pebble2HeartRateFiltered;
import org.radarcns.topic.AvroTopic;
import org.radarcns.util.Serialization;
import org.radarcns.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static android.bluetooth.BluetoothProfile.GATT_SERVER;
import static com.getpebble.android.kit.Constants.INTENT_PEBBLE_CONNECTED;
import static com.getpebble.android.kit.Constants.INTENT_PEBBLE_DISCONNECTED;

/** Manages scanning for an Pebble wearable and connecting to it */
class PebbleDeviceManager extends AbstractDeviceManager<PebbleService, PebbleDeviceStatus> {
    private static final UUID APP_UUID = UUID.fromString("a3b06265-d50c-4205-8ee4-e4c12abca326");
    private static final int ACCELERATION_LOG = 11;
    private static final int HEART_RATE_LOG = 12;
    private static final int HEART_RATE_FILTERED_LOG = 13;
    private static final int BATTERY_LEVEL_LOG = 14;

    private static final Logger logger = LoggerFactory.getLogger(PebbleDeviceManager.class);
    private static final Pattern CONTAINS_PEBBLE_PATTERN =
            Strings.containsIgnoreCasePattern("pebble");

    private final BroadcastReceiver connectReceiver;
    private final BroadcastReceiver disconnectReceiver;
    private final PebbleKit.PebbleDataLogReceiver dataLogReceiver;

    private final AvroTopic<ObservationKey, Pebble2Acceleration> accelerationTopic =
            createTopic("android_pebble_2_acceleration", Pebble2Acceleration.class);
    private final AvroTopic<ObservationKey, Pebble2HeartRate> heartRateTopic =
            createTopic("android_pebble_2_heartrate", Pebble2HeartRate.class);
    private final AvroTopic<ObservationKey, Pebble2HeartRateFiltered> heartRateFilteredTopic =
            createTopic("android_pebble_2_heartrate_filtered", Pebble2HeartRateFiltered.class);
    private final AvroTopic<ObservationKey, Pebble2BatteryLevel> batteryTopic =
            createTopic("android_pebble_2_battery_level", Pebble2BatteryLevel.class);

    private Pattern[] acceptableIds;

    public PebbleDeviceManager(PebbleService service) {
        super(service);

        this.dataLogReceiver = new PebbleKit.PebbleDataLogReceiver(APP_UUID) {
            @Override
            public void receiveData(Context context, UUID logUuid, Long timestamp,
                                    Long tag, byte[] data) {
                updateDeviceId();
                synchronized (PebbleDeviceManager.this) {
                    if (getName() == null) {
                        logger.info("Device is not acceptable");
                        return;
                    }
                    if (getState().getStatus() != DeviceStatusListener.Status.CONNECTED) {
                        updateStatus(DeviceStatusListener.Status.CONNECTED);
                    }
                }
                double time = Serialization.bytesToLong(data, 0) / 1000d;
                double timeReceived = System.currentTimeMillis() / 1000d;
                PebbleDeviceStatus state = getState();
                try {
                    switch (tag.intValue()) {
                        case ACCELERATION_LOG:
                            for (int i = 0; i < data.length; ) {
                                long timeLong = Serialization.bytesToLong(data, i);
                                if (timeLong == 0L) {
                                    break;
                                }
                                i += 8;
                                time = timeLong / 1000d;
                                float x = Serialization.bytesToShort(data, i) / 1000f;
                                i += 2;
                                float y = Serialization.bytesToShort(data, i) / 1000f;
                                i += 2;
                                float z = Serialization.bytesToShort(data, i) / 1000f;
                                i += 2;
                                send(accelerationTopic, new Pebble2Acceleration(time, timeReceived, x, y, z));
                                state.setAcceleration(x, y, z);
                            }
                            break;
                        case HEART_RATE_LOG:
                            float heartRate = Serialization.bytesToInt(data, 8);
                            send(heartRateTopic, new Pebble2HeartRate(time, timeReceived, heartRate));
                            state.setHeartRate(heartRate);
                            break;
                        case HEART_RATE_FILTERED_LOG:
                            float heartRateFiltered = Serialization.bytesToInt(data, 8);
                            send(heartRateFilteredTopic, new Pebble2HeartRateFiltered(time, timeReceived, heartRateFiltered));
                            state.setHeartRateFiltered(heartRateFiltered);
                            break;
                        case BATTERY_LEVEL_LOG:
                            float batteryLevel = data[8] / 100f;
                            boolean isCharging = data[9] == 1;
                            boolean isPluggedIn = data[10] == 1;
                            trySend(batteryTopic, 0L, new Pebble2BatteryLevel(time, timeReceived, batteryLevel, isCharging, isPluggedIn));
                            state.setBatteryLevel(batteryLevel);
                            state.setBatteryIsCharging(isCharging);
                            state.setBatteryIsPlugged(isPluggedIn);
                            break;
                        default:
                            logger.warn("Log {} not recognized", tag.intValue());
                    }
                } catch (Exception ex) {
                    logger.error("Failed to add data to state {}", state, ex);
                }
            }

            @Override
            public void onFinishSession(Context context, UUID logUuid, Long timestamp,
                                        Long tag) {
                logger.info("Pebble 2 session finished {}", getName());
            }
        };
        this.connectReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(INTENT_PEBBLE_CONNECTED)) {
                    if (intent.hasExtra("address")) {
                        String address = intent.getStringExtra("address").toUpperCase();
                        String name;
                        BluetoothAdapter btAdaptor = BluetoothAdapter.getDefaultAdapter();
                        if (BluetoothAdapter.checkBluetoothAddress(address)) {
                            BluetoothDevice btDevice = btAdaptor.getRemoteDevice(address);
                            address = btDevice.getAddress();
                            name = btDevice.getName();
                        } else {
                            name = address;
                            logger.warn("Pebble device not registered with the BluetoothAdaptor; set to address {}", address);
                        }
                        synchronized (PebbleDeviceManager.this) {
                            if (deviceIsAcceptable(name, address)) {
                                setName(name);
                                Map<String, String> attributes = new ArrayMap<>(3);
                                attributes.put("macAddress", address);
                                attributes.put("name", name);
                                attributes.put("sdk", "com.getpebble:pebblekit:4.0.0");
                                getService().registerDevice(name, attributes);
                                logger.info("Pebble device {} with address {} connected", name, address);
                            } else {
                                logger.warn("Pebble device {} with address {} not an accepted ID", name, address);
                            }
                        }
                    }
                    logger.info("Pebble connected with intent {}", BundleSerialization.bundleToString(intent.getExtras()));
                    updateStatus(DeviceStatusListener.Status.CONNECTED);
                }
            }
        };
        this.disconnectReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(INTENT_PEBBLE_DISCONNECTED)) {
                    logger.info("Pebble disconnected with intent {}", BundleSerialization.bundleToString(intent.getExtras()));
                    updateStatus(DeviceStatusListener.Status.DISCONNECTED);
                }
            }
        };

        updateStatus(DeviceStatusListener.Status.DISCONNECTED);
    }

    private void updateDeviceId() {
        synchronized (this) {
            if (getName() != null) {
                return;
            }
        }

        BluetoothManager btManager = (BluetoothManager) getService().getSystemService(Context.BLUETOOTH_SERVICE);

        for (BluetoothDevice btDevice : btManager.getConnectedDevices(GATT_SERVER)) {
            String name = btDevice.getName();
            if (name == null) {
                continue;
            }
            if (CONTAINS_PEBBLE_PATTERN.matcher(name).find()) {
                synchronized (this) {

                    if (deviceIsAcceptable(name, btDevice.getAddress())) {
                        setName(name);
                        logger.info("Pebble device set to {} with address {}", name,
                                btDevice.getAddress());
                    }
                }
            }
        }
        logger.info("No connected pebble device found");
    }

    private synchronized boolean deviceIsAcceptable(String name, String address) {
        return (this.acceptableIds.length == 0
                || Strings.findAny(acceptableIds, name)
                || Strings.findAny(acceptableIds, address));
    }

    @Override
    public void start(@NonNull final Set<String> acceptableIds) {
        synchronized (this) {
            this.acceptableIds = Strings.containsPatterns(acceptableIds);
        }
        logger.info("Registering Pebble2 receivers");
        PebbleService context = getService();
        PebbleKit.registerDataLogReceiver(context, dataLogReceiver);
        PebbleKit.registerPebbleConnectedReceiver(context, connectReceiver);
        PebbleKit.registerPebbleDisconnectedReceiver(context, disconnectReceiver);
        updateStatus(DeviceStatusListener.Status.READY);
    }

    @Override
    public void close() throws IOException {
        if (this.isClosed()) {
            return;
        }
        super.close();
        PebbleService context = getService();
        context.unregisterReceiver(dataLogReceiver);
        context.unregisterReceiver(connectReceiver);
        context.unregisterReceiver(disconnectReceiver);
        updateStatus(DeviceStatusListener.Status.DISCONNECTED);
    }

    @Override
    protected void registerDeviceAtReady() {
        // register at connect instead
    }
}
