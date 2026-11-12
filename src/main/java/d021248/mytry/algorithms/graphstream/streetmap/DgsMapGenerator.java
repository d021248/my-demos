package d021248.mytry.algorithms.graphstream.streetmap;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;

/// Fetches an OSM road network via the Overpass API and writes it as a DGS file
/// ready to be loaded as a `MapSource.Builtin` in `StreetMapViewer`.
///
/// Geocoding uses Nominatim. If Nominatim is unreachable (e.g. corporate proxy),
/// coordinates are taken from `KNOWN_CITIES`. Add entries there for offline use.
///
/// ## Usage
///
/// ```
/// mvn exec:java -Dexec.mainClass=...DgsMapGenerator                   # default city
/// mvn exec:java -Dexec.mainClass=...DgsMapGenerator -Dexec.args="Munich"
/// ```
public final class DgsMapGenerator {

  /// Offline fallback coordinates for a city (used when Nominatim is unreachable).
  private record CityConfig(double lat, double lon) {}

  private static final String DEFAULT_CITY = "Königsbrunn";
  private static final double RANGE_IN_METERS = 2000;
  private static final String RESOURCES_DATA =
      "src/main/resources/d021248/mytry/algorithms/graphstream/streetmap/data/";

  // Offline coordinate cache — add entries here for cities where Nominatim is
  // blocked.
  private static final Map<String, CityConfig> KNOWN_CITIES =
      Map.of(
          DEFAULT_CITY,
          new CityConfig(48.2683, 10.8897),
          "Bad Wiessee",
          new CityConfig(47.7173, 11.7200),
          "Haunstetten",
          new CityConfig(48.3170, 10.9025),
          "Augsburg",
          new CityConfig(48.3717, 10.8983),
          "Wiesloch",
          new CityConfig(49.2950, 8.6975),
          "Walldorf",
          new CityConfig(49.3061, 8.6447),
          "Memmingen",
          new CityConfig(47.9878, 10.1806),
          "Haidhausen",
          new CityConfig(48.1295, 11.5971));

  public static void main(String[] args) {
    var cityName = args.length > 0 ? args[0] : DEFAULT_CITY;
    var outputPath = RESOURCES_DATA + cityName.replace(" ", "") + "_Generated.dgs";
    try {
      System.out.println("Fetching coordinates for " + cityName + " via Nominatim...");
      double[] center = getCityCenter(cityName);
      System.out.printf(Locale.ROOT, "Center found: lat=%.4f lon=%.4f%n", center[0], center[1]);

      System.out.println("Fetching road network within " + RANGE_IN_METERS + " m...");
      var osmXmlData = fetchOsmData(center[0], center[1], RANGE_IN_METERS);

      System.out.println("Writing DGS file: " + outputPath);
      convertOsmToDgs(osmXmlData, outputPath);

      System.out.println(
          "Done. Load with: new MapSource.Builtin(\"%s_Generated\")"
              .formatted(cityName.replace(" ", "")));

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Resolves city coordinates via Nominatim; falls back to KNOWN_CITIES on
  // failure.
  private static double[] getCityCenter(String city) throws Exception {
    String url =
        "https://nominatim.openstreetmap.org/search?q="
            + URLEncoder.encode(city, StandardCharsets.UTF_8)
            + "&format=json&limit=1";

    var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    var request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("User-Agent", "JavaGraphStreamMapGenerator/1.0 (contact: dev@example.com)")
            .timeout(Duration.ofSeconds(10))
            .build();

    try {
      var response = client.send(request, HttpResponse.BodyHandlers.ofString());
      String body = response.body();
      if (body != null && body.contains("\"lat\"") && body.contains("\"lon\"")) {
        double lat = Double.parseDouble(extractJsonValue(body, "\"lat\":\""));
        double lon = Double.parseDouble(extractJsonValue(body, "\"lon\":\""));
        return new double[] {lat, lon};
      }
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      throw ie;
    } catch (Exception _) {
      // network error — try fallback
    }

    var fallback = KNOWN_CITIES.get(city);
    if (fallback != null) {
      System.out.println("Nominatim unreachable — using built-in coordinates.");
      return new double[] {fallback.lat(), fallback.lon()};
    }
    throw new IllegalArgumentException(
        "City '" + city + "' not found and no fallback coordinates available.");
  }

  private static String extractJsonValue(String json, String key) {
    int start = json.indexOf(key) + key.length();
    int end = json.indexOf("\"", start);
    return json.substring(start, end);
  }

  // Queries the Overpass API via POST with exponential backoff on 502 overload.
  private static String fetchOsmData(double lat, double lon, double radius) throws Exception {
    String query =
        String.format(
            Locale.ROOT,
            "[out:xml];"
                + "(way(around:%f,%f,%f)[highway~\"^(motorway|trunk|primary|secondary|tertiary|residential|service|unclassified|living_street)$\"];"
                + "way(around:%f,%f,%f)[waterway~\"^(river|stream|canal|drain)$\"];"
                + ");out body;>;out skel qt;",
            radius,
            lat,
            lon,
            radius,
            lat,
            lon);

    var body = "data=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
    var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();

    int maxAttempts = 5;
    long waitMs = 5_000;
    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      var request =
          HttpRequest.newBuilder()
              .uri(URI.create("https://overpass-api.de/api/interpreter"))
              .header("User-Agent", "JavaGraphStreamMapGenerator/1.0")
              .header("Content-Type", "application/x-www-form-urlencoded")
              .timeout(Duration.ofSeconds(60))
              .POST(HttpRequest.BodyPublishers.ofString(body))
              .build();

      var response = client.send(request, HttpResponse.BodyHandlers.ofString());
      String responseBody = response.body();

      if (responseBody != null
          && !responseBody.contains("<error>")
          && !responseBody.contains("Gateway Timeout")) {
        return responseBody;
      }

      if (attempt < maxAttempts) {
        System.out.printf(
            "Overpass busy (attempt %d/%d) — retrying in %ds...%n",
            attempt, maxAttempts, waitMs / 1000);
        Thread.sleep(waitMs);
        waitMs *= 2;
      }
    }
    throw new IllegalStateException("Overpass API unavailable after " + maxAttempts + " attempts.");
  }

  // Parses OSM XML and writes nodes and edges in DGS format.
  private static void convertOsmToDgs(String xmlData, String outputPath) throws Exception {
    var doc =
        DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(new ByteArrayInputStream(xmlData.getBytes(StandardCharsets.UTF_8)));

    try (var bw = new BufferedWriter(new FileWriter(outputPath))) {
      bw.write("DGS004\nnull 0 0\n\n# --- NODES ---\n");

      var nodes = doc.getElementsByTagName("node");
      for (int i = 0; i < nodes.getLength(); i++) {
        var node = (Element) nodes.item(i);
        String id = node.getAttribute("id");
        String lat = node.getAttribute("lat");
        String lon = node.getAttribute("lon");
        bw.write(String.format("an \"%s\" x=\"%s\" y=\"%s\"\n", id, lon, lat));
      }

      bw.write("\n# --- EDGES ---\n");

      var ways = doc.getElementsByTagName("way");
      for (int i = 0; i < ways.getLength(); i++) {
        var way = (Element) ways.item(i);
        String wayId = way.getAttribute("id");

        Map<String, String> tags = new HashMap<>();
        var tagNodes = way.getElementsByTagName("tag");
        for (int j = 0; j < tagNodes.getLength(); j++) {
          var tag = (Element) tagNodes.item(j);
          tags.put(tag.getAttribute("k"), tag.getAttribute("v"));
        }

        var flags =
            tags.containsKey("waterway") ? buildWaterwayFlags(tags) : buildHighwayFlags(tags);

        var ndNodes = way.getElementsByTagName("nd");
        List<String> nodeRefs = new ArrayList<>();
        for (int j = 0; j < ndNodes.getLength(); j++) {
          nodeRefs.add(((Element) ndNodes.item(j)).getAttribute("ref"));
        }

        for (int j = 0; j < nodeRefs.size() - 1; j++) {
          String from = nodeRefs.get(j);
          String to = nodeRefs.get(j + 1);
          bw.write(String.format("ae \"E_%s_%d\" \"%s\" > \"%s\"%s\n", wayId, j, from, to, flags));
        }
      }
    }
  }

  private static String buildHighwayFlags(Map<String, String> tags) {
    double speedMax = 50.0;
    if (tags.containsKey("maxspeed")) {
      try {
        speedMax = Double.parseDouble(tags.get("maxspeed").replaceAll("\\D", ""));
      } catch (Exception _) {
        // OSM maxspeed values like "50 mph" or "DE:urban" — fall through to default
      }
    } else {
      speedMax =
          switch (tags.getOrDefault("highway", "residential")) {
            case "motorway" -> 130.0;
            case "trunk", "primary" -> 100.0;
            case "secondary" -> 80.0;
            case "living_street" -> 7.0;
            default -> 50.0;
          };
    }
    var sb = new StringBuilder(" speedMax=").append(speedMax);
    if ("yes".equals(tags.get("bridge"))) sb.append(" isBridge=true");
    if ("yes".equals(tags.get("tunnel"))) sb.append(" isTunnel=true");
    if ("yes".equals(tags.get("toll"))) sb.append(" isTollway=true");
    appendName(sb, tags);
    return sb.toString();
  }

  private static String buildWaterwayFlags(Map<String, String> tags) {
    var sb = new StringBuilder(" isWaterway=true");
    sb.append(" waterway=\"")
        .append(escapeDgs(tags.getOrDefault("waterway", "river")))
        .append("\"");
    appendName(sb, tags);
    return sb.toString();
  }

  private static void appendName(StringBuilder sb, Map<String, String> tags) {
    var name = tags.get("name");
    if (name != null && !name.isBlank()) {
      sb.append(" name=\"").append(escapeDgs(name)).append("\"");
    }
  }

  // Escapes backslashes and double-quotes for DGS string attribute values.
  private static String escapeDgs(String s) {
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
