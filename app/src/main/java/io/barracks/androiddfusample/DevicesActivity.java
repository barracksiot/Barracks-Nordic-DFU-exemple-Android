package io.barracks.androiddfusample;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

/**
 * DevicesActivity scan ble devices around
 *
*/
public class DevicesActivity extends AppCompatActivity {

    public static final String EXTRA_BLE_DEVICE = "EXTRA_BLE_DEVICE";
    public static int PERMISSION_REQUEST_CONSTANT = 1;

    private RecyclerView mRecyclerView;
    private DevicesAdapter mAdapter;
    private BluetoothLeScannerCompat bleScanner;
    private TextView numberOfDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        numberOfDevices = (TextView) findViewById(R.id.numberOfDevices);
        mRecyclerView = (RecyclerView) findViewById(R.id.devices_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        findViewById(R.id.helpButtonMain).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://barracksiot.github.io/"));
                startActivity(browserIntent);
            }
        });

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new DevicesAdapter();
        mRecyclerView.setAdapter(mAdapter);

        bleScanner = BluetoothLeScannerCompat.getScanner();

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CONSTANT);

    }

    @Override
    protected void onResume() {
        super.onResume();

        findDevices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        bleScanner.stopScan(scanCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bleScanner.stopScan(scanCallback);
    }

    /**
     * Start discovering bluetooth devices using {@link BluetoothLeScannerCompat}
     */
    public void findDevices(){

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(2000)
                .setUseHardwareBatchingIfSupported(false).build();
        List<ScanFilter> filters = new ArrayList<>();

        bleScanner.stopScan(scanCallback);
        bleScanner.startScan(filters, settings, scanCallback);
    }


    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case 1 : {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    findDevices();
                }
            }
        }
    }

    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            mAdapter.devices = new ArrayList(results);
            mAdapter.notifyDataSetChanged();
            numberOfDevices.setText(getString(R.string.number_of_devices, mAdapter.devices.size()));
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e("bleScan","onScanFailed with coe : "+errorCode);
        }
    };


    /**
     * Adapter for {@link RecyclerView}
     */
    public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.ViewHolder> implements View.OnClickListener{

        ArrayList<ScanResult> devices = new ArrayList<>();

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, null);
            v.setOnClickListener(this);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            BluetoothDevice device = devices.get(position).getDevice();
            if(device != null) {
                holder.text1.setText(device.getName());
                holder.text2.setText(device.getAddress());
            }
        }

        @Override
        public int getItemCount() {
            return devices.size();
        }

        @Override
        public void onClick(View view) {
            int position = mRecyclerView.getChildLayoutPosition(view);
            Log.d("Devices","Click at position " + position);

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra(EXTRA_BLE_DEVICE, devices.get(position).getDevice());

            bleScanner.stopScan(scanCallback);
            startActivity(intent);
        }

        class ViewHolder extends RecyclerView.ViewHolder{

            TextView text1, text2;

            ViewHolder(View itemView) {
                super(itemView);
                text1 = (TextView) itemView.findViewById(android.R.id.text1);
                text2 = (TextView) itemView.findViewById(android.R.id.text2);
            }
        }
    }

}
