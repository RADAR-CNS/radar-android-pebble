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
#include <stdio.h>
#include "main.h"
#include "worker_listener.h"
#include "../common.h"
#include "../windows/main_window.h"

static void init(void) {
  app_worker_launch();
  worker_listener_init();
  main_window_push();
}

static void deinit(void) {
  worker_listener_deinit();
}

int main(void) {
  init();
  app_event_loop();
  deinit();
}
