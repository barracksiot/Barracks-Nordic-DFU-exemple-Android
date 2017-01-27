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

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import io.barracks.client.ota.BuildConfig;
import io.barracks.ota.client.UpdateCheckService;
import io.barracks.ota.client.api.UpdateDetails;
import io.barracks.ota.client.api.UpdateDetailsRequest;

/**
 * Created by saiimons on 16-04-07.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class UpdateCheckHelperTest {
    @Test
    public void calls() {
        TestCallback callback;

        UpdateCheckHelper helper = new UpdateCheckHelper("deadbeef");
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);

        callback = new TestCallback();
        helper.bind(RuntimeEnvironment.application, callback);
        manager.sendBroadcast(
                new Intent(UpdateCheckService.ACTION_CHECK)
                        .addCategory(UpdateCheckService.UPDATE_AVAILABLE)
                        .putExtra(UpdateCheckService.EXTRA_CALLBACK, callback.hashCode())
        );
        Assert.assertTrue(callback.available);
        Assert.assertFalse(callback.unavailable);
        Assert.assertFalse(callback.error);
        helper.unbind(RuntimeEnvironment.application);

        callback = new TestCallback();
        helper.bind(RuntimeEnvironment.application, callback);
        manager.sendBroadcast(
                new Intent(UpdateCheckService.ACTION_CHECK)
                        .addCategory(UpdateCheckService.UPDATE_UNAVAILABLE)
                        .putExtra(UpdateCheckService.EXTRA_CALLBACK, callback.hashCode())
        );
        Assert.assertTrue(callback.unavailable);
        Assert.assertFalse(callback.available);
        Assert.assertFalse(callback.error);
        helper.unbind(RuntimeEnvironment.application);

        callback = new TestCallback();
        helper.bind(RuntimeEnvironment.application, callback);
        manager.sendBroadcast(
                new Intent(UpdateCheckService.ACTION_CHECK)
                        .addCategory(UpdateCheckService.UPDATE_REQUEST_ERROR)
                        .putExtra(UpdateCheckService.EXTRA_CALLBACK, callback.hashCode())
        );
        Assert.assertTrue(callback.error);
        Assert.assertFalse(callback.available);
        Assert.assertFalse(callback.unavailable);
        helper.unbind(RuntimeEnvironment.application);
    }

    @Test
    public void service() {
        UpdateDetailsRequest request = new UpdateDetailsRequest.Builder()
                .unitId("HAL")
                .versionId("42")
                .build();
        TestCallback callback = new TestCallback();
        UpdateCheckHelper helper = new UpdateCheckHelper("deadbeef");
        helper.bind(RuntimeEnvironment.application, callback);
        helper.requestUpdate(request);
        Intent intent = Shadows.shadowOf(RuntimeEnvironment.application).getNextStartedService();
        Assert.assertNotNull(intent);
        Assert.assertEquals(intent.getComponent().getClassName(), UpdateCheckService.class.getName());
        Assert.assertEquals(intent.getAction(), UpdateCheckService.ACTION_CHECK);
        Assert.assertEquals(callback.hashCode(), intent.getIntExtra(UpdateCheckService.EXTRA_CALLBACK, 0));
        UpdateDetailsRequest request2 = intent.getParcelableExtra(UpdateCheckService.EXTRA_REQUEST);
        Assert.assertNotNull(request2);
        helper.unbind(RuntimeEnvironment.application);
    }

    private static final class TestCallback implements UpdateCheckCallback {
        boolean available = false;
        boolean unavailable = false;
        boolean error = false;

        @Override
        public void onUpdateAvailable(UpdateDetailsRequest request, UpdateDetails details) {
            available = true;
        }

        @Override
        public void onUpdateUnavailable(UpdateDetailsRequest request) {
            unavailable = true;
        }

        @Override
        public void onUpdateRequestError(UpdateDetailsRequest request, Throwable t) {
            error = true;
        }
    }
}
