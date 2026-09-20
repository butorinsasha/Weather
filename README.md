# Модификация APK для отключения SSL Pinning

Цель эксперимента: взять уже собранный APK, декомпилировать его, найти реализацию SSL Pinning, заменить `CertificatePinner` на пустой и добавить доверие к пользовательскому CA, после чего пересобрать, подписать и установить модифицированный APK.

> Эксперимент выполняется на тестовом APK `Weather`.

---

## 1. Собрать APK

### Release

```bash
# Собрать release APK
./gradlew clean assembleRelease
```

### Debug

```bash
# Собрать debug APK
./gradlew clean assembleDebug
```

---

## 2. Подписать release APK

Если release APK уже подписывается автоматически, этот шаг можно пропустить.

```bash
# Подписать release APK
~/Library/Android/sdk/build-tools/36.0.0/apksigner sign \
  --ks /Users/pushkinaleksandr/Code/AndroidStudioProjects/Weather/weather-release.jks \
  --ks-key-alias weather \
  --out /Users/pushkinaleksandr/Code/AndroidStudioProjects/Weather/app/build/outputs/apk/release/app-release-signed.apk \
  ./app/build/outputs/apk/release/app-release-unsigned.apk
```

---

## 3. Установить APK

```bash
# Установить оригинальный APK на эмулятор
adb -s emulator-5554 install \
  ./app/build/outputs/apk/release/app-release-signed.apk
```

---

## 4. Проверить запущенную Activity

```bash
# Проверить, какая Activity сейчас находится на переднем плане
adb shell dumpsys activity activities | grep -E "ResumedActivity"
```

Ожидаемый результат:

```text
topResumedActivity=ActivityRecord{... u0 com.example.weather/.WeatherActivity ...}
ResumedActivity: ActivityRecord{... u0 com.example.weather/.WeatherActivity ...}
```

---

## 5. Найти установленный APK

```bash
# Узнать путь к APK установленного приложения
adb shell pm path com.example.weather
```

Например:

```text
package:/data/app/.../base.apk
```

---

## 6. Вытащить APK с устройства

```bash
# Скачать установленный APK с эмулятора
adb pull \
/data/app/.../base.apk \
./weather-from-device.apk
```

В дальнейшем работаем именно с APK, который был получен с устройства.

---

## 7. Установить JADX

```bash
# Установить JADX
brew install jadx
```

Проверить:

```bash
# Проверить, что JADX установлен
jadx --version
```

---

## 8. Декомпилировать APK через JADX

```bash
# Декомпилировать APK в Java/Kotlin-подобный исходный код
jadx \
  -d ~/Code/AndroidStudioProjects/Weather/jadx-output \
  ~/Code/AndroidStudioProjects/Weather/output-0/weather-from-device/weather-from-device.apk \
  2>&1 | tee ~/Code/AndroidStudioProjects/Weather/jadx-errors.txt
```

JADX нужен прежде всего для понимания структуры приложения и поиска нужных классов.

---

## 9. Найти соответствие в mapping.txt

R8 сохраняет соответствие между исходными и обфусцированными именами в `mapping.txt`.

```bash
# Найти соответствие OkHttpClientProvider -> обфусцированный класс
grep -A 4 \
"OkHttpClientProvider" \
~/Code/AndroidStudioProjects/Weather/app/build/outputs/mapping/release/mapping.txt
```

Например:

```text
com.example.weather.api.yandex.OkHttpClientProvider -> b1.a:
```

Таким образом:

```text
OkHttpClientProvider
        ↓ R8
      b1.a
```

---

## 10. Найти OkHttpClientProvider

APKAnalyzer позволяет посмотреть классы и методы непосредственно внутри DEX.

```bash
# Найти OkHttpClientProvider в debug APK
~/Library/Android/sdk/cmdline-tools/latest/bin/apkanalyzer dex packages \
  ./app/build/outputs/apk/debug/app-debug.apk \
  | grep "OkHttpClientProvider"
```

Можно увидеть:

```text
C d 4	4	328	com.example.weather.api.yandex.OkHttpClientProvider
M d 1	1	128	com.example.weather.api.yandex.OkHttpClientProvider <clinit>()
M d 1	1	38	com.example.weather.api.yandex.OkHttpClientProvider <init>()
M d 1	1	34	com.example.weather.api.yandex.OkHttpClientProvider okhttp3.CertificatePinner getCertificatePinner()
M d 1	1	42	com.example.weather.api.yandex.OkHttpClientProvider okhttp3.OkHttpClient getClient()
F d 0	0	18	com.example.weather.api.yandex.OkHttpClientProvider com.example.weather.api.yandex.OkHttpClientProvider INSTANCE
F d 0	0	10	com.example.weather.api.yandex.OkHttpClientProvider okhttp3.CertificatePinner certificatePinner
F d 0	0	10	com.example.weather.api.yandex.OkHttpClientProvider okhttp3.OkHttpClient client
```

Это хороший кандидат для поиска SSL Pinning, потому что именно здесь создаётся `OkHttpClient`.

---

## 11. Найти обфусцированный класс в release APK

В release APK имена классов могут быть изменены R8/ProGuard.

Например:

```bash
# Найти обфусцированный класс, соответствующий OkHttpClientProvider
~/Library/Android/sdk/cmdline-tools/latest/bin/apkanalyzer dex packages \
  ./app/build/outputs/apk/release/app-release-unsigned.apk \
  | grep "b1.a"
```

Результат:

```text
M d 1	1	40	h.b1 android.os.LocaleList a(java.lang.String)
M d 1	1	40	e0.b1 android.view.ViewPropertyAnimator a(android.view.ViewPropertyAnimator,android.animation.ValueAnimator$AnimatorUpdateListener)
C d 1	1	187	b1.a
M d 1	1	136	b1.a <clinit>()
F d 0	0	11	b1.a b3.y a
```


```text
com.example.weather.api.yandex.OkHttpClientProvider
```

---

## 12. Посмотреть обфусцированный класс через APKAnalyzer

```bash
# Посмотреть код обфусцированного класса b1.a
~/Library/Android/sdk/cmdline-tools/latest/bin/apkanalyzer dex code \
  --class b1.a \
  ~/Code/AndroidStudioProjects/Weather/output-0/weather-from-device/weather-from-device.apk
```

Здесь можно увидеть создание `CertificatePinner` и `OkHttpClient`.

---



---

# Apktool

## 13. Установить Apktool

```bash
# Установить Apktool
brew install apktool
```

Проверить:

```bash
# Проверить установленную версию Apktool
apktool --version
```

---

## 14. Декомпилировать APK через Apktool

```bash
# Распаковать APK и декомпилировать DEX в Smali
apktool d \
  ~/Code/AndroidStudioProjects/Weather/output-0/weather-from-device/weather-from-device.apk \
  -o ~/Code/AndroidStudioProjects/Weather/apktool-output
```

После этого появится каталог:

```text
apktool-output/
├── AndroidManifest.xml
├── res/
├── smali/
├── assets/
└── ...
```

В отличие от JADX, Apktool позволяет изменить Smali и затем собрать APK обратно.

---

## 15. Найти SSL Pinning

Ищем строки с SHA-256 pin:

```bash
# Найти сертификатные pin'ы в Smali
grep -R -n "sha256/" \
~/Code/AndroidStudioProjects/Weather/apktool-output
```

Например:

```text
smali/b1/a.smali:24:
const-string v1, "sha256/EvDTBD56knrdNlvXhXf7oTrML7CkIX/HVw9yBsq6OLM="
```

Это и есть публичный ключ сертификата в формате pin:

```text
sha256/EvDTBD56knrdNlvXhXf7oTrML7CkIX/HVw9yBsq6OLM=
```

---

# Изменение CertificatePinner

## 16. Заменить CertificatePinner на пустой

Открыть файл:

```bash
# Открыть класс, создающий OkHttpClient
nano ~/Code/AndroidStudioProjects/Weather/apktool-output/smali/b1/a.smali
```

В оригинальном коде создаётся `CertificatePinner` с pin:

```smali
const-string v1, "sha256/EvDTBD56knrdNlvXhXf7oTrML7CkIX/HVw9yBsq6OLM="

filled-new-array {v1}, [Ljava/lang/String;
move-result-object v1

const/4 v2, 0x0
aget-object v1, v1, v2

new-instance v2, Lb3/f;
invoke-direct {v2, v1}, Lb3/f;-><init>(Ljava/lang/String;)V

invoke-virtual {v0, v2}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

new-instance v1, Lb3/h;
invoke-static {v0}, Lj2/l;->j2(Ljava/util/ArrayList;)Ljava/util/Set;
move-result-object v0

const/4 v2, 0x0
invoke-direct {v1, v0, v2}, Lb3/h;-><init>(Ljava/util/Set;Li2/c;)V
```

После этого создаётся `OkHttpClient.Builder`.

Важная строка:

```smali
iput-object v1, v0, Lb3/x;->q:Lb3/h;
```

Она означает примерно следующее:

```kotlin
builder.certificatePinner(pinner)
```

То есть созданный `CertificatePinner` с pin устанавливается в `OkHttpClient.Builder`.

### Оригинальная логика

Концептуально код выглядит так:

```kotlin
val pinner = CertificatePinner.Builder()
    .add("api.yandex.ru", "sha256/EvDTBD56knrdNlvXhXf7oTrML7CkIX/HVw9yBsq6OLM=")
    .build()

val builder = OkHttpClient.Builder()

builder.certificatePinner(pinner)
```

Нам не нужно удалять создание `pinner`.

Нужно только не устанавливать его в `builder`.

Вместо:

```smali
iput-object v1, v0, Lb3/x;->q:Lb3/h;
```

установить пустой `CertificatePinner`:

```smali
sget-object v2, Lb3/h;->c:Lb3/h;
iput-object v2, v0, Lb3/x;->q:Lb3/h;
```

Здесь:

```smali
Lb3/h;->c
```

— заранее созданный пустой `CertificatePinner`.

Концептуально это эквивалентно:

```kotlin
val emptyPinner = CertificatePinner.Builder()
    .build()

builder.certificatePinner(emptyPinner)
```

То есть вместо:

```kotlin
builder.certificatePinner(pinner)
```

получаем:

```kotlin
builder.certificatePinner(emptyPinner)
```

### Важно

Мы отключаем именно **Certificate Pinning**.

Обычная TLS-проверка сертификата при этом остаётся.

То есть:

```text
TLS
 │
 ├── проверка доверия сертификату
 │       └── TrustManager / CA
 │
 └── Certificate Pinning
         └── CertificatePinner
```

Мы убираем второй уровень проверки:

```text
CertificatePinner
```

но не отключаем TLS целиком.

---

# Network Security Config

## 17. Найти Network Security Config

```bash
# Найти конфигурацию сетевой безопасности
find \
~/Code/AndroidStudioProjects/Weather/apktool-output/res \
-name "network_security_config.xml"
```

Обычно файл находится здесь:

```text
res/xml/network_security_config.xml
```

---

## 18. Добавить доверие к пользовательским CA

Открыть конфигурацию:

```bash
# Открыть Network Security Config для редактирования
nano /Users/pushkinaleksandr/Code/AndroidStudioProjects/Weather/apktool-output/res/xml/network_security_config.xml
```

В `<base-config>` необходимо добавить:

```xml
<certificates src="user"/>
```

Итоговая конфигурация:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>

    <base-config>
        <trust-anchors>
            <certificates src="system" />
            <certificates src="user" />
        </trust-anchors>
    </base-config>

    <debug-overrides>
        <trust-anchors>
            <certificates src="user" />
        </trust-anchors>
    </debug-overrides>

</network-security-config>
```

### Что это даёт

```xml
<certificates src="system" />
```

означает:

```text
доверять системным CA
```

А:

```xml
<certificates src="user" />
```

означает:

```text
доверять CA, установленным пользователем
```

Это важно для Charles.

Когда Charles перехватывает HTTPS:

```text
Приложение
    ↓
Charles
    ↓
Yandex
```

Charles подменяет сертификат сервера своим сертификатом, подписанным Charles Root CA.

Поэтому приложение должно доверять Charles Root CA.

Здесь есть **две независимые проверки**:

```text
1. Network Security Config
   ↓
   Доверять Charles Root CA

2. CertificatePinner
   ↓
   Не требовать конкретный публичный ключ сервера
```

Если первая проверка не пройдена — приложение не доверяет сертификату Charles.

Если первая пройдена, но остаётся `CertificatePinner` — приложение всё равно может отклонить соединение из-за несовпадения pin.


---

# Сборка модифицированного APK

## 19. Пересобрать APK

```bash
# Собрать модифицированный APK из Apktool-проекта
apktool b \
  ~/Code/AndroidStudioProjects/Weather/apktool-output \
  -o ~/Code/AndroidStudioProjects/Weather/weather-modified-unsigned.apk
```

Результат:

```text
weather-modified-unsigned.apk
```

Это APK пока не подписан.

---

## 20. Выполнить zipalign (ytеобязательно для обхода SSL-Pinning)

APK перед подписью необходимо выровнять.

```bash
# Выполнить zipalign перед подписью
~/Library/Android/sdk/build-tools/36.0.0/zipalign \
  -p -f 4 \
  ~/Code/AndroidStudioProjects/Weather/weather-modified-unsigned.apk \
  ~/Code/AndroidStudioProjects/Weather/weather-modified-aligned.apk
```

Результат:

```text
weather-modified-aligned.apk
```

---

# Подпись модифицированного APK

## 21. Создать тестовый keystore

Модифицированный APK нельзя подписать оригинальной подписью разработчика, если приватного ключа от оригинального сертификата нет.

Создадим собственный тестовый сертификат.

```bash
# Создать тестовый keystore
keytool -genkeypair \
  -v \
  -keystore ~/Code/AndroidStudioProjects/Weather/weather-fake.jks \
  -alias weather-fake \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Например:

```text
CN=Nikolay Gogol
OU=Gogol Weather
O=Gogol Org
L=Moscow
ST=Russian Federation
C=RU
```

Получаем:

```text
weather-fake.jks
```

---

## 22. Подписать модифицированный APK

```bash
# Подписать модифицированный APK тестовым сертификатом
~/Library/Android/sdk/build-tools/36.0.0/apksigner sign \
  --ks ~/Code/AndroidStudioProjects/Weather/weather-fake.jks \
  --ks-key-alias weather-fake \
  --out ~/Code/AndroidStudioProjects/Weather/weather-modified-fake-signed.apk \
  ~/Code/AndroidStudioProjects/Weather/weather-modified-aligned.apk
```

Получаем:

```text
weather-modified-fake-signed.apk
```

---

## 23. Проверить подпись APK

```bash
# Проверить подпись модифицированного APK
~/Library/Android/sdk/build-tools/36.0.0/apksigner verify \
  --verbose \
  ~/Code/AndroidStudioProjects/Weather/weather-modified-fake-signed.apk
```

В результате не должно быть ошибок проверки подписи.

---

# Установка модифицированного APK

## 23. Удалить оригинальное приложение

Если APK подписан другим сертификатом, установить его поверх оригинального приложения нельзя.

Обычно появляется:

```text
INSTALL_FAILED_UPDATE_INCOMPATIBLE
```

Поэтому сначала удаляем оригинальное приложение:

```bash
# Удалить оригинальное приложение
adb uninstall com.example.weather
```

> При удалении приложения очищаются его данные.

---

## 24. Установить модифицированный APK

```bash
# Установить модифицированный APK
adb install \
  ~/Code/AndroidStudioProjects/Weather/weather-modified-fake-signed.apk
```

Проверить:

```bash
# Проверить, что приложение установлено
adb shell pm list packages | grep com.example.weather
```

---

# Проверка SSL Pinning через Charles

После установки модифицированного APK:

1. Запустить Charles.
2. Установить Charles Root Certificate на эмулятор.
3. Настроить proxy на эмуляторе.
4. Запустить приложение.
5. Выполнить запрос к API погоды.
6. Посмотреть HTTPS-запрос в Charles.

Если всё сделано правильно, Charles должен увидеть HTTPS-трафик приложения.

Схематично:

```text
                ┌──────────────────────┐
                │       Weather        │
                │                      │
                │    OkHttpClient      │
                │          │           │
                │          ▼           │
                │  empty Certificate   │
                │      Pinner          │
                └──────────┬───────────┘
                           │
                           │ HTTPS
                           ▼
                    ┌─────────────┐
                    │   Charles   │
                    │             │
                    │ Charles CA  │
                    └──────┬──────┘
                           │
                           │ HTTPS
                           ▼
                    ┌─────────────┐
                    │ Yandex API  │
                    └─────────────┘
```

---

# Если Charles всё ещё не видит HTTPS

Если после удаления `CertificatePinner` соединение всё равно не проходит, нужно проверить, нет ли другого механизма TLS-защиты.

## 28. Поиск других механизмов SSL Pinning

```bash
# Найти возможные альтернативные механизмы TLS-проверки
grep -R -n -E \
"CertificatePinner|TrustManager|X509TrustManager|HostnameVerifier|SSLSocketFactory|SSLContext" \
~/Code/AndroidStudioProjects/Weather/apktool-output/smali
```

Особенно интересны:

```text
CertificatePinner
X509TrustManager
TrustManager
HostnameVerifier
SSLSocketFactory
SSLContext
```

---

# Итоговая схема

После всех изменений приложение должно работать следующим образом:

```text
                HTTPS-запрос
Weather ─────────────────────────► Charles
   │                                  │
   │                                  │
   │ CertificatePinner                │ Charles Root CA
   │ отключён                         │
   │                                  │
   ▼                                  ▼
TrustManager ◄─────────────── доверяет пользовательскому CA
   │
   ▼
TLS-соединение
```

Изначально:

```text
HTTPS
  │
  ├── CA validation        ✓
  │
  └── Certificate Pinning  ✗
```

После модификации:

```text
HTTPS
  │
  ├── CA validation        ✓
  │
  └── Certificate Pinning  отключён
```

Таким образом, мы **не отключаем HTTPS и не отключаем TLS**.

Мы делаем две вещи:

```text
1. Разрешаем доверять пользовательскому CA
   └── Charles Root CA

2. Убираем Certificate Pinning
   └── пустой CertificatePinner
```

Именно поэтому Charles получает возможность выступить посредником между приложением и сервером и расшифровывать HTTPS-трафик для тестирования.