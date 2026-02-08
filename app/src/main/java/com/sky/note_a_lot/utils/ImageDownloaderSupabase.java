package com.sky.note_a_lot.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ImageDownloaderSupabase {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public static String downloadAndSaveImage(Context context, String publicSupabaseImageUrl) {

        Request request = new Request.Builder()
                .url(publicSupabaseImageUrl)
                .get()
                .build();

        Response response = null;
        ResponseBody body = null;
        File imageFile = null;
        FileOutputStream outputStream = null;
        InputStream inputStream = null;

        try {
            response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                return null;
            }

            body = response.body();
            if (body == null) return null;

            inputStream = body.byteStream();

            File dcimDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DCIM), "");

            if (!dcimDir.exists()) {
                dcimDir.mkdirs();
            }

            String fileName = generateImageName();
            imageFile = new File(dcimDir, fileName);

            outputStream = new FileOutputStream(imageFile);

            byte[] buffer = new byte[4096];
            int read;

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            outputStream.flush();

            return imageFile.getAbsolutePath();

        } catch (Exception e) {
            if (imageFile != null && imageFile.exists()) {
                imageFile.delete();
            }
            e.printStackTrace();
            return null;

        } finally {
            try {
                if (response != null) response.close();
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String generateImageName() {
        String timestamp = new SimpleDateFormat(
                "ddMMyyyy_HHmmssSSS",
                Locale.getDefault()
        ).format(new Date());

        return "IMG_" + timestamp + ".jpg";
    }
}
