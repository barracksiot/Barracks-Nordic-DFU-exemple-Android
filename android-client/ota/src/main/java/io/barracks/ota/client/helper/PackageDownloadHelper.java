/*
 *    Copyright 2016 Barracks Solutions Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.barracks.ota.client.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import io.barracks.ota.client.PackageDownloadService;
import io.barracks.ota.client.UpdateCheckService;
import io.barracks.ota.client.api.UpdateDetails;

/**
 * A helper which makes it easier to use the {@link PackageDownloadService}.
 * <p>
 * Use {@link #bind(Context, PackageDownloadCallback)} before starting a download,
 * and {@link #unbind(Context)} when you are done using the helper.
 * </p>
 */
public class PackageDownloadHelper extends BroadcastReceiver {
    private static final String TAG = PackageDownloadHelper.class.getSimpleName();
    private final String apiKey;
    private Context context;
    private PackageDownloadCallback callback;

    /**
     * Helper's contstructor.
     *
     * @param apiKey The API key provided by the Barracks platform.
     */
    public PackageDownloadHelper(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case PackageDownloadService.ACTION_DOWNLOAD_PACKAGE:
                if (callback.hashCode() == intent.getIntExtra(UpdateCheckService.EXTRA_CALLBACK, 0)) {
                    if (intent.hasCategory(PackageDownloadService.DOWNLOAD_PROGRESS)) {
                        callback.onDownloadProgress(intent.<UpdateDetails>getParcelableExtra(PackageDownloadService.EXTRA_UPDATE_DETAILS), intent.getIntExtra(PackageDownloadService.EXTRA_PROGRESS, 0));
                    } else if (intent.hasCategory(PackageDownloadService.DOWNLOAD_SUCCESS)) {
                        callback.onDownloadSuccess(intent.<UpdateDetails>getParcelableExtra(PackageDownloadService.EXTRA_UPDATE_DETAILS), intent.getStringExtra(PackageDownloadService.EXTRA_FINAL_DEST));
                    } else if (intent.hasCategory(PackageDownloadService.DOWNLOAD_ERROR)) {
                        callback.onDownloadFailure(intent.<UpdateDetails>getParcelableExtra(PackageDownloadService.EXTRA_UPDATE_DETAILS), (Throwable) intent.getSerializableExtra(PackageDownloadService.EXTRA_EXCEPTION));
                    }
                }
                break;
        }
    }

    /**
     * Call this method to register your callback before performing a download.
     *
     * @param context  The context.
     * @param callback The {@link PackageDownloadCallback} which will be called during the download.
     */
    public void bind(Context context, PackageDownloadCallback callback) {
        this.context = context;
        this.callback = callback;
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        manager.registerReceiver(this, PackageDownloadService.ACTION_DOWNLOAD_PACKAGE_FILTER);
    }

    /**
     * Call this method to unregister your callback and free the resources when you are done with
     * this helper.
     *
     * @param context The context.
     */
    public void unbind(Context context) {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        manager.unregisterReceiver(this);
        this.context = null;
        this.callback = null;
    }

    /**
     * This method request requests the download of a package.
     *
     * @param response The details received from the Barracks platform.
     * @see #requestDownload(UpdateDetails, String, String)
     */
    public void requestDownload(UpdateDetails response) {
        requestDownload(response, null, null);
    }

    /**
     * This method request requests the download of a package.<br>
     *
     * @param response  The details received from the Barracks platform.
     * @param tmpFile   The temporary destination of the package.
     * @param finalFile The final destination of the package.
     */
    public void requestDownload(UpdateDetails response, String tmpFile, String finalFile) {
        Intent intent = new Intent(context, PackageDownloadService.class)
                .setAction(PackageDownloadService.ACTION_DOWNLOAD_PACKAGE)
                .putExtra(PackageDownloadService.EXTRA_API_KEY, apiKey)
                .putExtra(PackageDownloadService.EXTRA_TMP_DEST, tmpFile)
                .putExtra(PackageDownloadService.EXTRA_FINAL_DEST, finalFile)
                .putExtra(PackageDownloadService.EXTRA_CALLBACK, callback.hashCode())
                .putExtra(PackageDownloadService.EXTRA_UPDATE_DETAILS, response);
        context.startService(intent);
    }
}
