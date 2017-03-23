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
#include <pebble.h>
#include "worker_listener.h"

DeviceState device_state;
WorkerStatus worker_status;
static void (*handler)(WorkerKey);

static void worker_message_handler(uint16_t type, AppWorkerMessage *data) {
  switch (type) {
    case WORKER_KEY_STATUS:
      worker_status = data->data0;
      break;
    case WORKER_KEY_DEVICE_STATE_ACCEL:
      device_state.x = data->data0;
      device_state.y = data->data1;
      device_state.z = data->data2;
      break;
    case WORKER_KEY_DEVICE_STATE_BATTERY:
      device_state.battery_level = data->data0;
      device_state.battery_charging = data->data1;
      device_state.battery_plugged = data->data2;
      break;
    case WORKER_KEY_DEVICE_STATE_HEART_RATE:
      device_state.heartRate = data->data0;
      device_state.heartRateFiltered = data->data1;
      break;
  }
  if (handler) handler((WorkerKey)type);
}

void worker_listener_init() {
  app_worker_message_subscribe(worker_message_handler);
  handler = NULL;
}

void worker_listener_handler(void (*callback)(WorkerKey)) {
  handler = callback;
}

void worker_listener_deinit() {
  app_worker_message_unsubscribe();
  handler = NULL;
}
