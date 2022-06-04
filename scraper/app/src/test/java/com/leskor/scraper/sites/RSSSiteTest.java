package com.leskor.scraper.sites;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leskor.scraper.dto.ReadabilityResponse;
import com.leskor.scraper.entities.Post;
import com.leskor.scraper.entities.Topic;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import javax.net.ssl.SSLSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RSSSiteTest {
    private static final URI INDEX_PAGE_URI = URI.create("https://digestwave.com");
    private static final URI READABILITY_PAGE_URI = URI.create("https://readability.digestwave.com");
    private static final String SITE_CODE = "DWV";
    private static final Duration INDEX_PAGE_TIMEOUT_DURATION = Duration.ofSeconds(1);
    private static final String TITLE_SUFFIX_TO_TRIM = "| Digestwave";

    private HttpClient httpClient;

    private RSSSite rssSite;

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpClient.class);
        rssSite = new RSSSite(
                INDEX_PAGE_URI,
                READABILITY_PAGE_URI,
                SITE_CODE,
                Topic.TECH,
                httpClient,
                INDEX_PAGE_TIMEOUT_DURATION,
                TITLE_SUFFIX_TO_TRIM,
                Set.of("Ad"),
                Set.of("Video-post")
        );
    }

    @Test
    void fetchPosts() throws Exception {
        var indexPageResponse = """
                <xml>
                    <item>
                        <link>https://digestwave.com/post/21</link>
                        <pubDate>Tue, 14 Dec 2021 20:25:30 GMT</pubDate>
                        <category><![CDATA[Ad]]></category>
                        <category><![CDATA[Interview]]></category>
                    </item>
                    <item>
                        <link>MALFORMED LINK</link>
                        <pubDate>Tue, 14 Dec 2021 12:05:30 GMT</pubDate>
                        <category><![CDATA[Review]]></category>
                        <category><![CDATA[Laptops]]></category>
                    </item>
                    <item>
                        <link>https://digestwave.com/post/42</link>
                        <pubDate>Tue, 14 Dec 2021 11:05:30 GMT</pubDate>
                        <category><![CDATA[Review]]></category>
                        <category><![CDATA[Laptops]]></category>
                    </item>
                    <item>
                        <link>https://digestwave.com/post/69</link>
                        <pubDate>MALFORMED DATE</pubDate>
                        <category><![CDATA[Review]]></category>
                        <category><![CDATA[Laptops]]></category>
                    </item>
                </xml>
                """;

        var readabilityResponse = new ReadabilityResponse(
                "Nice article | Digestwave",
                "<h1>Hello</h1><p>Long story short</p>",
                "Hello. Long story short",
                512,
                ""
        );

        when(httpClient.sendAsync(argThat(r -> r != null && r.uri().equals(INDEX_PAGE_URI)), notNull()))
                .thenReturn(completedFuture(createHttpResponseWithBody(indexPageResponse)));

        when(httpClient.sendAsync(
                argThat(r -> r != null
                        && r.uri().equals(READABILITY_PAGE_URI)
                        && r.bodyPublisher().isPresent()
                ),
                notNull()
        )).thenReturn(completedFuture(createHttpResponseWithBody(readabilityResponse)));

        CompletableFuture<List<Post>> resultFuture = rssSite.fetchPosts();
        List<Post> result = resultFuture.get(2, SECONDS);

        var expectedPost = new Post(
                SITE_CODE,
                ZonedDateTime.of(2021, 12, 14, 11, 5, 30, 0, ZoneId.of("Z")),
                "Nice article",
                "Hello. Long story short",
                "838002001",
                "https://digestwave.com/post/42",
                null,
                Topic.TECH
        );

        assertFalse(result.isEmpty(), "List of posts is empty");
        assertEquals(List.of(expectedPost), result, "List of post is not what we expected");
    }

    @Test
    void fetchPostsIgnoresPostIfTitleContainsExcludedString() throws Exception {
        var indexPageResponse = """
                <xml>
                    <item>
                        <link>https://digestwave.com/post/42</link>
                        <pubDate>Tue, 14 Dec 2021 11:05:30 GMT</pubDate>
                        <category><![CDATA[Review]]></category>
                        <category><![CDATA[Laptops]]></category>
                    </item>
                </xml>
                """;

        var readabilityResponse = new ReadabilityResponse(
                "Nice video-post",
                "<h1>Hello</h1><p>Long story short</p>",
                "Hello. Long story short",
                512,
                ""
        );

        when(httpClient.sendAsync(argThat(r -> r != null && r.uri().equals(INDEX_PAGE_URI)), notNull()))
                .thenReturn(completedFuture(createHttpResponseWithBody(indexPageResponse)));

        when(httpClient.sendAsync(
                argThat(r -> r != null
                        && r.uri().equals(READABILITY_PAGE_URI)
                        && r.bodyPublisher().isPresent()
                ),
                notNull()
        )).thenReturn(completedFuture(createHttpResponseWithBody(readabilityResponse)));

        CompletableFuture<List<Post>> resultFuture = rssSite.fetchPosts();
        List<Post> result = resultFuture.get(2, SECONDS);
        assertTrue(result.isEmpty(), "List of posts is not empty");
    }

    private static Stream<Arguments> bodyAndStatusForUnavailablePage() {
        return Stream.of(
                Arguments.of("xml", 500),
                Arguments.of(null, 404),
                Arguments.of(null, 200),
                Arguments.of("", 200),
                Arguments.of("lol", 200)
        );
    }

    @ParameterizedTest
    @MethodSource("bodyAndStatusForUnavailablePage")
    void fetchPostsReturnsEmptyListWhenIndexPageIsNotAvailable(String body, int status) throws Exception {
        when(httpClient.sendAsync(argThat(r -> r != null && r.uri().equals(INDEX_PAGE_URI)), notNull()))
                .thenReturn(completedFuture(createHttpResponseWithBodyAndStatus(body, status)));

        CompletableFuture<List<Post>> resultFuture = rssSite.fetchPosts();
        List<Post> result = resultFuture.get(2, SECONDS);

        assertTrue(result.isEmpty(), "Encountered non-empty list of posts");
    }

    @ParameterizedTest
    @MethodSource("bodyAndStatusForUnavailablePage")
    void fetchPostsReturnsEmptyListWhenAllPostPagesAreFailedToProcess(String body, int status) throws Exception {
        var indexPageResponse = """
                <xml>
                    <item>
                        <link>https://digestwave.com/post/42</link>
                        <pubDate>Tue, 14 Dec 2021 11:05:30 GMT</pubDate>
                        <category><![CDATA[Review]]></category>
                        <category><![CDATA[Laptops]]></category>
                    </item>
                </xml>
                """;

        when(httpClient.sendAsync(argThat(r -> r != null && r.uri().equals(INDEX_PAGE_URI)), notNull()))
                .thenReturn(completedFuture(createHttpResponseWithBody(indexPageResponse)));

        when(httpClient.sendAsync(
                argThat(r -> r != null
                        && r.uri().equals(READABILITY_PAGE_URI)
                        && r.bodyPublisher().isPresent()
                ),
                notNull()
        )).thenReturn(completedFuture(createHttpResponseWithBodyAndStatus(body, status)));

        CompletableFuture<List<Post>> resultFuture = rssSite.fetchPosts();
        List<Post> result = resultFuture.get(2, SECONDS);

        assertTrue(result.isEmpty(), "Encountered non-empty list of posts");
    }

    @Test
    void fetchPostsReturnsEmptyListWhenAllPostPagesAreCompletedWithExceptions() throws Exception {
        var indexPageResponse = """
                <xml>
                    <item>
                        <link>https://digestwave.com/post/42</link>
                        <pubDate>Tue, 14 Dec 2021 11:05:30 GMT</pubDate>
                        <category><![CDATA[Review]]></category>
                        <category><![CDATA[Laptops]]></category>
                    </item>
                </xml>
                """;

        when(httpClient.sendAsync(argThat(r -> r != null && r.uri().equals(INDEX_PAGE_URI)), notNull()))
                .thenReturn(completedFuture(createHttpResponseWithBody(indexPageResponse)));

        when(httpClient.sendAsync(
                argThat(r -> r != null
                        && r.uri().equals(READABILITY_PAGE_URI)
                        && r.bodyPublisher().isPresent()
                ),
                notNull()
        )).thenReturn(CompletableFuture.failedFuture(new NullPointerException()));

        CompletableFuture<List<Post>> resultFuture = rssSite.fetchPosts();
        List<Post> result = resultFuture.get(2, SECONDS);

        assertTrue(result.isEmpty(), "Encountered non-empty list of posts");
    }

    private static HttpResponse<Object> createHttpResponseWithBody(Object body) {
        return createHttpResponseWithBodyAndStatus(body, 200);
    }

    private static HttpResponse<Object> createHttpResponseWithBodyAndStatus(Object body, int status) {
        return new HttpResponse<>() {
            @Override
            public int statusCode() {
                return status;
            }

            @Override
            public HttpRequest request() {
                return null;
            }

            @Override
            public Optional<HttpResponse<Object>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return null;
            }

            @Override
            public String body() {
                try {
                    return body == null ? null : new ObjectMapper().writeValueAsString(body);
                } catch (JsonProcessingException e) {
                    return null;
                }
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return null;
            }

            @Override
            public HttpClient.Version version() {
                return null;
            }
        };
    }
}