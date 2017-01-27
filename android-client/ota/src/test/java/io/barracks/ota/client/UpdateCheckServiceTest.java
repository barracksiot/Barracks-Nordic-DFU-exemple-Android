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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ServiceController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import io.barracks.client.ota.BuildConfig;
import io.barracks.ota.client.api.UpdateDetailsRequest;
import io.barracks.ota.client.api.UpdateDetails;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Created by saiimons on 16-04-06.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class UpdateCheckServiceTest {
    ServiceController<UpdateCheckService> controller;
    UpdateCheckService service;
    LocalBroadcastManager manager;

    @Before
    public void prepare() {
        manager = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);
        controller = Robolectric.buildService(UpdateCheckService.class);
        service = controller.attach().create().get();
    }

    @Test
    public void callbackRan() {
        CallbackCalled testCallback = new CallbackCalled();
        manager.registerReceiver(testCallback, UpdateCheckService.ACTION_CHECK_FILTER);
        service.onHandleIntent(
                new Intent(UpdateCheckService.ACTION_CHECK)
                        .putExtra(UpdateCheckService.EXTRA_API_KEY, "mandatory")
        );
        manager.unregisterReceiver(testCallback);
        assertTrue(testCallback.called);
    }

    @Test
    public void missingParameters() {
        CallbackFailed testCallback = new CallbackFailed();
        manager.registerReceiver(testCallback, UpdateCheckService.ACTION_CHECK_FILTER);
        service.onHandleIntent(new Intent(UpdateCheckService.ACTION_CHECK));
        manager.unregisterReceiver(testCallback);
        assertTrue(testCallback.failed);
    }

    @Test
    public void missingKey() {
        CallbackFailed testCallback = new CallbackFailed();
        manager.registerReceiver(testCallback, UpdateCheckService.ACTION_CHECK_FILTER);
        service.onHandleIntent(
                new Intent(UpdateCheckService.ACTION_CHECK)
                        .putExtra(UpdateCheckService.EXTRA_REQUEST, new UpdateDetailsRequest.Builder().unitId("42").versionId("42").build())
        );
        manager.unregisterReceiver(testCallback);
        assertTrue(testCallback.failed);
    }

    @Test
    public void missingRequest() {
        CallbackFailed testCallback = new CallbackFailed();
        manager.registerReceiver(testCallback, UpdateCheckService.ACTION_CHECK_FILTER);
        service.onHandleIntent(
                new Intent(UpdateCheckService.ACTION_CHECK)
                        .putExtra(UpdateCheckService.EXTRA_API_KEY, "badc0fee")
        );
        manager.unregisterReceiver(testCallback);
        assertTrue(testCallback.failed);
    }

    @Test
    public void positiveResponse() throws IOException {
        File f = new File(ClassLoader.getSystemResource("update_check_response_success.json").getPath());
        BufferedReader reader = new BufferedReader(new FileReader(f));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        reader.close();
        MockWebServer server = new MockWebServer();
        server.enqueue(
                new MockResponse()
                        .addHeader("Content-Type", "application/json; charset=utf-8")
                        .addHeader("Cache-Control", "no-cache")
                        .setResponseCode(200)
                        .setBody(stringBuilder.toString())
        );

        CallbackSuccess testCallback = new CallbackSuccess();
        manager.registerReceiver(testCallback, UpdateCheckService.ACTION_CHECK_FILTER);
        UpdateDetailsRequest request = new UpdateDetailsRequest.Builder()
                .unitId("12")
                .versionId("v0.1")
                .build();
        service.onHandleIntent(
                new Intent(UpdateCheckService.ACTION_CHECK)
                        .putExtra(UpdateCheckService.EXTRA_URL, server.url("/").toString())
                        .putExtra(UpdateCheckService.EXTRA_API_KEY, "mandatory")
                        .putExtra(UpdateCheckService.EXTRA_REQUEST, request)
        );
        manager.unregisterReceiver(testCallback);
        assertNotNull(testCallback.response);
        Assert.assertTrue(testCallback.called);
    }

    @Test
    public void noUpdateResponse() {
        MockWebServer server = new MockWebServer();
        server.enqueue(
                new MockResponse()
                        .addHeader("Content-Type", "application/json; charset=utf-8")
                        .addHeader("Cache-Control", "no-cache")
                        .setStatus("HTTP/1.1 204 No content")
        );
        CallbackSuccess testCallback = new CallbackSuccess();
        manager.registerReceiver(testCallback, UpdateCheckService.ACTION_CHECK_FILTER);
        UpdateDetailsRequest request = new UpdateDetailsRequest.Builder()
                .unitId("12")
                .versionId("v0.1")
                .build();
        service.onHandleIntent(
                new Intent(UpdateCheckService.ACTION_CHECK)
                        .putExtra(UpdateCheckService.EXTRA_URL, server.url("/").toString())
                        .putExtra(UpdateCheckService.EXTRA_API_KEY, "mandatory")
                        .putExtra(UpdateCheckService.EXTRA_REQUEST, request)
        );
        manager.unregisterReceiver(testCallback);
        Assert.assertNull(testCallback.response);
        Assert.assertTrue(testCallback.called);
    }

    @Test
    public void errorResponse() {
        MockWebServer server = new MockWebServer();
        server.enqueue(
                new MockResponse()
                        .addHeader("Content-Type", "application/json; charset=utf-8")
                        .addHeader("Cache-Control", "no-cache")
                        .setResponseCode(500)
        );
        CallbackFailed testCallback = new CallbackFailed();
        manager.registerReceiver(testCallback, UpdateCheckService.ACTION_CHECK_FILTER);
        UpdateDetailsRequest request = new UpdateDetailsRequest.Builder()
                .unitId("12")
                .versionId("v0.1")
                .build();

        service.onHandleIntent(
                new Intent(UpdateCheckService.ACTION_CHECK)
                        .putExtra(UpdateCheckService.EXTRA_URL, server.url("/").toString())
                        .putExtra(UpdateCheckService.EXTRA_API_KEY, "mandatory")
                        .putExtra(UpdateCheckService.EXTRA_REQUEST, request)
        );
        Assert.assertTrue(testCallback.failed);

        manager.unregisterReceiver(testCallback);
    }

    @After
    public void finish() {
        controller.destroy();
    }

    private static final class CallbackCalled extends BroadcastReceiver {
        boolean called = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UpdateCheckService.ACTION_CHECK)) {
                called = true;
            }
        }
    }

    private static final class CallbackFailed extends BroadcastReceiver {
        boolean failed = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (
                    intent.getAction().equals(UpdateCheckService.ACTION_CHECK)
                            && intent.hasCategory(UpdateCheckService.UPDATE_REQUEST_ERROR)
                            && intent.hasExtra(UpdateCheckService.EXTRA_EXCEPTION)
                    ) {
                failed = true;
            }
        }
    }

    private static final class CallbackSuccess extends BroadcastReceiver {
        UpdateDetails response = null;
        boolean called = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (
                    intent.getAction().equals(UpdateCheckService.ACTION_CHECK) &&
                            (
                                    (intent.hasCategory(UpdateCheckService.UPDATE_AVAILABLE) && intent.hasExtra(UpdateCheckService.EXTRA_UPDATE_DETAILS)) ||
                                            intent.hasCategory(UpdateCheckService.UPDATE_UNAVAILABLE)
                            )
                    ) {
                response = intent.getParcelableExtra(UpdateCheckService.EXTRA_UPDATE_DETAILS);
                called = true;
            }
        }
    }
}
