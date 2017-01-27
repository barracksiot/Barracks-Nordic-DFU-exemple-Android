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

package io.barracks.ota.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.barracks.ota.client.api.UpdateDetails;
import io.barracks.ota.client.api.UpdateDetailsRequest;
import io.barracks.ota.client.helper.BarracksHelper;
import io.barracks.ota.client.helper.PackageDownloadCallback;
import io.barracks.ota.client.helper.PackageDownloadHelper;
import io.barracks.ota.client.helper.UpdateCheckCallback;
import io.barracks.ota.client.helper.UpdateCheckHelper;

public class CheckUpdateActivity extends AppCompatActivity {

    private static final String TAG = CheckUpdateActivity.class.getSimpleName();
    private UpdateCheckHelper updateCheckHelper;
    private PackageDownloadHelper packageDownloadHelper;
    private Button check, download;
    private EditText version;
    private TextView details;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_update);
        check = (Button) findViewById(R.id.btn_check);
        download = (Button) findViewById(R.id.btn_download);
        version = (EditText) findViewById(R.id.version);
        details = (TextView) findViewById(R.id.package_details);
        progressBar = (ProgressBar) findViewById(R.id.progress);
    }

    @Override
    protected void onResume() {
        super.onResume();

        BarracksHelper helper = new BarracksHelper("deadbeef", "https://app.barracks.io/");

        updateCheckHelper = helper.getUpdateCheckHelper();
        updateCheckHelper.bind(this, new UpdateCheckCallback() {
            @Override
            public void onUpdateAvailable(UpdateDetailsRequest request, UpdateDetails response) {
                details.setText(getString(
                        R.string.update_description,
                        response.getVersionId(),
                        Formatter.formatFileSize(CheckUpdateActivity.this, response.getPackageInfo().getSize()))
                );
                details.setTag(response);
            }

            @Override
            public void onUpdateUnavailable(UpdateDetailsRequest request) {
                details.setText(getString(R.string.update_unavailable));
                details.setTag(null);
            }

            @Override
            public void onUpdateRequestError(UpdateDetailsRequest request, Throwable t) {
                details.setText(getString(R.string.update_check_error, t.getMessage()));
                details.setTag(null);
            }
        });

        packageDownloadHelper = helper.getPackageDownloadHelper();
        packageDownloadHelper.bind(this, new PackageDownloadCallback() {
            @Override
            public void onDownloadSuccess(UpdateDetails details, String path) {
                progressBar.setProgress(0);
            }

            @Override
            public void onDownloadFailure(UpdateDetails details, Throwable throwable) {
                progressBar.setProgress(0);
            }

            @Override
            public void onDownloadProgress(UpdateDetails details, int progress) {
                progressBar.setProgress(progress);
            }
        });

        check.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            updateCheckHelper.requestUpdate(
                                    new UpdateDetailsRequest.Builder()
                                            .versionId(version.getText().toString())
                                            .unitId("moneypenny")
                                            .build()
                            );
                        } catch (Exception e) {
                            details.setText(getString(R.string.update_check_error, e.getMessage()));
                        }
                    }
                }
        );

        download.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        packageDownloadHelper.requestDownload((UpdateDetails) details.getTag());
                    }
                }
        );


    }

    @Override
    protected void onPause() {
        super.onPause();
        updateCheckHelper.unbind(this);
        packageDownloadHelper.unbind(this);
    }
}
