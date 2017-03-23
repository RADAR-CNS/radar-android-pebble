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
#include "worker.h"
#include "data_handler.h"
#include "common.h"
#include <pebble_worker.h>

static WorkerStatus worker_status;

static void send_worker_status() {
  AppWorkerMessage msg = {
    .data0 = worker_status,
  };
  app_worker_send_message(WORKER_KEY_STATUS, &msg);
}

static void start() {
  if (worker_status == WORKER_STATUS_RUNNING) {
    return;
  }
  data_handler_start();
  worker_status = WORKER_STATUS_RUNNING;
  send_worker_status();
}

static void stop() {
  if (worker_status == WORKER_STATUS_DISABLED) {
    return;
  }
  data_handler_stop();
  worker_status = WORKER_STATUS_DISABLED;
  send_worker_status();
}

static void worker_message_handler(uint16_t type, AppWorkerMessage *data) {
  AppWorkerMessage msg, b_msg, hr_msg;

  switch (type) {
    case WORKER_KEY_START_LOGGING:
      persist_write_bool(PERSIST_KEY_IS_LOGGING, true);
      start();
      break;
    case WORKER_KEY_STOP_LOGGING:
      persist_write_bool(PERSIST_KEY_IS_LOGGING, false);
      stop();
      break;
    case WORKER_KEY_TOGGLE_LOGGING:
      if (worker_status == WORKER_STATUS_DISABLED) {
        persist_write_bool(PERSIST_KEY_IS_LOGGING, true);
        start();
      } else {
        persist_write_bool(PERSIST_KEY_IS_LOGGING, false);
        stop();
      }
      break;
    case WORKER_KEY_DEVICE_STATE:
      msg.data0 = device_state.x;
      msg.data1 = device_state.y;
      msg.data2 = device_state.z;
      app_worker_send_message(WORKER_KEY_DEVICE_STATE_ACCEL, &msg);
      hr_msg.data0 = device_state.heartRate;
      hr_msg.data1 = device_state.heartRateFiltered;
      app_worker_send_message(WORKER_KEY_DEVICE_STATE_HEART_RATE, &hr_msg);
      b_msg.data0 = device_state.battery_level;
      b_msg.data1 = device_state.battery_charging;
      b_msg.data2 = device_state.battery_plugged;
      app_worker_send_message(WORKER_KEY_DEVICE_STATE_BATTERY, &b_msg);
      break;
    case WORKER_KEY_STATUS:
      send_worker_status();
      break;
  }
}

static void init(void) {
  APP_LOG(APP_LOG_LEVEL_INFO, "AppWorker launched");

  worker_status = WORKER_STATUS_DISABLED;
  app_worker_message_subscribe(worker_message_handler);

  if (!persist_exists(PERSIST_KEY_IS_LOGGING)) {
    persist_write_bool(PERSIST_KEY_IS_LOGGING, true);
  }
  if (persist_read_bool(PERSIST_KEY_IS_LOGGING)) {
    start();
  }
}

static void deinit(void) {
  stop();
  APP_LOG(APP_LOG_LEVEL_INFO, "AppWorker quit");
}

int main(void) {
  init();
  worker_event_loop();
  deinit();
}
