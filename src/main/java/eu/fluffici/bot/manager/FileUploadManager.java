/*
---------------------------------------------------------------------------------
File Name : FileUploadManager

Developer : vakea 
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 15/06/2024
Last Modified : 15/06/2024

---------------------------------------------------------------------------------
*/

package eu.fluffici.bot.manager;

import eu.fluffici.bot.api.hooks.FIleUploadCallback;
import eu.fluffici.bot.api.hooks.FileBuilder;
import eu.fluffici.bot.api.hooks.IFileUploader;
import lombok.SneakyThrows;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("All")
public class FileUploadManager implements IFileUploader  {
    private final int DEFAULT_BUFFER_SIZE = 4096;
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Uploads a file to a specified destination using the given file builder and callback.
     *
     * @param file     the file builder object representing the file to be uploaded
     * @param callback the callback object to handle the status of the upload operation
     * @throws IOException if an I/O error occurs during the upload process
     * @throws RuntimeException if any other exception occurs during the upload process
     */
    @Override
    public void uploadFile(FileBuilder file, FIleUploadCallback callback) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (InputStream inputStream = file.getData()) {
                byte[] buffer = new byte[this.getBufferSize(inputStream)];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                byte[] fileBytes = outputStream.toByteArray();

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("filename", file.getFileName(),
                                RequestBody.create(MediaType.parse("application/octet-stream"), fileBytes))
                        .build();

                Request request = new Request.Builder()
                        .url("https://autumn.fluffici.eu/".concat(file.getBucketName()))
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful())
                    callback.done(response);
                else
                    callback.error(response);
            }
        }
    }

    /**
     * Calculates an appropriate buffer size based on the size of the given InputStream.
     *
     * @param inputStream the InputStream to calculate the buffer size for
     * @return the calculated buffer size
     * @throws IOException if an I/O error occurs
     */
    private int getBufferSize(@NotNull InputStream inputStream) throws IOException {
        int size = inputStream.available();
        return Math.max(Math.min(size, DEFAULT_BUFFER_SIZE), 1024);
    }
}