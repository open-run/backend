package io.openur.domain.user.service;

import io.openur.global.storage.GcsStorageService;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserProfileImageStorageService {

    private static final byte[] PNG_SIGNATURE = new byte[] {
        (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    private final GcsStorageService gcsStorageService;
    private final long maxImageBytes;

    public UserProfileImageStorageService(
        GcsStorageService gcsStorageService,
        @Value("${openrun.users.profile-image.max-size-bytes:2097152}") long maxImageBytes
    ) {
        this.gcsStorageService = gcsStorageService;
        this.maxImageBytes = maxImageBytes;
    }

    public String store(String userId, MultipartFile image) {
        byte[] bytes = validateAndRead(image);
        String storageKey = "profile-images/users/" + userId + "/profile.png";
        gcsStorageService.upload(storageKey, bytes, MediaType.IMAGE_PNG_VALUE);
        return storageKey;
    }

    private byte[] validateAndRead(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Profile image is required");
        }

        if (!MediaType.IMAGE_PNG_VALUE.equals(image.getContentType())) {
            throw new IllegalArgumentException("Only PNG profile images are allowed");
        }

        if (image.getSize() > maxImageBytes) {
            throw new IllegalArgumentException("Profile image is too large");
        }

        try {
            byte[] bytes = image.getBytes();
            if (!hasPngSignature(bytes)) {
                throw new IllegalArgumentException("Only PNG profile images are allowed");
            }
            return bytes;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read profile image", e);
        }
    }

    private boolean hasPngSignature(byte[] bytes) {
        if (bytes.length < PNG_SIGNATURE.length) {
            return false;
        }

        for (int i = 0; i < PNG_SIGNATURE.length; i++) {
            if (bytes[i] != PNG_SIGNATURE[i]) {
                return false;
            }
        }

        return true;
    }
}
