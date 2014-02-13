# Android BLEKit Library

An Android library for BLEKit developers to ease interaction with Beacons. For a general, non-android specific BLEKit overview please consult the [BLEKit project](https://github.com/upnext/blekit).

---
## What does this library do?

It allows pre-configured interaction with Beacons. 

Configuration is provided in JSON file format (described later in this document) - either directly or as a link to an existing JSON on the web.

The basic objects presented in the cofiguration are:

* BLEAction - represents an action to be executed, eg. show a Dialog with a given message, or perform a check-in to Facebook

* BLECondition - represents a condition that has to be met in order for the BLEAction to be executed, eg. 'enter' (beacon has appeared), 'cameImmediate' (beacon became very close to the device)


It is possible to write your own custom BLEActions and BLEConditions and provide them to the BLEKit instance.

##How to use this library

The easiest and preferable method is to use the binary version (as an AAR library) and place this in your project.

###Permissions required by library
The library requires the following permissions in order to function properly:

* `android.permission.INTERNET` - required to fetch configuration from a given URL
* `android.permission.BLUETOOTH` - required to perfom Bluetooth LE scans
* `android.permission.BLUETOOTH_ADMIN` - required to check if Bluetooth is turned on
* `android.permission.RECEIVE_BOOT_COMPLETED` - required to respawn the BLEKit service on device reboot

###Android Studio/Gradle
* Download the latest binary version [AAR file](https://github.com/upnext/blekit-android/releases/download/v0.5.0/AndroidBLEKitLibrary-0.5.0.aar)
* Create a /libs directory inside your project and copy the AAR file there.
* Edit your build.gradle file, and add a "flatDir" entry to your repositories:

```
repositories {
 mavenCentral()
 flatDir {
   dirs 'libs'
 }
}
```
* Edit your build.gradle file to add AAR library file as a dependency:

```
dependencies {
 compile 'com.upnext.blekit:AndroidBLEKitLibrary:0.x.x@aar'
}
```


##Usage

###Basic setup
* Initiate object

````
BLEKit.create(context)
````

* provide configuration file (URL or direct JSON body)

````
.setJsonUrl("http://my.server.com/config.json")
//or
.setJsonContent(jsonContent) //jsonContent is a String
````

* start locating beacons

````
.start(context)
````


###Advanced Setup
The following is a more advanced example that makes use of extra features provided by the library.

````
BLEKit.create(this)
      .setJsonUrl("http://my.server.url/config.json")
      .setTargetActivityForNotifications(DashboardAcivity.class)
      .setEventListener(this)
      .setZoneUpdateListener(this)
      .setActionListener(this)
      .removeActionByType(FacebookCheckinAction.TYPE)
      .addAction(new MyCustomAlertAction())
      .addCondition(new MyCustomCondition())
      .setStateListener(this)
      .start(this);
````



##Dependecies
This library is dependant on [Radius Networks's Android IBeacon Library](https://github.com/RadiusNetworks/android-ibeacon-service	).

## Problems
Android support for BLE is not perfect. When listening for BLE devices (eg. iBeacon) we are constantly getting RSSI values of the signal.
Unfortunately even when the phone does not move and neither does the beacon, received values fluctuate a lot which distorts proximity approximations.
The thing is even worse when the phone is being rotated around itself (even when not changing distance from the beacon).
Moreover different phones (Nexus 5, SGS4) present different RSSI values when in the same distance from the beacon.

Because of those issues there is an apprximation made in the library - proximity is an average value of 3 most recently received.

##License 

This software is available under the MIT license, allowing you to use the library in your applications.

If you would like to contribute to the open source project, contact blekit@up-next.com

*Copyright (c) 2014 UP-NEXT. All rights reserved.
http://www.up-next.com*

*Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:
The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.*
