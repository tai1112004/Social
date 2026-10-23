package com.taihoang.social_backend;

import com.taihoang.social_backend.dto.CallSignalRequest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CallSignalRequestJsonTest {
    @Test
    void deserializesSdpPayloadWithTheJacksonVersionUsedBySpringWebSocket() throws Exception {
        String json = "{\"callId\":\"call-1\",\"payload\":{\"type\":\"offer\",\"sdp\":\"v=0\\r\\n\"}}";

        CallSignalRequest request = new JsonMapper().readValue(json, CallSignalRequest.class);

        assertEquals("call-1", request.callId());
        assertNotNull(request.payload());
        assertEquals("offer", request.payload().get("type").asString());
        assertEquals("v=0\r\n", request.payload().get("sdp").asString());
    }
}
