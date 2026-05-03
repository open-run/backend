package io.openur.domain.user.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserProfileImageStorageService {

    private static final byte[] PNG_SIGNATURE = new byte[] {
        (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    private final Path localAssetRoot;
    private final long maxImageBytes;

    public UserProfileImageStorageService(
        @Value("${openrun.nft.assets.local-root:}") String localAssetRoot,
        @Value("${openrun.users.profile-image.max-size-bytes:2097152}") long maxImageBytes
    ) {
        this.localAssetRoot = StringUtils.hasText(localAssetRoot)
            ? Path.of(localAssetRoot).toAbsolutePath().normalize()
            : null;
        this.maxImageBytes = maxImageBytes;
    }

    public String store(String userId, MultipartFile image) {
        byte[] bytes = validateAndRead(image);
        String storageKey = "profile-images/users/" + userId + "/profile.png";

        if (localAssetRoot == null) {
            throw new IllegalStateException("Local asset root is not configured");
        }

        Path target = localAssetRoot.resolve(storageKey).normalize();
        if (!target.startsWith(localAssetRoot)) {
            throw new IllegalArgumentException("Invalid profile image path");
        }

        try {
            Files.createDirectories(target.getParent());
            Files.write(target, bytes);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store profile image", e);
        }

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
