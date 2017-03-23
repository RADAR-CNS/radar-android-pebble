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

#include "common.h"

#define ACCELERATION_LOG 11
#define HEART_RATE_LOG 12
#define HEART_RATE_FILTERED_LOG 13
#define BATTERY_LEVEL_LOG 14

// Pebble2 data logging is apparently limited to 256 bytes. That means we can pack 18 acceleration
// messages in a single batch: 14*18 = 252
#define ACCELERATION_SIZE 14
#define ACCELERATION_BATCH 18
#define BATTERY_LEVEL_SIZE 11
#define HEART_RATE_SIZE 12

extern DeviceState device_state;

void data_handler_start();
void data_handler_stop();
