# Android BLEKit Library

An Android library for BLEKit developers to ease the interaction with Beacons.

---
## What does this library do?

It allows pre-configured interaction with Beacons. 

Configuration is provided in JSON file format (described later on) - either directly or as a link to an existing JSON on the web.

Basic objects present in cofiguration are:

* BLEAction - represents an action to be executed, eg. show a Dialog with given message or perform a check-in to Facebook

* BLECondition - represents a condition that has to be met in order for the BLEAction to be executed, eg. 'enter' (beacon has appeared), 'cameImmediate' (beacon became very close to the device)


It is possible to write your own custom BLEActions and BLEConditions and provide them to the BLEKit instance.

##How to use this library

The easiest and most desirable way is to use the bianry version (as a AAR library) and put in your project.

###Permissions required by library
The library requires following permissions in order to function properly:

* `android.permission.INTERNET` - required to fetch configuration from given URL
* `android.permission.BLUETOOTH` - required to perfom Bluetooth LE scans
* `android.permission.BLUETOOTH_ADMIN` - required to check if Bluetooth is turned on

###Android Studio/Gradle
* Download latest binary version [AAR file](htt://link.to.aar) 
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


##Setup - basic
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


##Setup - advanced
Below is a more advanced example using extra features provided by the library.

````
BLEKit.create(this)
      .setJsonUrl("http://my.server.url/config.json")
      .setTargetActivityForNotifications(DashboardAcivity.class)
      .setScanPeriods(new BLEKitScanPeriods(1100, 2000, 1100, 10000))
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
Describle problems with Android BLE RSSI.

##License 

This software is available under the MIT license, allowing you to use the library in your applications.

If you want to help with the open source project, contact blekit@up-next.com

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
