# oRing - Reminder
This app help you managing your contraceptive method (created for people using thermal contraception)

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="90">](https://f-droid.org/packages/com.lubenard.oring_reminder/)
[<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png"
     alt="Get it on Google Play"
     height="90">](https://play.google.com/store/apps/details?id=com.lubenard.oring_reminder)

## Screenshots
![unnamed](https://user-images.githubusercontent.com/42534397/146053444-0a66ec5b-3076-4f63-9954-0d8253f80a26.png)
![unnamed2](https://user-images.githubusercontent.com/42534397/146053451-280ad090-6258-4fba-b78c-a1ef26368afa.png)
![unnamed3](https://user-images.githubusercontent.com/42534397/133885040-bb269ef1-4292-4f0d-9a16-8e2c82e7b777.png)

## Features:
 - Can predict when you can take your protection off
 - Send a notification when you can take your protection off (with quick replies !)
 - Save your datas into a local database
 - Dark theme
 - Working with 'sessions'
 - Import / Export your datas and settings
 - Add as many pauses as you want in your sessions !
 - Available in 2 languages (FR and EN)
 - Completely open source (and always will be)
 
 ## Datas
Due to the sensitivity of the data, and the fact this app is open source,
no datas is collected. **Everything** remains on your phone.

## How to compile it ?

To compile an Android application, you will need android tools such as Gradle and java installed and configured.

Once installed, use the gradlew script.

For a release version:
```shell
./gradlew assembleRelease
```

For a debug version:
```shell
./gradlew assembleDebug
```

# Unit tests
You can run the unit tests using maestro (available here: https://maestro.mobile.dev/)
Once installed and emulator running, run:
```shell
maestro test maestro_tests/tests/
```


## Tech part
This app is coded only in Java and support all the way to Android Lollipop (Api 21)

Feel free to contribute or open a issue if needed.

## License

```
Copyright 2021 lubenard

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```

