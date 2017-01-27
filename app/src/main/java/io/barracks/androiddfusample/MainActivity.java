package io.barracks.androiddfusample;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

import io.barracks.ota.client.api.UpdateDetails;
import io.barracks.ota.client.api.UpdateDetailsRequest;
import io.barracks.ota.client.helper.BarracksHelper;
import io.barracks.ota.client.helper.PackageDownloadCallback;
import io.barracks.ota.client.helper.PackageDownloadHelper;
import io.barracks.ota.client.helper.UpdateCheckCallback;
import io.barracks.ota.client.helper.UpdateCheckHelper;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceController;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;


/**
 * MainActivity manage update check download and install on the target Nordic ble device
 *
 * - Check new available update from your Barracks account using {@link io.barracks.ota.client.helper.BarracksHelper }
 * - Download the update proceed to DFU install on the given {@link BluetoothDevice}, using {@link no.nordicsemi.android.dfu.DfuBaseService} from Nordic
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private UpdateCheckHelper updateCheckHelper;
    private PackageDownloadHelper packageDownloadHelper;
    private Button mainButton;
    private EditText versionIdEditText;
    private TextView versionID, updateState, installState, updateName, updateSize;
    private ProgressBar installProgressBar;

    private BluetoothDevice bluetoothDevice;
    private String updatePath;

    private DfuProgressListener dfuProgressListener;
    private DfuServiceController dfuServiceController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        versionID = (TextView) findViewById(R.id.versionIDTextView);
        updateState = (TextView) findViewById(R.id.updateStateTextView);
        installState = (TextView) findViewById(R.id.installStateTextView);
        updateName = (TextView) findViewById(R.id.updateNameTextView);
        updateSize = (TextView) findViewById(R.id.updateSizeTextView);
        versionIdEditText = (EditText) findViewById(R.id.versionIdEditText);
        mainButton = (Button) findViewById(R.id.mainButton);
        installProgressBar = (ProgressBar) findViewById(R.id.installProgressBar);

        mainButton.setText(R.string.check_update);

        findViewById(R.id.helpButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dfuServiceController != null){
                    dfuServiceController.abort();
                }
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://barracksiot.github.io/"));
                startActivity(browserIntent);
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            bluetoothDevice = (BluetoothDevice) intent.getExtras().get(DevicesActivity.EXTRA_BLE_DEVICE);
            if (bluetoothDevice != null) {
                ((TextView) findViewById(R.id.deviceNameTextView)).setText(bluetoothDevice.getName());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        BarracksHelper helper = new BarracksHelper("YOUR_API_KEY", "https://app.barracks.io/");

        // Update Check Helper from Barracks
        updateCheckHelper = helper.getUpdateCheckHelper();
        updateCheckHelper.bind(this, new UpdateCheckCallback() {
            @Override
            public void onUpdateAvailable(UpdateDetailsRequest request, UpdateDetails response) {

                updateState.setText(getString(R.string.update_description));
                updateName.setText(response.getVersionId());
                updateSize.setText(Formatter.formatFileSize(MainActivity.this, response.getPackageInfo().getSize()));

                updateState.setTag(response);
                mainButton.setText(getString(R.string.proceed_dfu));
            }

            @Override
            public void onUpdateUnavailable(UpdateDetailsRequest request) {
                updateState.setText(getString(R.string.update_unavailable));
                updateState.setTag(null);
            }

            @Override
            public void onUpdateRequestError(UpdateDetailsRequest request, Throwable t) {
                updateState.setText(getString(R.string.update_check_error, t.getMessage()));
                updateState.setTag(null);
            }
        });

        // Download package Helper from Barracks
        packageDownloadHelper = helper.getPackageDownloadHelper();
        packageDownloadHelper.bind(this, new PackageDownloadCallback() {
            @Override
            public void onDownloadSuccess(UpdateDetails details, String path) {
                installProgressBar.setProgress(50);
                updatePath = path;
                dfuInstall();
            }

            @Override
            public void onDownloadFailure(UpdateDetails details, Throwable throwable) {
                installProgressBar.setProgress(0);
            }

            @Override
            public void onDownloadProgress(UpdateDetails details, int progress) {
                installProgressBar.setProgress(progress / 2);
            }
        });

        // ProgressListener from Nordic for the DFU process
        dfuProgressListener = new DfuProgressListenerAdapter() {
            @Override
            public void onDfuCompleted(String deviceAddress) {
                mainButton.setText(getString(R.string.back_to_devices));
                mainButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onDfuAborted(String deviceAddress) {
                installState.setText(getString(R.string.aborted));
            }

            @Override
            public void onError(String deviceAddress, int error, int errorType, String message) {
                Log.e(TAG, "Error during dfu process : " + message);
                installState.setText(message);
            }

            @Override
            public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
                installProgressBar.setProgress(50 + (percent / 2 ));
            }

            @Override
            public void onDfuProcessStarted(String deviceAddress) {
                mainButton.setText(getString(R.string.uploading));
            }

            @Override
            public void onDeviceConnecting(String deviceAddress) {
                mainButton.setText(getString(R.string.connecting));
            }

            @Override
            public void onDeviceDisconnecting(String deviceAddress) {
                mainButton.setText(getString(R.string.disconnecting));
            }
        };


        mainButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Check Update
                        if (((Button) v).getText().equals(getString(R.string.check_update))) {
                            try {
                                updateCheckHelper.requestUpdate(
                                        new UpdateDetailsRequest.Builder()
                                                .versionId(versionIdEditText.getText().toString())
                                                .unitId("nrf52")
                                                .build()
                                );
                            } catch (Exception e) {
                                Log.e(TAG, "Check update error : " + e);
                                updateState.setText(getString(R.string.update_check_error, e.getMessage()));
                            }
                        }
                        // Download update to proceed DFU
                        else if (((Button) v).getText().equals(getString(R.string.proceed_dfu))) {

                            // UI
                            installState.setText(getString(R.string.download_package));
                            mainButton.setVisibility(View.INVISIBLE);
                            installProgressBar.setVisibility(View.VISIBLE);
                            installState.setVisibility(View.VISIBLE);

                            versionIdEditText.setEnabled(false);
                            versionID.setVisibility(View.VISIBLE);

                            // Download the given update with the Barracks packageDownloadHelper
                            packageDownloadHelper.requestDownload((UpdateDetails) updateState.getTag());

                        }
                        // Back to devices list
                        else {
                             finish();
                        }
                    }
                }
        );
    }

    /**
     * Proceed to DFU installation using the {@link no.nordicsemi.android.dfu.DfuBaseService} from Nordic
     * Check that he target package install is a .ZIP file
     */
    public void dfuInstall() {

        installState.setText(getString(R.string.proceed_to_dfu));
        Log.d(TAG, "Install Dfu Package on : " + bluetoothDevice);

        DfuServiceListenerHelper.registerProgressListener(this, dfuProgressListener);

        final DfuServiceInitiator starter = new DfuServiceInitiator(bluetoothDevice.getAddress())
                .setDeviceName(bluetoothDevice.getName())
                .setForceDfu(true)
                .setKeepBond(true)
                .setPacketsReceiptNotificationsEnabled(true);

        String newPath = updatePath.substring(0, updatePath.lastIndexOf('.'));
        newPath += ".zip";

        File f = new File(updatePath);
        f.renameTo(new File(newPath));

        starter.setZip(newPath);

        // We can use the controller to pause, resume or abort the DFU process.
        dfuServiceController = starter.start(this, DfuService.class);
    }


    @Override
    protected void onPause() {
        super.onPause();
        updateCheckHelper.unbind(this);
        packageDownloadHelper.unbind(this);
    }

    @Override
    protected void onDestroy() {
        if(dfuServiceController != null) {
            dfuServiceController.abort();
        }
        super.onDestroy();
    }
}
