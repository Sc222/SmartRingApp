package ru.sc222.smartringapp.ui.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ru.sc222.smartringapp.R;
import ru.sc222.smartringapp.services.SmartRingService;
import ru.sc222.smartringapp.utils.BluetoothUtils;
import ru.sc222.smartringapp.utils.LocationUtils;
import ru.sc222.smartringapp.utils.PermissionsUtils;
import ru.sc222.smartringapp.utils.PreferenceUtils;
import ru.sc222.smartringapp.utils.ServiceUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupNavigation();
        if (PermissionsUtils.hasRequiredPermissions(this)) {
            tryToStartService();
        } else
            ActivityCompat.requestPermissions(this, PermissionsUtils.getPermissionsToRequest(this), PermissionsUtils.REQUEST_CODE);
    }

    private void tryToStartService() {
        if (BluetoothUtils.isBluetoothEnabled() && LocationUtils.isLocationEnabled(this)) {
            Log.e("START SERVICE", "SSS");
            startService();
        } else {
            //todo register receivers and show enable bluetooth / location dialog
            Toast.makeText(getApplicationContext(), R.string.app_wont_work_if_bluetooth_and_location_are_disabled, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        //todo show are you sure want to quit dialog
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissionsList, @NonNull int[] grantResults) {
        if (requestCode == PermissionsUtils.REQUEST_CODE) {
            for (int status : grantResults) {
                if (status != PackageManager.PERMISSION_GRANTED) {
                    //todo replace with dialog
                    Toast.makeText(getApplicationContext(), R.string.app_wont_work_without_permissions, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }

            //all permissions are granted
            tryToStartService();
        }
    }

    private void startService() {
        if (!ServiceUtils.isServiceRunning(this, SmartRingService.class)) {
            Intent intent = new Intent(getApplicationContext(), SmartRingService.class);
            intent.setAction(SmartRingService.ACTION_START_FOREGROUND_SERVICE);
            startService(intent);
        }
    }

    private void setupNavigation() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_location, R.id.navigation_commands, R.id.navigation_device)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        int currItem = PreferenceUtils.getCurrentNavigationItem(this);

        switch (currItem) {
            case R.id.navigation_location:
            case R.id.navigation_commands:
            case R.id.navigation_device:
                navController.navigate(currItem);
                break;
        }

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            switch (destination.getId()) {
                case R.id.navigation_location:
                case R.id.navigation_commands:
                case R.id.navigation_device:
                    PreferenceUtils.saveCurrentNavigationItem(getApplicationContext(), destination.getId());
                    break;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
