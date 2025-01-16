package dev.fusion.dev.fusion.dlp;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.paging.Page;
import com.google.cloud.documentai.v1.BatchProcessMetadata;
import com.google.cloud.documentai.v1.BatchProcessResponse;
import com.google.cloud.documentai.v1.DocumentProcessorServiceClient;
import com.google.cloud.storage.*;
import java.io.File;
import java.io.IOException;

public class GCPPollOperation {
    private final String projectId;
    private final String outputBucketName;
    private final String outputPrefix;

    public GCPPollOperation(String projectId, String outputBucketName, String outputPrefix) {
        this.projectId = projectId;
        this.outputBucketName = outputBucketName;
        this.outputPrefix = outputPrefix;
    }

    // Polls the operation and processes the results
    public void pollAndProcessResults(String operationName, String localDirectory)
            throws IOException, InterruptedException {
        try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create()) {
            System.out.println("Polling for operation status...");

            // Poll operation status until it is done
            while (true) {
                // Fetch the operation by its name
                var operation = client.getOperationsClient().getOperation(operationName);

                if (operation.getDone()) {
                    System.out.println("Operation completed.");
                    if (operation.hasResponse()) {
                        System.out.println("Processing output files...");
                        processOutputFiles(localDirectory);
                    } else if (operation.hasError()) {
                        System.err.println("Operation failed with error: " + operation.getError().getMessage());
                    }
                    break;
                }

                // Wait before polling again
                System.out.println("Operation still in progress, waiting 10 seconds...");
                Thread.sleep(10_000);
            }
        }
    }

    // Processes output files from the GCS bucket and saves them locally
    private void processOutputFiles(String localDirectory) throws IOException {
        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
        Bucket bucket = storage.get(outputBucketName);

        // Ensure the local directory exists
        File directory = new File(localDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // List all files in the GCS bucket
        Page<Blob> blobs = bucket.list(Storage.BlobListOption.prefix(outputPrefix + "/"));
        int idx = 0;
        for (Blob blob : blobs.iterateAll()) {
            if (!blob.isDirectory()) {
                System.out.printf("Fetched file #%d: %s\n", ++idx, blob.getName());

                // Save the file locally
                File localFile = new File(directory, blob.getName().replace(outputPrefix + "/", ""));
                blob.downloadTo(localFile.toPath());
                System.out.println("Saved file to: " + localFile.getAbsolutePath());
            }
        }
    }
}