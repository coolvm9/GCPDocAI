package dev.fusion.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class JSONParser {

    public static void main(String[] args) throws IOException {
        // Path to your JSON file
        File file = new File(JSONParser.class.getClassLoader().getResource("results/layoutparser.json").getFile());
        if (file == null || !file.exists()) {
            System.out.println("File not found: results/layoutparser.json");
            return;
        }

        // Load JSON file into an ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(new File(file.toURI()));

        // Access 'documentLayout' -> 'blocks'
        JsonNode blocksNode = rootNode.path("documentLayout").path("blocks");
        if (blocksNode.isMissingNode()) {
            System.out.println("No blocks found under documentLayout");
            return;
        }

        // Iterate over each block, including nested ones
        iterateBlocks(blocksNode, 0);
    }

    // Recursive method to iterate over blocks and handle nested blocks
    private static void iterateBlocks(JsonNode blocksNode, int depth) {
        for (JsonNode blockNode : blocksNode) {
            // Print indentation based on the block depth to show hierarchy
            String indent = "  ".repeat(depth);

            // Extract block details
            String blockId = blockNode.path("blockId").asText();
            String text = blockNode.path("textBlock").path("text").asText();
            String type = blockNode.path("textBlock").path("type").asText();
            int pageStart = blockNode.path("pageSpan").path("pageStart").asInt();
            int pageEnd = blockNode.path("pageSpan").path("pageEnd").asInt();

            // Print block details with indentation for hierarchical view
            System.out.println(indent + "Block ID: " + blockId);
            System.out.println(indent + "Text: " + text);
            System.out.println(indent + "Type: " + type);
            System.out.println(indent + "Page Start: " + pageStart);
            System.out.println(indent + "Page End: " + pageEnd);
            System.out.println();

            // Check if this block has nested blocks
            if (blockNode.path("textBlock").has("blocks")) {
                System.out.println(indent + "Nested blocks found in Block ID: " + blockId);
                iterateBlocks(blockNode.path("textBlock").path("blocks"), depth + 1);
            }

            // Handle table blocks if present
            if (blockNode.has("tableBlock")) {
                JsonNode tableBlock = blockNode.path("tableBlock");
                System.out.println(indent + "Table Block in Block ID: " + blockId);
                iterateTableBlock(tableBlock, depth + 1);
            }
        }
    }

    // Method to iterate over table blocks
    private static void iterateTableBlock(JsonNode tableBlockNode, int depth) {
        String indent = "  ".repeat(depth);
        JsonNode bodyRows = tableBlockNode.path("bodyRows");

        for (JsonNode row : bodyRows) {
            System.out.println(indent + "Row:");
            for (JsonNode cell : row.path("cells")) {
                for (JsonNode cellBlock : cell.path("blocks")) {
                    String cellBlockId = cellBlock.path("blockId").asText();
                    String cellText = cellBlock.path("textBlock").path("text").asText();
                    System.out.println(indent + "  Cell Block ID: " + cellBlockId);
                    System.out.println(indent + "  Cell Text: " + cellText);
                }
            }
        }
    }
}