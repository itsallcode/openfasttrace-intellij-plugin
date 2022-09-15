package org.itallcode.openfasttrace.intelijplugin.wait;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeoutException;

import static org.itallcode.openfasttrace.intelijplugin.remoterobot.RemoteRobotProperties.*;

/**
 * This strategy waits for the Remote Robot server to become available.
 */
public class RobotServerReadyWaitStrategy {
    private static final long RETRY_DELAY_MILLIS = 1000;
    private final Duration timeout;

    /**
     * Wait for the Remote Robot server to become available.
     *
     * @param timeout maximum time to wait for the server to become available
     * @throws TimeoutException if the given timeout is reached without being able to connect to the robot server
     */
    public static void wait(final Duration timeout) throws TimeoutException {
        final RobotServerReadyWaitStrategy strategy = new RobotServerReadyWaitStrategy(timeout);
        strategy.waitUntilReady();
    }

    private RobotServerReadyWaitStrategy(final Duration timeout) {
        this.timeout = timeout;
    }

    private void waitUntilReady() throws TimeoutException {
        final OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(timeout)
                .retryOnConnectionFailure(true)
                .build();
        poll(client, ROBOT_BASE_URL);
        client.dispatcher().executorService().shutdown();
    }

    private void poll(final OkHttpClient client, final String url) throws TimeoutException {
        final Request request = new Request.Builder().url(url).build();
        final Instant until = Instant.now().plus(this.timeout);
        do {
            try (final Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    return;
                }
            } catch (IOException exception) {
                // keep trying.
                try {
                    delayPollingRetry();
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(interruptedException);
                }
            }
        } while (Instant.now().isBefore(until));
        throw new TimeoutException("Timed out waiting for Remote Robot server to become available");
    }

    @SuppressWarnings("java:S2925")
    private static void delayPollingRetry() throws InterruptedException {
        Thread.sleep(RETRY_DELAY_MILLIS);
    }
}
