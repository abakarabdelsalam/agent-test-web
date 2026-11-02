package dev.abakar.agenttest.agent;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Ce que Java renvoie à Claude après une action : du texte, éventuellement une image. */
public record ToolResult(String text, byte[] image, boolean error) {

    public static ToolResult ok(String text) {
        return new ToolResult(text, null, false);
    }

    public static ToolResult error(String text) {
        return new ToolResult(text, null, true);
    }

    public static ToolResult image(byte[] png) {
        return new ToolResult("Capture d'écran de " + png.length / 1024 + " Ko.", png, false);
    }

    /** Bloc tool_result de l'API : il reprend l'id de la demande pour que Claude relie les deux. */
    public Map<String, Object> toBlock(String toolUseId) {
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("type", "tool_result");
        block.put("tool_use_id", toolUseId);
        if (image == null) {
            block.put("content", text);
        } else {
            block.put("content", List.of(
                    Map.of("type", "image", "source", Map.of(
                            "type", "base64",
                            "media_type", "image/png",
                            "data", Base64.getEncoder().encodeToString(image))),
                    Map.of("type", "text", "text", text)));
        }
        if (error) {
            block.put("is_error", true);
        }
        return block;
    }
}
