package com.devingotaswitch.s3;

import android.content.Context;
import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.devingotaswitch.youruserpools.CIBHelper;

import java.io.File;

public class S3Helper {
    private static String TAG = "S3Helper";

    private AmazonS3 s3;
    private TransferUtility transferUtility;

    private static final String RANKINGS_BUCKET = "rankings-bucket";
    private static final String RANKINGS_KEY = "rankings.json";
    private static final String FILE_NAME = "rankings.json";
    public S3Helper(Context context) {
        s3 = new AmazonS3Client(CIBHelper.getCredentialsProvider());

        transferUtility = new TransferUtility(s3, context);
    }

    public void updateRankings(Context context) {
        if (s3 == null) {
            Log.d(TAG, "Can't refresh rankings, s3 client not initialized");
        }
        File file = new File(context.getFilesDir(), FILE_NAME);
        TransferObserver observer = transferUtility.download(
                RANKINGS_BUCKET,
                RANKINGS_KEY,
                file
        );

        observer.setTransferListener(new TransferListener(){

            @Override
            public void onStateChanged(int id, TransferState state) {
                //TODO: once done, send intent to new activity
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int percentage = (int) (bytesCurrent/bytesTotal * 100);
                //TODO: Display percentage transfered to user
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e(TAG, "Failed to refresh rankings: " + ex.getMessage());
            }
        });
    }
}
