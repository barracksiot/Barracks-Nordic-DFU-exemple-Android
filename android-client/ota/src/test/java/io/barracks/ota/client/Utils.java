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

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Set;

import io.barracks.ota.client.api.UpdateDetails;

/**
 * Created by saiimons on 16-04-12.
 */
public class Utils {
    public static boolean compareBundles(Bundle one, Bundle two) {
        if (one.size() != two.size())
            return false;

        Set<String> setOne = one.keySet();
        Object valueOne;
        Object valueTwo;

        for (String key : setOne) {
            valueOne = one.get(key);
            valueTwo = two.get(key);
            if (valueOne instanceof Bundle && valueTwo instanceof Bundle && !compareBundles((Bundle) valueOne, (Bundle) valueTwo)) {
                return false;
            } else if (valueOne == null) {
                if (valueTwo != null || !two.containsKey(key))
                    return false;
            } else if (!valueOne.equals(valueTwo)) {
                return false;
            }
        }

        return true;
    }

    public static Gson getRobolectricGson(GsonBuilder builder) {
        return builder
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
                ).create();
    }

    public static UpdateDetails getUpdateDetailsFromFile(String filename) throws FileNotFoundException {
        UpdateCheckService checkService = new UpdateCheckService();
        Gson gson = Utils.getRobolectricGson(checkService.setUpGsonBuilder(new GsonBuilder()));
        File f = new File(ClassLoader.getSystemResource(filename).getPath());
        return gson.fromJson(new FileReader(f), UpdateDetails.class);
    }
}
