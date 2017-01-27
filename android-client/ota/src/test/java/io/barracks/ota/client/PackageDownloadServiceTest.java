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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ServiceController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import io.barracks.client.ota.BuildConfig;
import io.barracks.ota.client.api.PackageInfo;
import io.barracks.ota.client.api.UpdateDetails;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import okio.Buffer;

import static org.junit.Assert.assertTrue;

/**
 * Created by saiimons on 16-04-21.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class PackageDownloadServiceTest {
    LocalBroadcastManager manager;
    ServiceController<PackageDownloadService> controller;
    PackageDownloadService service;
    MockWebServer server;
    UpdateDetails successResponse, failureResponse, ioErrorResponse, signatureFailResponse;

    @Before
    public void prepare() throws IOException, NoSuchFieldException, IllegalAccessException {
        manager = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);
        controller = Robolectric.buildService(PackageDownloadService.class);
        service = controller.attach().create().get();
        server = new MockWebServer();
        successResponse = Utils.getUpdateDetailsFromFile("download_success.json");
        failureResponse = Utils.getUpdateDetailsFromFile("download_success.json");
        ioErrorResponse = Utils.getUpdateDetailsFromFile("download_success.json");
        signatureFailResponse = Utils.getUpdateDetailsFromFile("download_success.json");

        final MockResponse success = new MockResponse()
                .setBody(
                        new Buffer()
                                .readFrom(
                                        new FileInputStream(
                                                new File(ClassLoader.getSystemResource("file.txt").getPath())
                                        )
                                )

                );

        final MockResponse failure = new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Cache-Control", "no-cache")
                .setResponseCode(500);

        final MockResponse ioerror = new MockResponse()
                .setBody(
                        new Buffer()
                                .readFrom(
                                        new FileInputStream(
                                                new File(ClassLoader.getSystemResource("file.txt").getPath())
                                        )
                                )

                )
                .setSocketPolicy(SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY);

        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest recordedRequest) throws InterruptedException {
                String path = recordedRequest.getPath();
                if ("/failure".equals(path)) {
                    return failure;
                } else if ("/ioerror".equals(path)) {
                    return ioerror;
                } else if ("/signature".equals(path)) {
                    return success;
                } else {
                    return success;
                }
            }
        });


        PackageInfo info = successResponse.getPackageInfo();
        Field url = PackageInfo.class.getDeclaredField("url");
        url.setAccessible(true);
        url.set(info, server.url("/success").toString());

        info = failureResponse.getPackageInfo();
        url = PackageInfo.class.getDeclaredField("url");
        url.setAccessible(true);
        url.set(info, server.url("/failure").toString());

        info = ioErrorResponse.getPackageInfo();
        url = PackageInfo.class.getDeclaredField("url");
        url.setAccessible(true);
        url.set(info, server.url("/ioerror").toString());

        info = signatureFailResponse.getPackageInfo();
        url = PackageInfo.class.getDeclaredField("url");
        url.setAccessible(true);
        url.set(info, server.url("/signature").toString());
        Field md5 = PackageInfo.class.getDeclaredField("md5");
        md5.setAccessible(true);
        md5.set(info, "md5failure");
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void downloadSuccess() throws IOException, NoSuchFieldException, IllegalAccessException {
        CallbackSuccess callbackSuccess = new CallbackSuccess();
        manager.registerReceiver(callbackSuccess, PackageDownloadService.ACTION_DOWNLOAD_PACKAGE_FILTER);
        service.onHandleIntent(
                new Intent(PackageDownloadService.ACTION_DOWNLOAD_PACKAGE)
                        .putExtra(PackageDownloadService.EXTRA_UPDATE_DETAILS, successResponse)
        );
        manager.unregisterReceiver(callbackSuccess);
        assertTrue(callbackSuccess.success);
    }

    @Test
    public void downloadProgress() throws IOException, NoSuchFieldException, IllegalAccessException {
        CallBackProgress callBackProgress = new CallBackProgress();
        manager.registerReceiver(callBackProgress, PackageDownloadService.ACTION_DOWNLOAD_PACKAGE_FILTER);
        service.onHandleIntent(
                new Intent(PackageDownloadService.ACTION_DOWNLOAD_PACKAGE)
                        .putExtra(PackageDownloadService.EXTRA_UPDATE_DETAILS, successResponse)
        );
        manager.unregisterReceiver(callBackProgress);
        assertTrue(callBackProgress.progress);
    }

    @Test
    public void downloadSuccessCustomPath() {
        CallbackSuccess callbackSuccess = new CallbackSuccess();
        manager.registerReceiver(callbackSuccess, PackageDownloadService.ACTION_DOWNLOAD_PACKAGE_FILTER);
        service.onHandleIntent(
                new Intent(PackageDownloadService.ACTION_DOWNLOAD_PACKAGE)
                        .putExtra(PackageDownloadService.EXTRA_UPDATE_DETAILS, successResponse)
                        .putExtra(PackageDownloadService.EXTRA_TMP_DEST, new File("tmp/tmp.dl"))
                        .putExtra(PackageDownloadService.EXTRA_FINAL_DEST, new File("final/final.dl"))
        );
        manager.unregisterReceiver(callbackSuccess);
        assertTrue(callbackSuccess.success);
    }

    @Test
    public void readOnly() throws IOException {
        File f = new File(RuntimeEnvironment.application.getFilesDir(), "readonly").getAbsoluteFile();
        f.mkdirs();
        f.setWritable(true);
        f = new File(f, "dummy");
        f.createNewFile();
        f.setReadOnly();
        f.getParentFile().setReadOnly();

        // Test the temp
        CallbackFailure callbackFailure = new CallbackFailure();
        manager.registerReceiver(callbackFailure, PackageDownloadService.ACTION_DOWNLOAD_PACKAGE_FILTER);
        service.onHandleIntent(
                new Intent(PackageDownloadService.ACTION_DOWNLOAD_PACKAGE)
                        .putExtra(PackageDownloadService.EXTRA_UPDATE_DETAILS, successResponse)
                        .putExtra(PackageDownloadService.EXTRA_TMP_DEST, f.getPath())
        );
        assertTrue(callbackFailure.failure);
        manager.unregisterReceiver(callbackFailure);

        // Test the destination
        callbackFailure = new CallbackFailure();
        manager.registerReceiver(callbackFailure, PackageDownloadService.ACTION_DOWNLOAD_PACKAGE_FILTER);
        service.onHandleIntent(
                new Intent(PackageDownloadService.ACTION_DOWNLOAD_PACKAGE)
                        .putExtra(PackageDownloadService.EXTRA_UPDATE_DETAILS, successResponse)
                        .putExtra(PackageDownloadService.EXTRA_FINAL_DEST, f.getPath())
        );
        assertTrue(callbackFailure.failure);
        manager.unregisterReceiver(callbackFailure);


        callbackFailure = new CallbackFailure();
        manager.registerReceiver(callbackFailure, PackageDownloadService.ACTION_DOWNLOAD_PACKAGE_FILTER);
        service.onHandleIntent(
                new Intent(PackageDownloadService.ACTION_DOWNLOAD_PACKAGE)
                        .putExtra(PackageDownloadService.EXTRA_UPDATE_DETAILS, successResponse)
                        .putExtra(PackageDownloadService.EXTRA_TMP_DEST, new File(f, "dircantbe").getPath())
        );
        assertTrue(callbackFailure.failure);
        manager.unregisterReceiver(callbackFailure);

        f.setReadable(true);
        f.setWritable(true);
        f.delete();
        callbackFailure = new CallbackFailure();
        manager.registerReceiver(callbackFailure, PackageDownloadService.ACTION_DOWNLOAD_PACKAGE_FILTER);
        service.onHandleIntent(
                new Intent(PackageDownloadService.ACTION_DOWNLOAD_PACKAGE)
                        .putExtra(PackageDownloadService.EXTRA_UPDATE_DETAILS, successResponse)
                        .putExtra(PackageDownloadService.EXTRA_FINAL_DEST, f.getPath())
        );
        assertTrue(callbackFailure.failure);
        manager.unregisterReceiver(callbackFailure);
    }

    @Test
    public void responseFailure() throws InterruptedException {
        CallbackFailure callbackFailure = new CallbackFailure();
        manager.registerReceiver(callbackFailure, PackageDownloadService.ACTION_DOWNLOAD_PACKAGE_FILTER);
        service.onHandleIntent(
                new Intent(PackageDownloadService.ACTION_DOWNLOAD_PACKAGE)
                        .putExtra(PackageDownloadService.EXTRA_UPDATE_DETAILS, failureResponse)
        );
        assertTrue(callbackFailure.failure);
        manager.unregisterReceiver(callbackFailure);


        callbackFailure = new CallbackFailure();
        manager.registerReceiver(callbackFailure, PackageDownloadService.ACTION_DOWNLOAD_PACKAGE_FILTER);
        service.onHandleIntent(
                new Intent(PackageDownloadService.ACTION_DOWNLOAD_PACKAGE)
                        .putExtra(PackageDownloadService.EXTRA_UPDATE_DETAILS, ioErrorResponse)
        );
        assertTrue(callbackFailure.failure);
        manager.unregisterReceiver(callbackFailure);


        callbackFailure = new CallbackFailure();
        manager.registerReceiver(callbackFailure, PackageDownloadService.ACTION_DOWNLOAD_PACKAGE_FILTER);
        service.onHandleIntent(
                new Intent(PackageDownloadService.ACTION_DOWNLOAD_PACKAGE)
                        .putExtra(PackageDownloadService.EXTRA_UPDATE_DETAILS, signatureFailResponse)
        );
        assertTrue(callbackFailure.failure);
        manager.unregisterReceiver(callbackFailure);
    }

    private static class CallbackSuccess extends BroadcastReceiver {
        private boolean success = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (
                    intent.getAction().equals(PackageDownloadService.ACTION_DOWNLOAD_PACKAGE)
                            && intent.hasCategory(PackageDownloadService.DOWNLOAD_SUCCESS)
                    ) {
                success = true;
            }
        }
    }


    private static class CallbackFailure extends BroadcastReceiver {
        private boolean failure = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (
                    intent.getAction().equals(PackageDownloadService.ACTION_DOWNLOAD_PACKAGE)
                            && intent.hasCategory(PackageDownloadService.DOWNLOAD_ERROR)
                    ) {
                failure = true;
            }
        }
    }

    private static class CallBackProgress extends BroadcastReceiver {
        private boolean progress = false;
        private int last = 0;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (
                    intent.getAction().equals(PackageDownloadService.ACTION_DOWNLOAD_PACKAGE)
                            && intent.hasCategory(PackageDownloadService.DOWNLOAD_PROGRESS)
                    ) {
                int current = intent.getIntExtra(PackageDownloadService.EXTRA_PROGRESS, -1);
                if (current == last || current == last + 1) {
                    progress = true;
                    last = current;
                } else {
                    progress = false;
                }
            } else {
                Log.e("FAIL", intent.toString());
            }
        }
    }
}
