package dev.abakar.agenttest.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

class RetryPolicyTest {

    private static final RetryPolicy POLICY = new RetryPolicy(3, Duration.ZERO);

    @Test
    void retriesAPassingErrorUntilItWorks() {
        int[] calls = {0};

        String result = POLICY.run(() -> {
            if (++calls[0] < 3) {
                throw new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE);
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(3, calls[0]);
    }

    @Test
    void givesUpAfterTheLastAttempt() {
        int[] calls = {0};

        assertThrows(HttpServerErrorException.class, () -> POLICY.run(() -> {
            calls[0]++;
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        }));
        assertEquals(3, calls[0]);
    }

    @Test
    void doesNotRetryAnErrorCausedByTheRequest() {
        int[] calls = {0};

        assertThrows(HttpClientErrorException.class, () -> POLICY.run(() -> {
            calls[0]++;
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        }));
        assertEquals(1, calls[0]);
    }
}
