# Pebble RADAR-pRMT plugin

Application to be run on an Android 4.4 (or later) device with Bluetooth Low Energy (Bluetooth 4.0 or later), to interact with wearable devices. The app is cloned from the [Empatica sample app][2].

Currently the Empatica E4 and Pebble 2 are supported. Also note that the application only runs on an ARM architecture.

![Screenshot](/man/screen20161215_edited.png?raw=True "Screenshot 2016-12-15")

## Installation

To run this app with a Pebble device, the RADAR-CNS app must be installed on the Pebble 2. For now, [install the Pebble SDK](https://developer.pebble.com/sdk/install/) on your machine. Go to the `pebble/` directory. There we can use the [Pebble command line tools](https://developer.pebble.com/guides/tools-and-resources/pebble-tool/). First, build the app with
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

Make a pull request once the code is working.
