package red.ethel.townnames.bananas.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import red.ethel.townnames.bananas.BananasEntry;
import red.ethel.townnames.bananas.ContentId;

/**
 * HTTP client for the OpenTTD Bananas REST API.
 *
 * <p>Network errors propagate as {@link IOException} or {@link InterruptedException}. Implements
 * {@link AutoCloseable}; callers should close this instance when done to release the underlying
 * connection pool.
 */
public final class BananasHttpClient implements AutoCloseable {

    private static final String DEFAULT_BASE_URL = "https://bananas-api.openttd.org";

    private final String baseUrl;
    private final HttpClient http;
    private final ObjectMapper mapper;

    /** Create a client against the production Bananas API. */
    public BananasHttpClient() {
        this(DEFAULT_BASE_URL);
    }

    /** Create a client against a custom base URL (used in tests). */
    public BananasHttpClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.http = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    /**
     * Search for NewGRF town-name packages matching the query. An empty query returns all available
     * town-name GRFs.
     */
    public List<BananasEntry> search(String query) throws IOException, InterruptedException {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        var request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/package/newgrf?q=" + encoded))
                .GET()
                .build();

        var response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Bananas search failed with status " + response.statusCode());
        }

        JsonNode root = mapper.readTree(response.body());
        var results = new ArrayList<BananasEntry>();
        for (JsonNode node : root) {
            results.add(parseEntry(node));
        }
        return List.copyOf(results);
    }

    /**
     * Download the binary content of a NewGRF file. Returns the raw bytes; the caller is responsible
     * for caching.
     */
    public byte[] download(BananasEntry entry) throws IOException, InterruptedException {
        String url = baseUrl + "/download/" + entry.contentId().grfid();
        var request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        var response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) {
            throw new IOException("Bananas download failed with status " + response.statusCode());
        }
        return response.body();
    }

    @Override
    public void close() {
        http.close();
    }

    private static BananasEntry parseEntry(JsonNode node) {
        var id = new ContentId(
                node.path("content-id").asText(), node.path("md5sum").asText());
        return new BananasEntry(
                id,
                node.path("version").asText(),
                node.path("name").asText(),
                node.has("description") && !node.get("description").isNull()
                        ? node.get("description").asText()
                        : null,
                node.path("license").asText(), // API uses "license" (US), stored as "licence" (GB)
                node.path("author").asText());
    }
}
