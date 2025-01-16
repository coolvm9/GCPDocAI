package dev.fusion.dev.fusion.dlp;

import org.apache.commons.lang3.RandomStringUtils;

public class GCPSubmitPoll {

    public static void main(String[] args) throws  Exception{
        String projectId = "13092500545";
        String location = "us";
        String processorId = "1afab172b21cf028";
        String outputBucketName = "neat_vent_dlp_test_1";
        String outputPrefix = generateOutputPrefix("output-prefix");
        String localDirectory = "custom-output-folder";


        // Create an instance of GCPLayoutParserSubmitBatch
        GCPLayoutParserSubmitBatch parserBatch =
                new GCPLayoutParserSubmitBatch(projectId,
                        location,
                        processorId,
                        outputBucketName,
                        outputPrefix);
        String operationName = parserBatch.submitBatchProcess("gs://neat_vent_dlp_test_1/sample_ssn_test.pdf");
        // poll for the operation status
        // Create an instance of GCPPollOperation
        GCPPollOperation pollOperation = new GCPPollOperation(projectId, outputBucketName, outputPrefix);
        pollOperation.pollAndProcessResults(operationName, localDirectory);

    }

    public static String generateOutputPrefix(String basePrefix) {
        // Generate a random alphanumeric string of length 8
        String randomSuffix = RandomStringUtils.randomAlphanumeric(8);
        // Append the random string to the base prefix
        return String.format("%s-%s", basePrefix, randomSuffix);
    }
}
