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

package io.barracks.ota.client.api;

import android.os.Parcel;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import io.barracks.client.ota.BuildConfig;

/**
 * Created by saiimons on 16-04-06.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class UpdateDetailsTest {

    public static void assertValues(UpdateDetails response) {
        Assert.assertNotNull(response);
        Assert.assertTrue("42".equals(response.getVersionId()));
        Assert.assertTrue("http://barracks.io/".equals(response.getPackageInfo().getUrl()));
        Assert.assertTrue("deadbeef".equals(response.getPackageInfo().getMd5()));
        Assert.assertEquals(21432144324324322L, response.getPackageInfo().getSize().longValue());
    }

    private UpdateDetails parseFromResources() throws IOException {
        Gson gson = new GsonBuilder()
                .setExclusionStrategies(
                        new ExclusionStrategy() {
                            @Override
                            public boolean shouldSkipField(FieldAttributes f) {
                                return "__robo_data__".equals(f.getName());
                            }

                            @Override
                            public boolean shouldSkipClass(Class<?> clazz) {
                                return false;
                            }
                        }
                )
                .create();
        ClassLoader.getSystemResource("update_check_response_success.json");
        File f = new File(ClassLoader.getSystemResource("update_check_response_success.json").getPath());
        return gson.fromJson(new FileReader(f), UpdateDetails.class);
    }

    @Test
    public void parseSuccess() throws IOException {
        UpdateDetails response = parseFromResources();
        assertValues(response);
    }

    @Test
    public void parcel() throws IOException {
        UpdateDetails response = parseFromResources();

        Parcel parcel = Parcel.obtain();
        response.writeToParcel(parcel, 0);

        parcel.setDataPosition(0);

        response = UpdateDetails.CREATOR.createFromParcel(parcel);
        assertValues(response);
    }

}
