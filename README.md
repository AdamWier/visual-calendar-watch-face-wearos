Visual Schedule Watch Face
===============================

Project description
--------------------------------
This is a watch face to connect to the user's google calendar, display the current event and progress bar. It will also periodically update them on the event progress.

Steps to build in Android Studio
--------------------------------
Because a watch face only contains a service, that is, there is no Activity, you first need to turn
off the launch setting that opens an Activity on your device.

To do that (and once the project is open) go to Run -> Edit Configurations. Select the **app**
module and the **General** tab. In the Launch Options, change **Launch:** to **Nothing**. This will
allow you to successfully install the watch face on the Wear device.

When installed, you will need to select the watch face in the watch face picker, i.e., the watch
face will not launch on its own like a regular app.
