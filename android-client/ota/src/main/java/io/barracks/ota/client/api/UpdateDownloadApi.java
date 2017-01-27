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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * This interface describes the call to the Barracks platform which downloads a package.
 */
public interface UpdateDownloadApi {
    /**
     * The call to the Barracks platform which downloads a package.
     *
     * @param url The url of the package.
     * @param key The API key provided by the Barracks platform.
     * @return A {@link Call} to execute in order to download the package.
     */
    @GET()
    @Streaming
    Call<ResponseBody> downloadUpdate(@Url String url, @Header("Authorization") String key);
}
