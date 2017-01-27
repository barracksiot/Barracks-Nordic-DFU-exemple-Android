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

import java.io.FileNotFoundException;

import io.barracks.client.ota.BuildConfig;
import io.barracks.ota.client.PackageDownloadService;
import io.barracks.ota.client.Utils;
import io.barracks.ota.client.api.UpdateDetails;

/**
 * Created by saiimons on 27/04/2016.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class PackageDownloadHelperTest {

    @Test
    public void calls() {
        TestCallback callback;

        PackageDownloadHelper helper = new PackageDownloadHelper("deadbeef");
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(RuntimeEnvironment.application);

        callback = new TestCallback();
        helper.bind(RuntimeEnvironment.application, callback);
        manager.sendBroadcast(
                new Intent(PackageDownloadService.ACTION_DOWNLOAD_PACKAGE)
                        .addCategory(PackageDownloadService.DOWNLOAD_SUCCESS)
                        .putExtra(PackageDownloadService.EXTRA_CALLBACK, callback.hashCode())
        );
        Assert.assertTrue(callback.success);
        Assert.assertFalse(callback.failure);
        Assert.assertFalse(callback.progress);
        helper.unbind(RuntimeEnvironment.application);

        callback = new TestCallback();
        helper.bind(RuntimeEnvironment.application, callback);
        manager.sendBroadcast(
                new Intent(PackageDownloadService.ACTION_DOWNLOAD_PACKAGE)
                        .addCategory(PackageDownloadService.DOWNLOAD_ERROR)
                        .putExtra(PackageDownloadService.EXTRA_CALLBACK, callback.hashCode())
        );
        Assert.assertTrue(callback.failure);
        Assert.assertFalse(callback.progress);
        Assert.assertFalse(callback.success);
        helper.unbind(RuntimeEnvironment.application);

        callback = new TestCallback();
        helper.bind(RuntimeEnvironment.application, callback);
        manager.sendBroadcast(
                new Intent(PackageDownloadService.ACTION_DOWNLOAD_PACKAGE)
                        .addCategory(PackageDownloadService.DOWNLOAD_PROGRESS)
                        .putExtra(PackageDownloadService.EXTRA_CALLBACK, callback.hashCode())
        );
        Assert.assertTrue(callback.progress);
        Assert.assertFalse(callback.failure);
        Assert.assertFalse(callback.success);
        helper.unbind(RuntimeEnvironment.application);
    }

    @Test
    public void service() throws FileNotFoundException {
        UpdateDetails response = Utils.getUpdateDetailsFromFile("download_success.json");
        TestCallback callback = new TestCallback();
        PackageDownloadHelper helper = new PackageDownloadHelper("deadbeef");
        helper.bind(RuntimeEnvironment.application, callback);
        helper.requestDownload(response);
        Intent intent = Shadows.shadowOf(RuntimeEnvironment.application).getNextStartedService();
        Assert.assertNotNull(intent);
        Assert.assertEquals(intent.getComponent().getClassName(), PackageDownloadService.class.getName());
        Assert.assertEquals(intent.getAction(), PackageDownloadService.ACTION_DOWNLOAD_PACKAGE);
        Assert.assertEquals(callback.hashCode(), intent.getIntExtra(PackageDownloadService.EXTRA_CALLBACK, 0));
        UpdateDetails response2 = intent.getParcelableExtra(PackageDownloadService.EXTRA_UPDATE_DETAILS);
        Assert.assertNotNull(response2);
        helper.unbind(RuntimeEnvironment.application);
    }

    private static final class TestCallback implements PackageDownloadCallback {
        boolean success = false;
        boolean progress = false;
        boolean failure = false;

        @Override
        public void onDownloadSuccess(UpdateDetails details, String path) {
            success = true;
        }

        @Override
        public void onDownloadFailure(UpdateDetails details, Throwable throwable) {
            failure = true;
        }

        @Override
        public void onDownloadProgress(UpdateDetails details, int progress) {
            this.progress = true;
        }
    }
}
