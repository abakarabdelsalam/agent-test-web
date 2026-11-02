package dev.abakar.agenttest.claude;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ClaudeReplyTest {

    private final ClaudeReply reply = new ClaudeReply("tool_use", List.of(
            Map.of("type", "thinking", "thinking", "", "signature", "abc"),
            Map.of("type", "text", "text", "J'ouvre le site."),
            Map.of("type", "tool_use", "id", "toolu_1", "name", "ouvrir",
                    "input", Map.of("url", "http://localhost:4200"))));

    @Test
    void extractsToolCalls() {
        ClaudeReply.ToolCall call = reply.toolCalls().getFirst();

        assertEquals("toolu_1", call.id());
        assertEquals("ouvrir", call.name());
        assertEquals("http://localhost:4200", call.input().get("url"));
    }

    @Test
    void keepsOnlyTextForText() {
        assertEquals("J'ouvre le site.", reply.text());
    }

    @Test
    void keepsEveryBlockToSendBack() {
        assertEquals(3, reply.content().size());
    }
}
