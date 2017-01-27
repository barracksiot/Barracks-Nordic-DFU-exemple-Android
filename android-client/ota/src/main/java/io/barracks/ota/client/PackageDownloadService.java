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

package io.barracks.ota.client;

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestException;
import java.security.DigestInputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.barracks.ota.client.api.UpdateDetails;
import io.barracks.ota.client.api.UpdateDownloadApi;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * This service is used to handle the download of a package in the background.<br>
 * It uses the {@link LocalBroadcastManager} to send updates of the download, using the categories
 * for {@link PackageDownloadService#DOWNLOAD_SUCCESS success},
 * {@link PackageDownloadService#DOWNLOAD_PROGRESS progress} and
 * {@link PackageDownloadService#DOWNLOAD_ERROR failure} for a specific {@link PackageDownloadService#ACTION_DOWNLOAD_PACKAGE action}
 *
 * @see io.barracks.ota.client.helper.BarracksHelper
 */
public class PackageDownloadService extends IntentService {
    /**
     * Defines the action used to start the download of a package.
     *
     * @see Intent#setAction(String)
     */
    public static final String ACTION_DOWNLOAD_PACKAGE = "io.barracks.ota.client.DOWNLOAD_PACKAGE";

    /**
     * This key is used to specify the {@link UpdateDetails details} used as a reference for downloading a package.
     */
    public static final String EXTRA_UPDATE_DETAILS = "updateDetails";
    /**
     * This key is used to specify the API key used for downloading a package.
     */
    public static final String EXTRA_API_KEY = "apiKey";
    /**
     * This key is used to specify the temporary destination when downloading a package.
     */
    public static final String EXTRA_TMP_DEST = "tmpDest";
    /**
     * This key is used to specify the final destination when downloading a package.
     */
    public static final String EXTRA_FINAL_DEST = "finalDest";
    /**
     * This key is used to report an {@link Throwable exception} thrown during the download.
     */
    public static final String EXTRA_EXCEPTION = "exception";
    /**
     * This key is used to report of the progress of a download
     */
    public static final String EXTRA_PROGRESS = "progress";
    /**
     * This key is used to report the callback's identifier
     */
    public static final String EXTRA_CALLBACK = "callback";

    /**
     * Category used to notify when a download is complete.
     */
    public static final String DOWNLOAD_SUCCESS = "io.barracks.ota.client.DOWNLOAD_SUCCESS";
    /**
     * Category used to notify when a download has failed.
     */
    public static final String DOWNLOAD_ERROR = "io.barracks.ota.client.DOWNLOAD_ERROR";
    /**
     * Category used to notify when a download is in progress.
     */
    public static final String DOWNLOAD_PROGRESS = "io.barracks.ota.client.DOWNLOAD_PROGRESS";

    /**
     * Intent filter used by {@link android.content.BroadcastReceiver} to register to the {@link LocalBroadcastManager}
     */
    public static final IntentFilter ACTION_DOWNLOAD_PACKAGE_FILTER;

    static {
        ACTION_DOWNLOAD_PACKAGE_FILTER = new IntentFilter(ACTION_DOWNLOAD_PACKAGE);
        ACTION_DOWNLOAD_PACKAGE_FILTER.addCategory(DOWNLOAD_SUCCESS);
        ACTION_DOWNLOAD_PACKAGE_FILTER.addCategory(DOWNLOAD_ERROR);
        ACTION_DOWNLOAD_PACKAGE_FILTER.addCategory(DOWNLOAD_PROGRESS);
    }

    public PackageDownloadService() {
        this(PackageDownloadService.class.getSimpleName());
    }

    /**
     * Creates a {@link PackageDownloadService}.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public PackageDownloadService(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        switch (intent.getAction()) {
            case ACTION_DOWNLOAD_PACKAGE:
                downloadPackage(
                        intent.getStringExtra(EXTRA_API_KEY),
                        intent.getStringExtra(EXTRA_TMP_DEST),
                        intent.getStringExtra(EXTRA_FINAL_DEST),
                        intent.<UpdateDetails>getParcelableExtra(EXTRA_UPDATE_DETAILS),
                        intent.getIntExtra(EXTRA_CALLBACK, -1)
                );
                break;
        }
    }

    /**
     * This method is proceeding with the download and notifying the rest of the application using
     * the {@link LocalBroadcastManager}.
     *
     * @param apiKey    The API key provided by the Barracks platform.
     * @param tmpDest   The temporary path for the download.
     * @param finalDest The final path for the download.
     * @param update    The {@link UpdateDetails} retrieved from the Barracks platform.
     * @param callback  The callback identifier.
     */
    private void downloadPackage(String apiKey, String tmpDest, String finalDest, UpdateDetails update, int callback) {
        File tmp = TextUtils.isEmpty(tmpDest) ? new File(getFilesDir(), Defaults.DEFAULT_TMP_DL_DESTINATION) : new File(tmpDest);
        File destination = TextUtils.isEmpty(finalDest) ? new File(getFilesDir(), Defaults.DEFAULT_FINAL_DL_DESTINATION) : new File(finalDest);
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Defaults.DEFAULT_BASE_URL).build();
        UpdateDownloadApi loader = retrofit.create(UpdateDownloadApi.class);
        Call<ResponseBody> call = loader.downloadUpdate(update.getPackageInfo().getUrl(), apiKey);

        // Setup the files to be loaded and moved
        if (!setupFile(tmp) || !setupFile(destination)) {
            notifyError(update, new IOException("Failed to setup " + tmp.getPath() + " or " + destination.getPath()), callback);
            return;
        }

        // Initiate the transfer
        OutputStream os = null;
        try {
            os = new FileOutputStream(tmp);
            Response<ResponseBody> response = call.execute();
            if (!response.isSuccessful()) {
                notifyError(update, new IOException("Call to : " + call.request().url().toString() + " failed : " + response.code() + " " + response.message()), callback);
                return;
            }
            InputStream is = response.body().byteStream();
            int read;
            int total = 0;
            byte buff[] = new byte[1024];
            while ((read = is.read(buff)) != -1) {
                os.write(buff, 0, read);
                total += read;
                notifyProgress(update, (int) (total * 100 / update.getPackageInfo().getSize()), callback);
            }
            checkPackageIntegrity(update, tmp);
            moveToFinalDestination(tmp, destination);
        } catch (IOException | GeneralSecurityException e) {
            notifyError(update, e, callback);
            return;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        notifySuccess(update, destination, callback);
    }

    /**
     * Convenience method for notifying the application of a download completion using the {@link LocalBroadcastManager}
     *
     * @param details     The {@link UpdateDetails} retrieved from the Barracks platform.
     * @param destination The destination where the file has been moved.
     * @param callback    The callback identifier.
     */
    private void notifySuccess(UpdateDetails details, File destination, int callback) {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.sendBroadcast(
                new Intent(ACTION_DOWNLOAD_PACKAGE)
                        .addCategory(DOWNLOAD_SUCCESS)
                        .putExtra(EXTRA_UPDATE_DETAILS, details)
                        .putExtra(EXTRA_CALLBACK, callback)
                        .putExtra(EXTRA_FINAL_DEST, destination.getPath())
        );
    }

    /**
     * Convenience method for notifying the application of a download failure using the {@link LocalBroadcastManager}
     *
     * @param details   The {@link UpdateDetails} retrieved from the Barracks platform.
     * @param exception The exception caught during the process.
     * @param callback  The callback identifier.
     */
    private void notifyError(UpdateDetails details, Exception exception, int callback) {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.sendBroadcast(
                new Intent(ACTION_DOWNLOAD_PACKAGE)
                        .addCategory(DOWNLOAD_ERROR)
                        .putExtra(EXTRA_UPDATE_DETAILS, details)
                        .putExtra(EXTRA_CALLBACK, callback)
                        .putExtra(EXTRA_EXCEPTION, exception)
        );
    }

    /**
     * Convenience method for notifying the application of a download progress using the {@link LocalBroadcastManager}
     *
     * @param details  The {@link UpdateDetails} retrieved from the Barracks platform.
     * @param progress The progress percentage.
     * @param callback The callback identifier.
     */
    private void notifyProgress(UpdateDetails details, int progress, int callback) {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.sendBroadcast(
                new Intent(ACTION_DOWNLOAD_PACKAGE)
                        .addCategory(DOWNLOAD_PROGRESS)
                        .putExtra(EXTRA_UPDATE_DETAILS, details)
                        .putExtra(EXTRA_CALLBACK, callback)
                        .putExtra(EXTRA_PROGRESS, progress)
        );
    }

    /**
     * Prepares a file for a download : deletes it, creates the parent directories.
     *
     * @param file The file to be prepared.
     * @return True if the file has been successfully prepared, false otherwise.
     */
    protected boolean setupFile(File file) {
        // Check if the destination exists
        if (file.exists()) {
            if (!file.delete()) {
                return false;
            }
        }
        // Check if the parent directory exists or can be created and is a directory
        File tmpParent = file.getParentFile();
        return (tmpParent.mkdirs() || tmpParent.exists()) && tmpParent.isDirectory();
    }

    /**
     * This method checks the package's <code>file</code> integrity.<br>
     * It uses the md5 provided in the <code>details</code> parameter.
     *
     * @param details The {@link UpdateDetails} retrieved from the Barracks platform.
     * @param file    The file which was downloaded.
     * @throws IOException              If an exception is raised while accessing the file.
     * @throws GeneralSecurityException If the hash verification fails.
     */
    protected void checkPackageIntegrity(UpdateDetails details, File file) throws IOException, GeneralSecurityException {
        InputStream is = null;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            is = new FileInputStream(file);
            is = new DigestInputStream(is, md);
            byte[] buffer = new byte[8192];
            while (is.read(buffer) != -1) {
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            throw e;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (md != null) {
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            if (!sb.toString().equals(details.getPackageInfo().getMd5())) {
                throw new DigestException("Wrong file signature " + sb.toString() + " - " + details.getPackageInfo().getMd5());
            }
        }
    }

    /**
     * This method moves the <code>temporary</code> file to its <code>destination</code>.<br>
     * If the file can't be moved, the method tries to copy it.
     *
     * @param temporary   The temporary file.
     * @param destination The destination file.
     * @throws IOException If an exception occurs during the copy phase.
     */
    protected void moveToFinalDestination(File temporary, File destination) throws IOException {
        if (!temporary.renameTo(destination)) {
            FileInputStream fis = null;
            FileOutputStream fos = null;
            try {
                fis = new FileInputStream(temporary);
                fos = new FileOutputStream(destination);
                byte[] buffer = new byte[8192];
                int read;
                while ((read = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
            } catch (IOException e) {
                throw e;
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    private class Binder extends android.os.Binder {
        PackageDownloadService getService() {
            // TODO return a wrapper exposing only the necessary methods
            return PackageDownloadService.this;
        }
    }
}
