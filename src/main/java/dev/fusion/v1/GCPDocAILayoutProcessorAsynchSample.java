package dev.fusion.v1;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.documentai.v1.*;
import com.google.protobuf.ByteString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class GCPDocAILayoutProcessorAsynchSample {

    public static void main(String[] args) throws IOException {
        String projectId = "13092500545";
        String location = "us";
        String processorId = "1afab172b21cf028";

        File file = new File(GCPDocAILayoutProcessorAsynchSample.class.getClassLoader().getResource("sample-layout.pdf").getFile());
        Path filePath = file.toPath();
        System.out.println("File Path: " + filePath);

        ByteString pdfBytes = ByteString.readFrom(new FileInputStream(file));

        Document document = processDocument(pdfBytes, projectId, location, processorId);
        System.out.println("Document Text: " + document.getDocumentLayout());


    }


    public static Document processDocument(ByteString pdfBytes, String projectId, String location, String processorId) throws IOException {
        DocumentProcessorServiceSettings settings = DocumentProcessorServiceSettings.newBuilder().build();
        try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create(settings)) {
            ProcessorName processorName = ProcessorName.of(projectId, location, processorId);

                ProcessOptions processOptions = ProcessOptions.newBuilder()
                    .setLayoutConfig(ProcessOptions.LayoutConfig.newBuilder()
                            .setChunkingConfig(ProcessOptions.LayoutConfig.ChunkingConfig.newBuilder()
                                    .setChunkSize(50)
                                    .setIncludeAncestorHeadings(true)
                                    .build())
                            .build())
                    .build();

            ProcessRequest request = ProcessRequest.newBuilder()
                    .setName(processorName.toString())
                    .setProcessOptions(processOptions)
                    .setRawDocument(RawDocument.newBuilder()
                            .setContent(pdfBytes)
                            .setMimeType("application/pdf")
                            .build())
                    .build();
            ProcessResponse response = client.processDocument(request);
//            OperationFuture<ProcessResponse, ?> future = client.batchProcessDocumentsAsync(request);


            return response.getDocument();
        }
    }
}