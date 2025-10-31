package org.example.atg;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * ATG API via Java HttpClient med headers som efterliknar en riktig webbläsare.
 */
public class AtgApiClient implements AutoCloseable {

	private static final String BASE_URL = "https://api.atg.se/";
    private final HttpClient client;

    public AtgApiClient() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    private String fetch(String endpoint) throws IOException, InterruptedException {
        String url = BASE_URL + endpoint;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                // viktiga headers för att likna en vanlig browser
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "sv-SE,sv;q=0.9,en;q=0.8")
                .header("Cache-Control", "no-cache")
                .header("Pragma", "no-cache")
                .header("Referer", "https://www.atg.se/")
                .header("Origin", "https://www.atg.se")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                        + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36")
                .header("Sec-Fetch-Site", "same-origin")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Dest", "empty")
                .GET()
                .build();

        System.out.println("🔍 Hämtar från: " + url);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        System.out.println("📡 ATG svar: " + status);

        if (status != 200) {
            System.out.println("❌ Feltext:\n" + response.body());
            throw new IOException("ATG svarade " + status + " för " + endpoint);
        }

        return response.body();
    }

    /** Hämtar kalendern med tillgängliga V86-omgångar. */
    public String getV86Calendar() throws IOException, InterruptedException {
        return fetch("products/V86");
    }

    /** Hämtar en specifik V86-omgångs detaljer (hästar, kuskar, banor...) */
    public String getV86ProductForDate(String date) throws IOException, InterruptedException {
        return fetch("products/V86/" + date);
    }

    @Override
    public void close() {
        // inget att stänga
    }
}
