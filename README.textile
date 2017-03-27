h1. STOP HERE.

*The Deacon Project, Deacon library and Deacon demo application are no longer maintained or supported. If you have reached this page in search of a push solution for Android, please refer to Google C2DM.*

h1. README

The Deacon Project aims to produce an open-source push notifications library for the Android platform. "Deacon" is a Java class library used by Android developers to receive Push notifications from a Meteor comet web server. "Deacon-Demo" (http://github.com/davidrea/Deacon-Demo/) is an Android app that is used for testing and demonstration of Deacon, and is also developed by members of the Deacon project.

For more information see:
http://www.deaconproject.org/
http://github.com/davidrea/Deacon
http://wiki.github.com/davidrea/Deacon/
http://www.meteorserver.org/

h1. WHAT'S HERE

src/ ....... Source tree, containing all Deacon source files

doc/ ....... Documentation, including design models and API reference

COPYING* ... License files

h1. ECLIPSE CONFIGURATION

To develop or test Deacon in Eclipse:

0) Create a new Eclipse project and add the Deacon source files to it
1) Install the Android SDK from http://developer.android.com/sdk/index.html
2) Run the Android executable (in the tools/ directory) to install an Android platform (preferably the latest, 2.1)
3) Right click on the Deacon Eclipse project and configure the build path
4) Click "Add External Jar..." and select the android.jar file from the appropriate Android platform directory
5) Click the "Add Library..." and add a JUnit library to the project.
6) Ensure the JUnit library has higher precedence than the android.jar file.

h1. SUPPORT

Quick start instructions are located in the "Overview" section of the API reference.

If you need help with Deacon, or would like to contribute to the project, please visit the project mailing list at http://www.deaconproject.org/mailing-list/
