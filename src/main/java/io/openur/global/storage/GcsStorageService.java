package io.openur.global.storage;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GcsStorageService {

    private final Storage storage;
    private final String bucket;

    public GcsStorageService(
        Storage storage,
        @Value("${openrun.gcs.bucket}") String bucket
    ) {
        this.storage = storage;
        this.bucket = bucket;
    }

    public void upload(String storageKey, byte[] bytes, String contentType) {
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucket, storageKey))
            .setContentType(contentType)
            .setCacheControl("public, max-age=31536000")
            .build();
        storage.create(blobInfo, bytes);
    }
}
