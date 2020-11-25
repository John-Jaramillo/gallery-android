package edu.cnm.deepdive.gallery.service;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import edu.cnm.deepdive.gallery.model.Image;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ImageRepository {

  private static final int POOL_SIZE = 6;

  private final Context context;
  private final ContentResolver resolver;
  private final GoogleSignInService signInService;
  private final GalleryWebService webService;
  private final MediaType multipartFormType;

  public ImageRepository(Context context) {
    this.context = context;
    resolver = context.getContentResolver();
    signInService = GoogleSignInService.getInstance();
    webService = GalleryWebService.getInstance();
    multipartFormType = MediaType.parse("multipart/form-data");
  }

  public Single<List<Image>> getAll() {
    return signInService.refreshBearerToken()
        .observeOn(Schedulers.io())
        .flatMap(webService::getAllImages);
  }

  public Single<Image> add(Uri uri, String title, String description) {
    return signInService.refreshBearerToken()
        .observeOn(Schedulers.io())
        .flatMap((token) -> {
          try (
              Cursor cursor = resolver.query(uri, null, null, null, null);
              InputStream input = resolver.openInputStream(uri);
          ) {
            MediaType type = MediaType.parse(resolver.getType(uri));
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            cursor.moveToFirst();
            String filename = cursor.getString(nameIndex);
            File outputDir = context.getCacheDir();
            File outputFile = File.createTempFile("xfer", null, outputDir);
            Files.copy(input, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            RequestBody fileBody = RequestBody.create(outputFile, type);
            MultipartBody.Part filePart = MultipartBody.Part
                .createFormData("file", filename, fileBody);
            RequestBody titlePart = RequestBody.create(title, multipartFormType);
            if (description != null) {
              RequestBody descriptionPart = RequestBody.create(description, multipartFormType);
              return webService.post(token, filePart, titlePart, descriptionPart);
            } else {
              return webService.post(token, filePart, titlePart);
            }
          }
        });

  }

}

