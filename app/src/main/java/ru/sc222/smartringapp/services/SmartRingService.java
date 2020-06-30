package ru.sc222.smartringapp.services;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import ru.sc222.smartringapp.ble.ButtonBleManager;
import ru.sc222.smartringapp.db.AppDatabase;
import ru.sc222.smartringapp.db.Location;
import ru.sc222.smartringapp.db.tasks.LocationsDbLoader;
import ru.sc222.smartringapp.utils.BluetoothUtils;
import ru.sc222.smartringapp.utils.LocationUtils;
import ru.sc222.smartringapp.utils.NotificationUtils;
import ru.sc222.smartringapp.viewmodels.SharedBluetoothViewModel;
import ru.sc222.smartringapp.viewmodels.SharedLocationViewModel;

//TODO REFACTOR SERVICE, SPLIT INTO CLASSES
public class SmartRingService extends Service {

    private static final String TAG_FOREGROUND_SERVICE = "FOREGROUND_SERVICE";
    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
    private static final int BUFFER_CLEAR_FREQUENCY = 10;
    private int bufferCount = 0;

    private boolean isScannerStarted = false;
    private SharedLocationViewModel sharedLocationViewModel;
    private List<Location> allLocations;


    //TODO USE VIEWMODEL
    //address and device
    private Map<String, BluetoothDevice> devices = new HashMap<>();
    private HashSet<String> devicesBuffer = new HashSet<>(); //to clear invisible devices

    private ButtonBleManager buttonBleManager;
    private String[] buttonStates = { //TODO fix
            "Обычное нажатие",
            "Двойное нажатие",
            "Длинное нажатие"
    };
    private SharedBluetoothViewModel sharedBluetoothViewModel;

    //for binding
    private final IBinder binder = new BleServiceBinder();

    public class BleServiceBinder extends Binder {
        public SmartRingService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SmartRingService.this;
        }
    }

    public SharedBluetoothViewModel getBluetoothViewModel() {
        return sharedBluetoothViewModel;
    }

    public SharedLocationViewModel getLocationViewModel() {
        return sharedLocationViewModel;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("service", "oncreate");
        sharedLocationViewModel = new SharedLocationViewModel();
        //todo make requests LESS FREQUENT and change mindistance (~ONCE PER 5 MINS)
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        assert mLocationManager != null;
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LocationUtils.MIN_TIME,
                LocationUtils.MIN_DISTANCE, mLocationListener);
        sharedLocationViewModel.getLocations().observeForever(locations -> {
            allLocations = locations;
            Log.e("LOADED", "LOCATIONS: " + locations.size());
        });
        loadLocationsFromDb();

        sharedBluetoothViewModel = new SharedBluetoothViewModel(getApplicationContext());
        buttonBleManager = new ButtonBleManager(getApplicationContext());
        buttonBleManager
                .getButtonState()
                .observeForever(integer -> sendCommand(integer, buttonStates, allLocations, sharedLocationViewModel.getCurrentLocation().getValue()));
    }

    private void sendCommand(int rawValue, String[] buttonStates, List<Location> allLocations, int currentLocation) {
        //TODO REFACTOR
        if (rawValue > 0 && rawValue <= buttonStates.length) {
            Log.d("ble", "ble btn state:" + buttonStates[rawValue - 1]);
            if (currentLocation >= 0) {
                Location location = allLocations.get(currentLocation);
                Log.d("command", location.getCommand(rawValue - 1));
            }
        } else {
            Log.e("ble", "ble btn state:" + "Состояние неизвестно");
        }
    }

    //TODO !!! reload locations when database has changed (send intent from add location service)
    public void loadLocationsFromDb() {
        LocationsDbLoader locationsDbLoader = new LocationsDbLoader(sharedLocationViewModel, AppDatabase.getInstance(getApplicationContext()));
        locationsDbLoader.execute();
    }

    //TODO !!! set "OUTSIDE" LOCATION WHEN GPS IS OFF
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            if (allLocations != null) {
                int currLocIndex = LocationUtils.getCurrentLocation(allLocations, location);
                sharedLocationViewModel.setCurrentLocation(currLocIndex);
            }
            //Log.e("curr loc", "loc: " + location.getLatitude() + " " + location.getLongitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            //todo restart service
        }

        @Override
        public void onProviderDisabled(String provider) {
            //todo stop service and show message "location turned off"
        }
    };

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(final int callbackType, @NonNull final ScanResult result) {
        }

        @Override
        public void onBatchScanResults(@NonNull final List<ScanResult> results) {
            bufferCount++;
            if (bufferCount == BUFFER_CLEAR_FREQUENCY) {
                bufferCount = 0;
                devicesBuffer.clear();
            }

            for (ScanResult result : results) {
                devicesBuffer.add(result.getDevice().getAddress());
                BluetoothDevice device = result.getDevice();
                if (!devices.containsKey(device.getAddress())) {
                    devices.put(device.getAddress(), device);
                }
            }

            for (Iterator<Map.Entry<String, BluetoothDevice>> it = devices.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, BluetoothDevice> entry = it.next();
                if (!devicesBuffer.contains(entry.getKey()))
                    it.remove();
            }

            //update model for application communication
            sharedBluetoothViewModel.setDevicesMap(devices);
            //Log.e("ble","scan accomplished: "+results.size());
            //todo is connected bool
            //TODO SETUP connect/disconnect receiver
            //todo RECONNECT when changed device in settings
            BluetoothUtils.connectToDevice(devices, buttonBleManager, getApplicationContext());
        }

        @Override
        public void onScanFailed(final int errorCode) {
            //Toast.makeText(getApplicationContext(), "SCANNING FAILED", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null)
                switch (action) {
                    case ACTION_START_FOREGROUND_SERVICE:
                        startForegroundService();
                        //Toast.makeText(getApplicationContext(), "Foreground service is started.", Toast.LENGTH_LONG).show();
                        break;
                    case ACTION_STOP_FOREGROUND_SERVICE:
                        stopForegroundService();
                        //Toast.makeText(getApplicationContext(), "Foreground service is stopped.", Toast.LENGTH_LONG).show();
                        break;
                }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startForegroundService() {
        NotificationUtils.makeFirstNotificationSetup(getApplicationContext());
        Notification notification = NotificationUtils.createServiceNotification("Device not connected", getApplicationContext());
        NotificationUtils.notify(notification, getApplicationContext());
        startForeground(NotificationUtils.NOTIFICATION_ID, notification);

        //todo restart scanning when location turned on
        isScannerStarted = BluetoothUtils.startScanning(isScannerStarted, scanCallback);
    }

    private void stopForegroundService() {
        Log.d(TAG_FOREGROUND_SERVICE, "Stop foreground service.");
        stopForeground(true);
        stopSelf();
    }
}