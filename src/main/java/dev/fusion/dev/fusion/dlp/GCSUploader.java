package dev.fusion.dev.fusion.dlp;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class GCSUploader {
    private  static String bucketName ="neat_vent_dlp_test_1";
    private  static Storage storage;
    private  static String projectId = "13092500545";

    public GCSUploader(String bucketName, String projectId) {
        this.bucketName = bucketName;
        this.storage = StorageOptions.newBuilder()
                .setProjectId(projectId)
                .build()
                .getService();
    }

    public String uploadFile(String objectName, String resourceFileName) {
        try {
            // Load the file from the resources directory
            Path filePath = Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource(resourceFileName)).toURI());

            // Read the file's content
            byte[] fileContent = Files.readAllBytes(filePath);

            // Specify the GCS blob (object) details
            BlobId blobId = BlobId.of(bucketName, objectName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

            // Upload the file to the GCS bucket
            storage.create(blobInfo, fileContent);
            String fileUri = String.format("gs://%s/%s", bucketName, objectName);
            System.out.println("File uploaded to: " + fileUri);

            return fileUri; // Return the GCS URI
        } catch (IOException | NullPointerException | IllegalArgumentException | URISyntaxException e) {
            System.err.println("Error uploading file to GCS: " + e.getMessage());
            return null; // Return null in case of an error
        }
    }


    public static void main(String[] args) {
        // Create an instance of GCSUploader
        GCSUploader uploader = new GCSUploader(bucketName, projectId);

        // Replace with the desired object name in the bucket
        String objectName = "uploaded-file.pdf";

        // Replace with the name of the file in the resources directory
        String resourceFileName = "sample_ssn_test.pdf";

        // Upload the file and get the URI
        String fileUri = uploader.uploadFile(objectName, resourceFileName);
        if (fileUri != null) {
            System.out.println("Uploaded file is accessible at: " + fileUri);
        } else {
            System.out.println("File upload failed.");
        }
    }


}
