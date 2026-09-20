./gradlew clean assembleRelease
./gradlew clean assembleDebug


~/Library/Android/sdk/build-tools/36.0.0/apksigner sign \
--ks /Users/pushkinaleksandr/Code/AndroidStudioProjects/Weather/weather-release.jks \
--ks-key-alias weather \
--out /Users/pushkinaleksandr/Code/AndroidStudioProjects/Weather/app/build/outputs/apk/release/app-release-signed.apk \
./app/build/outputs/apk/release/app-release-unsigned.apk


adb -s emulator-5554 install \
  ./app/build/outputs/apk/release/app-release-signed.apk


adb shell dumpsys activity activities | grep -E "ResumedActivity"
    topResumedActivity=ActivityRecord{66b2408 u0 com.example.weather/.WeatherActivity t62}
    ResumedActivity: ActivityRecord{66b2408 u0 com.example.weather/.WeatherActivity t62}


adb shell pm path com.example.weather
    package:/data/app/~~tHh8LcISsRzsfRRv17S7ug==/com.example.weather-SaqnEN7mzryfruCdGE7aJg==/base.apk


adb pull \
/data/app/~~tHh8LcISsRzsfRRv17S7ug==/com.example.weather-SaqnEN7mzryfruCdGE7aJg==/base.apk \
./weather-from-device.apk


~/Library/Android/sdk/cmdline-tools/latest/bin/apkanalyzer dex packages \
./app/build/outputs/apk/debug/app-debug.apk \
| grep "OkHttpClientProvider"
C d 4	4	328	com.example.weather.api.yandex.OkHttpClientProvider
M d 1	1	128	com.example.weather.api.yandex.OkHttpClientProvider <clinit>()
M d 1	1	38	com.example.weather.api.yandex.OkHttpClientProvider <init>()
M d 1	1	34	com.example.weather.api.yandex.OkHttpClientProvider okhttp3.CertificatePinner getCertificatePinner()
M d 1	1	42	com.example.weather.api.yandex.OkHttpClientProvider okhttp3.OkHttpClient getClient()
F d 0	0	18	com.example.weather.api.yandex.OkHttpClientProvider com.example.weather.api.yandex.OkHttpClientProvider INSTANCE
F d 0	0	10	com.example.weather.api.yandex.OkHttpClientProvider okhttp3.CertificatePinner certificatePinner
F d 0	0	10	com.example.weather.api.yandex.OkHttpClientProvider okhttp3.OkHttpClient client


~/Library/Android/sdk/cmdline-tools/latest/bin/apkanalyzer dex packages \
./app/build/outputs/apk/release/app-release-unsigned.apk \
| grep "b1.a"
M d 1	1	40	h.b1 android.os.LocaleList a(java.lang.String)
M d 1	1	40	e0.b1 android.view.ViewPropertyAnimator a(android.view.ViewPropertyAnimator,android.animation.ValueAnimator$AnimatorUpdateListener)
C d 1	1	187	b1.a
M d 1	1	136	b1.a <clinit>()
F d 0	0	11	b1.a b3.y a


~/Library/Android/sdk/cmdline-tools/latest/bin/apkanalyzer dex packages \
./app/build/outputs/apk/debug/app-debug.apk \
| grep "OkHttpClientProvider"
C d 4	4	328	com.example.weather.api.yandex.OkHttpClientProvider
M d 1	1	128	com.example.weather.api.yandex.OkHttpClientProvider <clinit>()
M d 1	1	38	com.example.weather.api.yandex.OkHttpClientProvider <init>()
M d 1	1	34	com.example.weather.api.yandex.OkHttpClientProvider okhttp3.CertificatePinner getCertificatePinner()
M d 1	1	42	com.example.weather.api.yandex.OkHttpClientProvider okhttp3.OkHttpClient getClient()
F d 0	0	18	com.example.weather.api.yandex.OkHttpClientProvider com.example.weather.api.yandex.OkHttpClientProvider INSTANCE
F d 0	0	10	com.example.weather.api.yandex.OkHttpClientProvider okhttp3.CertificatePinner certificatePinner
F d 0	0	10	com.example.weather.api.yandex.OkHttpClientProvider okhttp3.OkHttpClient client


grep -A 4 \                                                                     
"OkHttpClientProvider" \                              
~/Code/AndroidStudioProjects/Weather/app/build/outputs/mapping/release/mapping.txt
188:189:okhttp3.OkHttpClient com.example.weather.api.yandex.OkHttpClientProvider.getClient():15:15 -> onLocationChanged
188:189:void com.example.weather.WeatherDetailActivity.loadWeather(double,double):141 -> onLocationChanged
188:189:void com.example.weather.WeatherDetailActivity.access$loadWeather(com.example.weather.WeatherDetailActivity,double,double):22 -> onLocationChanged
188:189:void onLocationChanged(android.location.Location):94 -> onLocationChanged
190:192:void com.example.weather.WeatherDetailActivity.loadWeather(double,double):141:141 -> onLocationChanged
--
com.example.weather.api.yandex.OkHttpClientProvider -> b1.a:
# {"id":"sourceFile","fileName":"OkHttpClientProvider.kt"}
    okhttp3.OkHttpClient client -> a
      # {"id":"com.android.tools.r8.residualsignature","signature":"Lb3/y;"}
    1:12:void okhttp3.CertificatePinner$Builder.<init>():327:327 -> <clinit>
    1:12:void <clinit>():8 -> <clinit>


~/Library/Android/sdk/cmdline-tools/latest/bin/apkanalyzer dex code \
--class b1.a \
~/Code/AndroidStudioProjects/Weather/output-0/weather-from-device/weather-from-device.apk


~/Library/Android/sdk/cmdline-tools/latest/bin/apkanalyzer dex code \
--class com.example.weather.api.yandex.OkHttpClientProvider \
~/Code/AndroidStudioProjects/Weather/app/build/outputs/apk/debug/app-debug.apk


brew install jadx


jadx \
-d ~/Code/AndroidStudioProjects/Weather/jadx-output \
~/Code/AndroidStudioProjects/Weather/output-0/weather-from-device/weather-from-device.apk \
2>&1 | tee /Users/pushkinaleksandr/Code/AndroidStudioProjects/Weather/jadx-errors.txt


brew install apktool


apktool d \
~/Code/AndroidStudioProjects/Weather/output-0/weather-from-device/weather-from-device.apk \
-o ~/Code/AndroidStudioProjects/Weather/apktool-output


grep -R -n "sha256/" \
~/Code/AndroidStudioProjects/Weather/apktool-output
~/Code/AndroidStudioProjects/Weather/apktool-output/smali/b2/e.smali:5023:    const-string v1, "sha256/"
~/Code/AndroidStudioProjects/Weather/apktool-output/smali/b3/f.smali:239:    const-string v0, "sha256/"
~/Code/AndroidStudioProjects/Weather/apktool-output/smali/b3/f.smali:329:    const-string v1, "pins must start with \'sha256/\' or \'sha1/\': "
~/Code/AndroidStudioProjects/Weather/apktool-output/smali/b1/a.smali:24:    const-string v1, "sha256/EvDTBD56knrdNlvXhXf7oTrML7CkIX/HVw9yBsq6OLM="


???????????????????????????????????????????????????????????????
a.smali
    iput-object v1, v0, Lb3/x;->q:Lb3/h;
->
    sget-object v2, Lb3/h;->c:Lb3/h;
    iput-object v2, v0, Lb3/x;->q:Lb3/h;
???????????????????????????????????????????????????????????????


find /Users/pushkinaleksandr/Code/AndroidStudioProjects/Weather/apktool-output \
-iname "*network*security*" \
-o -name "AndroidManifest.xml"
~/Code/AndroidStudioProjects/Weather/apktool-output/res/xml/network_security_config.xml
~/Code/AndroidStudioProjects/Weather/apktool-output/original/AndroidManifest.xml
~/Code/AndroidStudioProjects/Weather/apktool-output/AndroidManifest.xml


nano /Users/pushkinaleksandr/Code/AndroidStudioProjects/Weather/apktool-output/res/xml/network_security_config.xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config>
        <trust-anchors>
            <certificates src="system" />
->          <certificates src="user"/> 
        </trust-anchors>
    </base-config>
    <debug-overrides>
        <trust-anchors>
            <certificates src="user" />
        </trust-anchors>
    </debug-overrides>
</network-security-config>


apktool b \
~/Code/AndroidStudioProjects/Weather/apktool-output \
-o ~/Code/AndroidStudioProjects/Weather/weather-modified-unsigned.apk
I: Using Apktool 3.0.3 on weather-from-device.apk with 8 threads
I: smali has not changed.
I: AndroidManifest.xml and resources have not changed.
I: Building apk file...
I: Importing assets...
I: Importing unknown files...
I: Built apk into: /Users/pushkinaleksandr/Code/AndroidStudioProjects/Weather/weather-modified-unsigned.apk


~/Library/Android/sdk/build-tools/36.0.0/zipalign \
-p -f 4 \
~/Code/AndroidStudioProjects/Weather/weather-modified-unsigned.apk \
~/Code/AndroidStudioProjects/Weather/weather-modified-aligned.apk


keytool -genkeypair \
-v \
-keystore ~/Code/AndroidStudioProjects/Weather/weather-fake.jks \      
-alias weather-fake \
-keyalg RSA \
-keysize 2048 \
-validity 10000


~/Library/Android/sdk/build-tools/36.0.0/apksigner sign \
--ks ~/Code/AndroidStudioProjects/Weather/weather-fake.jks \
--ks-key-alias weather-fake \
--out ~/Code/AndroidStudioProjects/Weather/weather-modified-fake-signed.apk \
~/Code/AndroidStudioProjects/Weather/weather-modified-aligned.apk



adb install weather-modified-fake-signed.apk
Performing Incremental Install
Serving...
All files should be loaded. Notifying the device.
Failure [INSTALL_FAILED_UPDATE_INCOMPATIBLE: Existing package com.example.weather signatures do not match newer version; ignoring!]
Performing Streamed Install
adb: failed to install weather-modified-fake-signed.apk: Failure [INSTALL_FAILED_UPDATE_INCOMPATIBLE: Existing package com.example.weather signatures do not match newer version; ignoring!]


adb uninstall com.example.weather


adb install weather-modified-fake-signed.apk
