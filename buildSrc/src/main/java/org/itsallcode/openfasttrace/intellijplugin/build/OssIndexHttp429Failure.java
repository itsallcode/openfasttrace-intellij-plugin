package org.itsallcode.openfasttrace.intellijplugin.build;

import java.util.regex.Pattern;

/**
 * Detects OSS Index audit failures caused by HTTP 429 quota or rate limiting.
 */
public final class OssIndexHttp429Failure {
    public static final String WARNING_MESSAGE = "OSS Index returned HTTP 429. The configured OSS Index plan quota may "
            + "be exceeded. Check the OSS Index plan and quota. Dependency security audit result is unavailable for "
            + "this run; continuing build.";

    private static final Pattern OSS_INDEX = Pattern.compile("(?i)\\boss\\s*index\\b");
    private static final Pattern HTTP_429 = Pattern.compile(
            "(?i)(\\bhttp/\\d+(?:\\.\\d+)?\\s+429\\b|\\bstatus\\s*:?\\s*[^\\r\\n]*\\b429\\b|\\b429\\s+too many requests\\b)");

    private OssIndexHttp429Failure() {
        // Utility class.
    }

    /**
     * Check whether a failure chain describes an OSS Index HTTP 429 response.
     *
     * @param failure exception thrown by the OSS Index audit task
     * @return {@code true} if the chain mentions OSS Index and an HTTP 429 status
     */
    public static boolean matches(final Throwable failure) {
        boolean mentionsOssIndex = false;
        boolean mentionsHttp429 = false;
        Throwable current = failure;
        while (current != null) {
            final String message = current.getMessage();
            mentionsOssIndex |= containsOssIndex(message);
            mentionsHttp429 |= containsHttp429(message);
            current = current.getCause();
        }
        return mentionsOssIndex && mentionsHttp429;
    }

    private static boolean containsOssIndex(final String message) {
        return message != null && OSS_INDEX.matcher(message).find();
    }

    private static boolean containsHttp429(final String message) {
        return message != null && HTTP_429.matcher(message).find();
    }
}
