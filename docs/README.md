---
title: How to build your own apk of this app ?
---

## Requirements: 
 - Android Studio
 - Java version > 8

## Steps to follow:
 - Launch Android Studio.
 - Click Build, then Generate Signed apk
 - Tick 'APK' and not 'Android App Bundle'
 - Follow the steps. If you do not have a signing key, it is highly recommended to create one.

## How to launch the unit tests

```
./gradlew tests
```

If all tests are failing, please ensure the java version is above 8.

If a newer java version is installed in a custom location, export JAVA_HOME

```
export JAVA_HOME=/path/to/newer/java
```

and relaunch the tests.

## How to (re)compile the javadoc:

```
javadoc -protected -splitindex -d ~/test_doc $(find app/src/main/java/com/lubenard/oring_reminder/ -name '*.java' -print) -bootclasspath ~/<Path_to>/Android/Sdk/platforms/android-29/android.jar -Xdoclint:none
```


 
