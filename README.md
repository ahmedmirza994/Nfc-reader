# NFC Reader

![](https://advicesacademy.com/wp-content/uploads/2015/02/NFC-on-Android.png)

A new flutter plugin to help developers looking to use internal hardware inside Android devices for reading NFC tags.

## Supported NFC Format

| Platform | Supported NFC Tags |
| --- | --- |
| Android | NDEF |

```dart
import 'package:nfc_reader/nfc_reader.dart';
```

## How to use

### Android setup

Add those two lines to your `AndroidManifest.xml` on the top

```xml
<uses-permission android:name="android.permission.NFC" />
<uses-feature
        android:name="android.hardware.nfc"
        android:required="true" />
```


For better details look at the demo app.

## Getting Started

For help getting started with Flutter, view our online
[documentation](https://flutter.io/).

For help on editing plugin code, view the [documentation](https://flutter.io/developing-packages/#edit-plugin-package).
