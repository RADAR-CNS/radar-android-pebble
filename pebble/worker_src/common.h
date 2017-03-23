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
#pragma once
#include <inttypes.h>

typedef enum worker_key {
  WORKER_KEY_START_LOGGING,
  WORKER_KEY_STOP_LOGGING,
  WORKER_KEY_TOGGLE_LOGGING,
  WORKER_KEY_STATUS,
  WORKER_KEY_DEVICE_STATE,
  WORKER_KEY_DEVICE_STATE_ACCEL,
  WORKER_KEY_DEVICE_STATE_BATTERY,
  WORKER_KEY_DEVICE_STATE_HEART_RATE,
} WorkerKey;

typedef enum worker_status {
  WORKER_STATUS_RUNNING,
  WORKER_STATUS_DISABLED,
} WorkerStatus;

typedef struct DeviceState {
  int16_t x, y, z;
  int32_t heartRate;
  int32_t heartRateFiltered;
  int8_t battery_level;
  int8_t battery_charging;
  int8_t battery_plugged;
} DeviceState;