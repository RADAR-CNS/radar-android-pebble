# DEPRECATION NOTICE

All plugin development has moved to https://github.com/RADAR-base/radar-commons-android in the plugins directory. Please view that directory for examples of a plugin.

# Pebble RADAR-pRMT plugin

Pebble plugin for the RADAR-AndroidApplication app, to be run on an Android 4.4 (or later) device with Bluetooth Low Energy (Bluetooth 4.0 or later), to interact with wearable devices.

## Installation

First, add the plugin code to your application:

```gradle
repositories {
    maven { url  'http://dl.bintray.com/radar-cns/org.radarcns' }
}

dependencies {
    compile 'org.radarcns:radar-android-pebble:0.1-alpha.1'
}
```

To run this app with a Pebble device, the RADAR-CNS app must be installed on the Pebble. For now, [install the Pebble SDK](https://developer.pebble.com/sdk/install/) on your machine. Go to the `pebble/` directory. There we can use the [Pebble command line tools](https://developer.pebble.com/guides/tools-and-resources/pebble-tool/). First, build the app with
```shell
pebble build
```
Then run the following sequence:

1. Pair the Pebble with the app on the endpoint.
2. Disable Bluetooth on the endpoint.
3. Enable Bluetooth on your phone.
4. Pair the Pebble with the Pebble app on your phone.
5. Open the developer menu on the Pebble app on your phone and enable developer mode.
6. Install the app with `pebble install --phone 1.2.3.4` with the IP address stated in the Pebble app on your phone.
7. Disable Bluetooth on your phone. If desired, remove the pairing with your phone and the Pebble 2 device.
8. Enable Bluetooth on the endpoint.

The RADAR-CNS Pebble app will now send data to the endpoint.

## Contributing

Code should be formatted using the [Google Java Code Style Guide](https://google.github.io/styleguide/javaguide.html), except using 4 spaces as indentation. Make a pull request once the code is working.
