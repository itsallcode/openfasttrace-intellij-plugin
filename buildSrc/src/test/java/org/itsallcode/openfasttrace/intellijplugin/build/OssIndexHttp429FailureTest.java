package org.itsallcode.openfasttrace.intellijplugin.build;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

class OssIndexHttp429FailureTest {
    @Test
    void givenWrappedOssIndexHttp429Failure_whenMatching_thenReturnsTrue() {
        final RuntimeException failure = new RuntimeException(
                "Could not audit the project: Connection to OSS Index failed, check your credentials: "
                        + "Unexpected response; status:HTTP/1.1 429 Too Many Requests",
                new RuntimeException("Unexpected response; status:HTTP/1.1 429 Too Many Requests"));

        assertThat(OssIndexHttp429Failure.matches(failure), is(true));
    }

    @Test
    void givenSeparatedOssIndexAndHttp429MessagesInCauseChain_whenMatching_thenReturnsTrue() {
        final RuntimeException failure = new RuntimeException(
                "Connection to OSS Index failed, check your credentials",
                new RuntimeException("Unexpected response; status:HTTP/1.1 429 Too Many Requests"));

        assertThat(OssIndexHttp429Failure.matches(failure), is(true));
    }

    @Test
    void givenVulnerabilityFailure_whenMatching_thenReturnsFalse() {
        final RuntimeException failure = new RuntimeException(
                "Vulnerabilities detected, check log output to review them");

        assertThat(OssIndexHttp429Failure.matches(failure), is(false));
    }

    @Test
    void givenOssIndexAuthenticationFailure_whenMatching_thenReturnsFalse() {
        final RuntimeException failure = new RuntimeException(
                "Connection to OSS Index failed, check your credentials: HTTP/1.1 401 Unauthorized");

        assertThat(OssIndexHttp429Failure.matches(failure), is(false));
    }

    @Test
    void givenOssIndexConnectionFailure_whenMatching_thenReturnsFalse() {
        final RuntimeException failure = new RuntimeException(
                "Connection to OSS Index failed, check your internet status: ossindex.sonatype.org");

        assertThat(OssIndexHttp429Failure.matches(failure), is(false));
    }

    @Test
    void givenOssIndexNon429HttpStatusFailure_whenMatching_thenReturnsFalse() {
        final RuntimeException failure = new RuntimeException(
                "Connection to OSS Index failed, check your credentials: "
                        + "Unexpected response; status:HTTP/1.1 500 Internal Server Error");

        assertThat(OssIndexHttp429Failure.matches(failure), is(false));
    }

    @Test
    void givenNonOssIndexHttp429Failure_whenMatching_thenReturnsFalse() {
        final RuntimeException failure = new RuntimeException("Unexpected response; status:HTTP/1.1 429 Too Many Requests");

        assertThat(OssIndexHttp429Failure.matches(failure), is(false));
    }

    @Test
    void givenWarningMessage_whenReading_thenIncludesQuotaAndUnavailableResultGuidance() {
        assertThat(
                OssIndexHttp429Failure.WARNING_MESSAGE,
                allOf(
                        containsString("OSS Index returned HTTP 429"),
                        containsString("plan quota"),
                        containsString("Dependency security audit result is unavailable"),
                        containsString("continuing build")));
    }
}
