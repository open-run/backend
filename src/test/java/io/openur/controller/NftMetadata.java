package io.openur.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class NftMetadata {
    private static final String CID = "bafybeidylx7ggx7u6pli4cdivcgapozjvqous6alhtnzpeaaheksgn35gq";
    private static final String AVATAR_ROOT = "src/test/resources/nft-avatar";
    private static final String OUTPUT_PATH = "src/test/resources/nft-metadata";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        try {
            File rootDir = new File(AVATAR_ROOT);
            generateAllMetadata(rootDir);
        } catch (IOException e) {
            System.err.println("An error occurred while generating metadata: " + e.getMessage());
        }
    }

    public static void generateAllMetadata(File dir) throws IOException {
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("invalid directory: " + dir.getAbsolutePath());
            return;
        }

        Files.createDirectories(new File(OUTPUT_PATH).toPath());

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                generateAllMetadata(file);
            } else if (file.getName().endsWith(".png") || file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg")) {
                String relativePath = dir.getAbsolutePath().replace(new File(AVATAR_ROOT).getAbsolutePath(), "")
                    .replace(File.separatorChar, '/')
                    .replaceAll("^/", "");
                String category = relativePath.split("/")[0];

                String fileName = file.getName();
                String name = fileName.substring(0, fileName.lastIndexOf('.'));
                String imagePath = String.format("ipfs://%s/%s/%s", CID, relativePath, fileName);

                ObjectNode metadata = mapper.createObjectNode();
                metadata.put("name", name);
                metadata.put("description", category + " 파츠입니다.");
                metadata.put("image", imagePath);

                ArrayNode attributes = mapper.createArrayNode();
                ObjectNode attrCategory = mapper.createObjectNode();
                attrCategory.put("trait_type", "category");
                attrCategory.put("value", category);
                attributes.add(attrCategory);

                ObjectNode attrRarity = mapper.createObjectNode();
                attrRarity.put("trait_type", "rarity");
                attrRarity.put("value", "common");// 지금은 common으로 뒀는데, 나중에 변경될 때마다 바꿔줘야함.
                attributes.add(attrRarity);

                metadata.set("attributes", attributes);

                File outputFile = new File(OUTPUT_PATH, name + ".json");
                mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, metadata);

                System.out.println("✅ " + outputFile.getName() + " 생성 완료");
            }
        }
    }
}
