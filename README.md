# Smart Ring
Smart Ring - система для управления умным домом, которая состоит из 3-х частей:
- Носимое устройство
- Мобильное приложение
- Сервер

## Носимое устройство
##### Описание
Устройство представляет собой кольцо из Bluetooth модуля, кнопки, батареи и зарядки. При нажатии на кнопку, сигнал передается по Bluetooth на смартфон. Такой принцип позволяет включать устройство только при нажатии на кнопку, что снижает энергопотребление и позволяет перезаряжать девайс как можно реже.
##### Исходные файлы
- [Прошивка](https://github.com/Sc222/SmartRingApp/tree/master/device/examples/ble_peripheral/smart_ring_ble)
- [Схемотехника](https://github.com/Sc222/SmartRingApp/tree/master/schematics)

##### Установка
Для компиляции прошивки требуется установить [nRF5 SDK 15.0.0](https://www.nordicsemi.com/Software-and-tools/Software/nRF5-SDK/Download#infotabs) в папку [/device](https://github.com/Sc222/SmartRingApp/tree/master/device).
## Мобильное приложение
##### Описание
Мобильное приложение используется для настройки устройства, обработки нажатий на его кнопку и отправки данных на сервер.
Приложение состоит из 3 основных разделов:
- Раздел настройки устройства
- Раздел настройки команд
- Раздел настройки локаций

При считывании данных с устройства, приложение определяет текущую локацию пользователя и на основании полученных данных формирует событие, отправляемое на сервер.
##### Исходные файлы
- [Android](https://github.com/Sc222/SmartRingApp/tree/master/application)

##### Зависимости
- **Для интерфейса:**
  - 'com.google.android.gms:play-services-maps:16.1.0'
  - 'androidx.appcompat:appcompat:1.1.0'
  - 'androidx.transition:transition:1.3.1'
  - 'com.google.android.material:material:1.1.0'
  - 'androidx.constraintlayout:constraintlayout:1.1.3'
  - 'androidx.vectordrawable:vectordrawable:1.1.0'
  - 'androidx.navigation:navigation-fragment:2.2.2'
  - 'androidx.navigation:navigation-ui:2.2.2'
  - 'androidx.recyclerview:recyclerview:1.1.0'
- **Для работы с Bluetooth:**
  - 'no.nordicsemi.android.support.v18:scanner:1.4.3'
  - 'no.nordicsemi.android:ble-livedata:2.2.0'
  - 'no.nordicsemi.android:log:2.2.0'
- **Для работы с базой данных:**
  - 'androidx.room:room-runtime:2.2.5'
  - 'androidx.room:room-compiler:2.2.5'
- **Для http запросов:**
  - 'com.android.volley:volley:1.1.1'
- **Остальные:**
  - 'androidx.lifecycle:lifecycle-extensions:2.2.0'
  - 'androidx.preference:preference:1.1.1'
  - 'androidx.legacy:legacy-support-v4:1.0.0'

## Cервер
##### Описание
Сервер используется для интеграции устройства с какой-либо системой управления умным домом (на данный момент реализована интеграция со SmartThings).  
Сервер получает обновления состояния кнопки от мобильного приложения и отсылает их в систему SmartThings.
##### Исходные файлы
- [Сервер](https://github.com/Sc222/SmartRingApp/tree/master/server)

##### Установка
На данный момент система использует сервер, который запущен на [glitch](https://glitch.com/edit/#!/smart-ring-webhook?path=index.js%3A301%3A0)  
Для запуска сервера локально выполните:
```sh
$ cd server
$ npm install
$ node start
```
## Что не работает / требует улучшений
### Устройство
- На данный момент не реализованы двойные, тройные, длинные и другие нажатия, вместо этого отладочная плата просто обрабатывает нажатия на 4 разные кнопки.
### Мобильное приложение
- Раздел "устройства" не функционирует полностью, интерфейс реализован, но не передает настройки устройству
- Требуется выполнить рефакторинг сервиса и возможно его требуется разделить на 2 отдельных (Location и Ble Service)
- Требуется оптимизировать сканирование устройств и настроить фильтры, чтобы снизить энергопотребление
- Некоторые списки реализованы не через RecyclerAdapter'ы, нужно это исправить
### Сервер
 - Требуется добавить базу данных (или хотя бы текстовый файл) для хранения текущего состояния кнопки
 - На данный момент реализована поддержка одного устройства. Нужно сделать поддержку нескольких и придумать, как генерировать ```externalDeviceId``` на основании локации и комнаты, в которой будет работать устройство
