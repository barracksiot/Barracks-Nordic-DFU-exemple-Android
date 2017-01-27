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
import android.os.Parcelable;

/**
 * This class encapsulates the details about an update package.<br>
 * It is a {@link Parcelable} which allows for it to be sent back and forth to the different
 * services with the use of an {@link android.content.Intent Intent}
 */
public class PackageInfo implements Parcelable {

    /**
     * @see Parcelable
     */
    public static final Creator<PackageInfo> CREATOR = new Creator<PackageInfo>() {
        @Override
        public PackageInfo createFromParcel(Parcel in) {
            return new PackageInfo(in);
        }

        @Override
        public PackageInfo[] newArray(int size) {
            return new PackageInfo[size];
        }
    };

    /**
     * The url to call for downloading the package.
     */
    private String url;
    /**
     * The MD5 hash of the package.
     */
    private String md5;
    /**
     * The size of the package.
     */
    private Long size;

    private PackageInfo() {

    }

    /**
     * Parcelable constructor
     *
     * @param in The parcel to read from.
     * @see Parcelable
     */
    protected PackageInfo(Parcel in) {
        url = in.readString();
        md5 = in.readString();
        size = in.readLong();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(md5);
        dest.writeLong(size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Get the url to call for downloading the package.
     *
     * @return the url to call for downloading the package.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the MD5 hash of the package.
     *
     * @return the MD5 hash of the package.
     */
    public String getMd5() {
        return md5;
    }

    /**
     * Get the size of the package.
     *
     * @return the size of the package.
     */
    public Long getSize() {
        return size;
    }
}
