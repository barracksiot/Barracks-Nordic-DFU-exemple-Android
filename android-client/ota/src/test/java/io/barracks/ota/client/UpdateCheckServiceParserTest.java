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

import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import io.barracks.client.ota.BuildConfig;
import io.barracks.ota.client.api.UpdateDetails;
import io.barracks.ota.client.api.UpdateDetailsTest;

/**
 * Created by saiimons on 16-04-07.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class UpdateCheckServiceParserTest {

    @Test
    public void standardParser() throws IOException {
        UpdateCheckService service = new UpdateCheckService();
        checkJsonResponse(service);
    }

    @Test
    public void customParser() throws IOException {
        UpdateCheckService service = new CustomUpdateCheckService();
        checkJsonResponse(service);
    }

    private void checkJsonResponse(UpdateCheckService service) throws IOException {
        GsonBuilder builder = service.setUpGsonBuilder(new GsonBuilder());
        Gson gson = Utils.getRobolectricGson(builder);
        ClassLoader.getSystemResource("update_check_response_success.json");
        File f = new File(ClassLoader.getSystemResource("update_check_response_success.json").getPath());
        UpdateDetails response = gson.fromJson(new FileReader(f), UpdateDetails.class);
        UpdateDetailsTest.assertValues(response);
        assertProperties(response);

        String json = gson.toJson(response);
        response = gson.fromJson(json, UpdateDetails.class);
        UpdateDetailsTest.assertValues(response);
        assertProperties(response);
    }

    private void assertProperties(UpdateDetails response) {
        Bundle b = response.getCustomUpdateData();
        Assert.assertTrue(b.getBoolean("boolean"));
        Assert.assertEquals(3.14159265d, b.getDouble("double"), 0.0d);
        Assert.assertEquals(123, b.getLong("integer"));
        Assert.assertTrue("toto".equals(b.getString("string")));
    }

    private static final class CustomUpdateCheckService extends UpdateCheckService {
        @Override
        protected TypeAdapter<UpdateDetails> getResponsePropertiesAdapter(Gson gson, TypeToken<UpdateDetails> type) {
            return new CustomPropertiesAdapter(this, gson, type);
        }
    }

    private static final class CustomPropertiesAdapter extends UpdateCheckService.DefaultResponseAdapter {

        public CustomPropertiesAdapter(TypeAdapterFactory factory, Gson gson, TypeToken<UpdateDetails> type) {
            super(factory, gson, type);
        }

        @Override
        public void write(JsonWriter out, UpdateDetails response) throws IOException {
            JsonElement tree = getDelegate().toJsonTree(response);
            JsonObject obj = tree.getAsJsonObject();
            JsonObject customUpdateData = new JsonObject();
            customUpdateData.addProperty("string", response.getCustomUpdateData().getString("string"));
            customUpdateData.addProperty("integer", response.getCustomUpdateData().getLong("integer"));
            customUpdateData.addProperty("boolean", response.getCustomUpdateData().getBoolean("boolean"));
            customUpdateData.addProperty("double", response.getCustomUpdateData().getBoolean("double"));
            obj.add("customUpdateData", customUpdateData);
            getElementAdapter().write(out, tree);
        }

        @Override
        public UpdateDetails read(JsonReader in) throws IOException {
            JsonElement tree = getElementAdapter().read(in);
            UpdateDetails response = getDelegate().fromJsonTree(tree);
            response.getCustomUpdateData().putString("string", "toto");
            response.getCustomUpdateData().putLong("integer", 123);
            response.getCustomUpdateData().putBoolean("boolean", true);
            response.getCustomUpdateData().putDouble("double", 3.14159265d);
            return response;
        }
    }
}
