/*
 * Copyright (C) 2020 Luke Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.klinker.messenger.api.implementation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helper for working with binary data.
 */
public class BinaryUtils {

    public static byte[] getMediaBytes(Context context, String uri, String mimeType, boolean scale) {
        byte[] bytes;
        if (mimeType.startsWith("image/") && !mimeType.equals("image/gif")) {
            try {
                Bitmap bitmap = scale ?
                        ImageScaler.scaleToSend(context, Uri.parse(uri)) :
                        MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(uri));

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(mimeType.equals("image/png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 60, baos);
                bytes = baos.toByteArray();

                bitmap.recycle();
            } catch (Exception | OutOfMemoryError e) {
                e.printStackTrace();
                bytes = new byte[0];
            }
        } else {
            try {
                InputStream stream = context.getContentResolver()
                        .openInputStream(Uri.parse(uri));
                bytes = readBytes(stream);
                stream.close();
            } catch (Exception | OutOfMemoryError e) {
                e.printStackTrace();
                bytes = new byte[0];
            }
        }

        return bytes;
    }

    private static byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        return byteBuffer.toByteArray();
    }

}
