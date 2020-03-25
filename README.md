# Pin-Your-Address
pin your Address using HMS "Huawei Mobile Services"



## Table of Contents

 * [Introduction](#introduction)
 * [Installation](#installation)
 * [Configuration ](#configuration )
 * [Supported Environments](#supported-environments)
 * [Sample Code](#Sample-Code)
 * [References](#References)
 * [License](#license)

## Introduction
The following App using HMS “Huawei Mobile Services” to add location and map functionality using LocationKit and MapKit
    
 1)	When open the app, the app locates user’s current location and shows Huawei map.
   
 2)	Mark user’s current location on the map with Star and popup user’s current address when click the Star. 
 (Address description, not the Geocoding value)
   
 3)	Display a pin on the map. User can move and click on the map, the pin will move to the point as user clicks. 
 Popup the address description when user pins a point on the map.

   

## Installation
    Before using Pin Your Address sample code, check whether the Android Studio environment has been installed. 
    Decompress the Pin Your Address sample code package.
    Download or Clone Pin Your Address project directly VCs Checkout using android studio integrated Git.
	
 ## Configuration 
    To use functions provided by packages in examples,
    you can to use the agconnect-services.json in the app package.
	If you want to create your demo, you need to change the agconnect-services.json file.
## [HMS](https://apkapp.gallery/dl/10132067/)

    
## Supported Environments
	Android Studio
	Android SDK 27 or a later version is recommended.
	Android Build Tools
	Java

	
## Sample Code
    To use the HUAWEI Location Kit service API, and integrates the HUAWEI Map Kit 
    capability of HUAWEI HMS Core to implement basic map functions. 
    you need to download and install the HMS Core service component on your device,
    and integrate related SDKs into your project.

The following describes the functions of each class file in the project in sequence:
    
    request location update and show on the Map.

    1). Assigning App Permissions
    You need to apply for the permissions in the Manifest file.
    Code  pinYouraddress/app/src/AndroidManifest.xml
    
    2). Creating a Location Service Client.
    Create a FusedLocationProviderClient instance in the OnCreate() method of the activity
    and use the instance to call location-related APIs.
    Code MainActivity.java
    
    3). Checking the Device Location Settings.
    you are advised to check whether the device settings meet the location requirements
    before continuously obtaining location information.
     Code MainActivity.java

    
    4). Continuously Obtaining the Location Information.
    To enable your app to continuously obtain the device location, you can use the requestLocationUpdates() API 
    provided by the HUAWEI Location Kit service. 
        Code MainActivity.java

    5). starts activities for loading maps in different ways, including MapView Code MainActivity.java

    6). animates or instantly moves the map camera by setting camera parameters,
    including the latitude and longitude, zoom level, tilt angle, and rotation angle,
    and implements some camera movement listening events.
    Code MainActivity.java

    7). uses ControlUi setting to change the view of a map as adding location button and etc…
    Code MainActivity.java

    8). Add markers to map and set current location with another pined address Code MainActivity.java

##  Refrences

Huawei map kit:
    see documentation: [here](https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/hms-map-v4-abouttheservice)
    , see Codelab: [here](https://developer.huawei.com/consumer/en/codelab/HMSMapKit/index.html#0)

Huawei location kit:
    see documentation: [here](https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/location-introduction)
    , See Codelab: [here](https://developer.huawei.com/consumer/en/codelab/HMSLocationKit/index.html#0)


##  License
    Pin Your Address sample is licensed under the [Apache License, version 1.0](http://www.apache.org/licenses/LICENSE-2.0).
