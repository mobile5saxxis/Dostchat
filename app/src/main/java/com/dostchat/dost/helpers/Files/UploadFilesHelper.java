package com.dostchat.dost.helpers.Files;

import android.os.Handler;
import android.os.Looper;

import com.dostchat.dost.interfaces.UploadCallbacks;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * Created by Abderrahim El imame on 7/26/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public class UploadFilesHelper extends RequestBody {


    private File mFile;
    private byte[] arrBytes;
    private UploadCallbacks mUploadCallbacks;
    private String mimeType;
    private String mType;

    private static final int DEFAULT_BUFFER_SIZE = 2048;


    public UploadFilesHelper(final File mFile, final UploadCallbacks mUploadCallbacks, String mimeType, byte[] arrBytes, String mType) {
        this.mFile = mFile;
        this.mUploadCallbacks = mUploadCallbacks;
        this.mimeType = mimeType;
        this.mType = mType;
        this.arrBytes = arrBytes;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(mimeType);

    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength;
        FileInputStream fileInputStream;
        ByteArrayInputStream byteArrayInputStream;


        if (mFile != null && arrBytes == null) {
            fileLength = mFile.length();
            fileInputStream = new FileInputStream(mFile);
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            long uploaded = 0;

            try {
                int read;
                Handler handler = new Handler(Looper.getMainLooper());
                while ((read = fileInputStream.read(buffer)) != -1) {

                    // update progress on UI thread
                    handler.post(new Updater(uploaded, fileLength));

                    uploaded += read;
                    sink.write(buffer, 0, read);
                }
            } finally {
                fileInputStream.close();
            }
        } else {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            fileLength = arrBytes.length;
            byteArrayInputStream = new ByteArrayInputStream(arrBytes);
            long uploaded = 0;

            try {
                int read;
                Handler handler = new Handler(Looper.getMainLooper());
                while ((read = byteArrayInputStream.read(buffer)) != -1) {

                    // update progress on UI thread
                    handler.post(new Updater(uploaded, fileLength));

                    uploaded += read;
                    sink.write(buffer, 0, read);
                }
            } finally {
                byteArrayInputStream.close();
            }
        }


    }

    private class Updater implements Runnable {
        private long mUploaded;
        private long mTotal;

        public Updater(long uploaded, long total) {
            mUploaded = uploaded;
            mTotal = total;
        }

        @Override
        public void run() {
            mUploadCallbacks.onUpdate((int) (100 * mUploaded / mTotal), mType);
        }
    }

}
