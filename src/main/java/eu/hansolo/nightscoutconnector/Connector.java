package eu.hansolo.nightscoutconnector;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.AccessDeniedException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import static eu.hansolo.nightscoutconnector.Constants.*;
import static eu.hansolo.toolbox.unit.UnitDefinition.MILLIGRAM_PER_DECILITER;
import static eu.hansolo.toolbox.unit.UnitDefinition.MILLIMOL_PER_LITER;


public class Connector {
    private static HttpClient httpClient;
    private static HttpClient httpClientAsync;


    // ******************** Sync methods **************************************
    public static final Entry getCurrentEntry(final String nightscoutUrl, final String apiSecret, final String nightscoutToken) throws AccessDeniedException, IllegalArgumentException {
        if (null == nightscoutUrl || nightscoutUrl.isEmpty()) { return null; }
        final String        secret     = null == apiSecret ? "" : apiSecret;
        final String        token      = (null == nightscoutToken || nightscoutToken.isEmpty()) ? "" : nightscoutToken;
        final StringBuilder urlBuilder = new StringBuilder().append(nightscoutUrl).append(CURRENT_JSON);
        if (!token.isEmpty()) { urlBuilder.append("&token=").append(token); }

        final String               url      = urlBuilder.toString();
        final HttpResponse<String> response = get(url, secret);
        if (null != response) {
            String jsonText = response.body();
            List<Entry> entries = getEntriesFromJsonText(jsonText);
            return entries.isEmpty() ? null : entries.get(0);
        }
        return null;
    }

    public static final List<Entry> getLastNEntries(final int noOfEntries, final String nightscoutUrl, final String apiSecret, final String nightscoutToken) throws IllegalArgumentException {
        if (null == nightscoutUrl || nightscoutUrl.isEmpty()) { throw new IllegalArgumentException("nightscoutUrl cannot be null"); }
        final String secret      = null == apiSecret ? "" : apiSecret;
        final String token       = (null == nightscoutToken || nightscoutToken.isEmpty()) ? "" : nightscoutToken;

        if (noOfEntries < 1) { throw new IllegalArgumentException("no of entries must at least be 1"); }

        final StringBuilder urlBuilder  = new StringBuilder().append(nightscoutUrl).append(ENTRIES_JSON).append("?count=").append(noOfEntries);
        if (!token.isEmpty()) { urlBuilder.append("&token=").append(token); }

        final String      url     = urlBuilder.toString();
        final List<Entry> entries;
        try {
            entries = getEntriesFromJsonText(get(url, secret).body());
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }
        return entries;
    }

    public static final List<Entry> getEntriesFromTo(final ZonedDateTime from, final ZonedDateTime to, final String nightscoutUrl, final String apiSecret, final String nightscoutToken) throws IllegalArgumentException {
        if (null == nightscoutUrl || nightscoutUrl.isEmpty()) { throw new IllegalArgumentException("nightscoutUrl cannot be null"); }
        final String secret      = null == apiSecret ? "" : apiSecret;
        final String token       = (null == nightscoutToken || nightscoutToken.isEmpty()) ? "" : nightscoutToken;
        final long   fromSeconds = from.toEpochSecond();
        final long   toSeconds   = to.toEpochSecond();
        if (fromSeconds >= toSeconds) { throw new IllegalArgumentException("from must be before to parameter"); }
        if (toSeconds - fromSeconds < 3600) { throw new IllegalArgumentException("The duration defined by from and to must be at least 1 hour"); }

        final int           noOfEntries = (int) ((toSeconds - fromSeconds) / 300);
        final StringBuilder urlBuilder  = new StringBuilder().append(nightscoutUrl).append(ENTRIES_JSON).append("?find[date][$gte]=").append(fromSeconds * 1000).append("&find[date][$lte]=").append(toSeconds * 1000).append("&count=").append(noOfEntries);
        if (!token.isEmpty()) { urlBuilder.append("&token=").append(token); }

        final String      url = urlBuilder.toString();
        final List<Entry> entries;
        try {
            entries = getEntriesFromJsonText(get(url, secret).body());
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }
        return entries;
    }

    public static final List<Entry> getEntriesFromInterval(final TimeInterval interval, final String nightscoutUrl, final String apiSecret, final String nightscoutToken) throws IllegalArgumentException {
        if (null == nightscoutUrl || nightscoutUrl.isEmpty()) { throw new IllegalArgumentException("nightscoutUrl cannot be null"); }
        final String        secret     = null == apiSecret ? "" : apiSecret;
        final String        token      = (null == nightscoutToken || nightscoutToken.isEmpty()) ? "" : nightscoutToken;
        final long          now        = Instant.now().getEpochSecond();
        final long          from       = (now - interval.getSeconds()) * 1000;
        final long          to         = now * 1000;
        final StringBuilder urlBuilder = new StringBuilder().append(nightscoutUrl).append(ENTRIES_JSON).append("?find[date][$gte]=").append(from).append("&find[date][$lte]=").append(to).append("&count=").append(interval.getNoOfEntries());
        if (!token.isEmpty()) { urlBuilder.append("&token=").append(token); }

        final String      url = urlBuilder.toString();
        final List<Entry> entries;
        try {
            entries = getEntriesFromJsonText(get(url, secret).body());
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }
        return entries;
    }

    public static final List<Treatment> getTreatmentsFromInterval(final TimeInterval interval, final String nightscoutUrl, final String apiSecret, final String nightscoutToken) {
        if (null == nightscoutUrl || nightscoutUrl.isEmpty()) { return null; }
        final String        secret     = null == apiSecret ? "" : apiSecret;
        final String        token      = (null == nightscoutToken || nightscoutToken.isEmpty()) ? "" : nightscoutToken;
        final long                         now   = Instant.now().getEpochSecond();
        final long                         from  = (now - interval.getSeconds()) * 1000;
        final long                         to    = now * 1000;
        final StringBuilder urlBuilder = new StringBuilder().append(nightscoutUrl).append(TREATMENTS_JSON).append("?find[date][$gte]=").append(from).append("&find[date][$lte]=").append(to).append("&count=").append(interval.getNoOfEntries());
        if (!token.isEmpty()) { urlBuilder.append("&token=").append(token); }

        final String                             url = urlBuilder.toString();
        final List<Treatment> treatments;
        try {
            treatments = getTreatmentsFromJsonText(get(url, secret).body());
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }
        return treatments;
    }


    // ******************** Async methods *************************************
    public static final CompletableFuture<List<Entry>> getLastNEntriesAsync(final int noOfEntries, final String nightscoutUrl, final String apiSecret, final String nightscoutToken) throws IllegalArgumentException {
        if (null == nightscoutUrl || nightscoutUrl.isEmpty()) { throw new IllegalArgumentException("nightscoutUrl cannot be null"); }
        final String secret      = null == apiSecret ? "" : apiSecret;
        final String token       = (null == nightscoutToken || nightscoutToken.isEmpty()) ? "" : nightscoutToken;

        if (noOfEntries < 1) { throw new IllegalArgumentException("no of entries must at least be 1"); }

        final StringBuilder urlBuilder  = new StringBuilder().append(nightscoutUrl).append(ENTRIES_JSON).append("?count=").append(noOfEntries);
        if (!token.isEmpty()) { urlBuilder.append("&token=").append(token); }

        final String                         url = urlBuilder.toString();
        final CompletableFuture<List<Entry>> cf  = getAsync(url, secret).thenApply(r -> {
            try {
                return (null == r || null == r.body() || r.body().isEmpty()) ? new ArrayList<>() : getEntriesFromJsonText(r.body());
            } catch (AccessDeniedException e) {
                throw new RuntimeException(e);
            }
        });
        return cf;
    }

    public static final CompletableFuture<List<Entry>> getEntriesFromToAsync(final ZonedDateTime from, final ZonedDateTime to, final String nightscoutUrl, final String apiSecret, final String nightscoutToken) throws IllegalArgumentException {
        if (null == nightscoutUrl || nightscoutUrl.isEmpty()) { throw new IllegalArgumentException("nightscoutUrl cannot be null"); }
        final String secret      = null == apiSecret ? "" : apiSecret;
        final String token       = (null == nightscoutToken || nightscoutToken.isEmpty()) ? "" : nightscoutToken;
        final long   fromSeconds = from.toEpochSecond();
        final long   toSeconds   = to.toEpochSecond();
        if (fromSeconds >= toSeconds) { throw new IllegalArgumentException("from must be before to parameter"); }
        if (toSeconds - fromSeconds < 3600) { throw new IllegalArgumentException("The duration defined by from and to must be at least 1 hour"); }

        final int           noOfEntries = (int) ((toSeconds - fromSeconds) / 300);
        final StringBuilder urlBuilder  = new StringBuilder().append(nightscoutUrl).append(ENTRIES_JSON).append("?find[date][$gte]=").append(fromSeconds * 1000).append("&find[date][$lte]=").append(toSeconds * 1000).append("&count=").append(noOfEntries);
        if (!token.isEmpty()) { urlBuilder.append("&token=").append(token); }

        final String                         url = urlBuilder.toString();
        final CompletableFuture<List<Entry>> cf  = getAsync(url, secret).thenApply(r -> {
            try {
                return (null == r || null == r.body() || r.body().isEmpty()) ? new ArrayList<>() : getEntriesFromJsonText(r.body());
            } catch (AccessDeniedException e) {
                throw new RuntimeException(e);
            }
        });
        return cf;
    }

    public static final CompletableFuture<List<Entry>> getEntriesFromIntervalAsync(final TimeInterval interval, final String nightscoutUrl, final String apiSecret, final String nightscoutToken) throws IllegalArgumentException {
        if (null == nightscoutUrl || nightscoutUrl.isEmpty()) { throw new IllegalArgumentException("nightscoutUrl cannot be null"); }
        final String        secret     = null == apiSecret ? "" : apiSecret;
        final String        token      = (null == nightscoutToken || nightscoutToken.isEmpty()) ? "" : nightscoutToken;
        final long          now        = Instant.now().getEpochSecond();
        final long          from       = (now - interval.getSeconds()) * 1000;
        final long          to         = now * 1000;
        final StringBuilder urlBuilder = new StringBuilder().append(nightscoutUrl).append(ENTRIES_JSON).append("?find[date][$gte]=").append(from).append("&find[date][$lte]=").append(to).append("&count=").append(interval.getNoOfEntries());
        if (!token.isEmpty()) { urlBuilder.append("&token=").append(token); }

        final String                         url = urlBuilder.toString();
        final CompletableFuture<List<Entry>> cf  = getAsync(url, secret).thenApply(r -> {
            try {
                return (null == r || null == r.body() || r.body().isEmpty()) ? new ArrayList<>() : getEntriesFromJsonText(r.body());
            } catch (AccessDeniedException e) {
                throw new RuntimeException(e);
            }
        });
        return cf;
    }

    public static final CompletableFuture<List<Treatment>> getTreatmentsFromIntervalAsync(final TimeInterval interval, final String nightscoutUrl, final String apiSecret, final String nightscoutToken) {
        if (null == nightscoutUrl || nightscoutUrl.isEmpty()) { return null; }
        final String        secret     = null == apiSecret ? "" : apiSecret;
        final String        token      = (null == nightscoutToken || nightscoutToken.isEmpty()) ? "" : nightscoutToken;
        final long                         now   = Instant.now().getEpochSecond();
        final long                         from  = (now - interval.getSeconds()) * 1000;
        final long                         to    = now * 1000;
        final StringBuilder urlBuilder = new StringBuilder().append(nightscoutUrl).append(TREATMENTS_JSON).append("?find[date][$gte]=").append(from).append("&find[date][$lte]=").append(to).append("&count=").append(interval.getNoOfEntries());
        if (!token.isEmpty()) { urlBuilder.append("&token=").append(token); }

        final String                             url = urlBuilder.toString();
        final CompletableFuture<List<Treatment>> cf  = getAsync(url, secret).thenApply(r -> {
            try {
                return (null == r || null == r.body() || r.body().isEmpty()) ? new ArrayList<>() : getTreatmentsFromJsonText(r.body());
            } catch (AccessDeniedException e) {
                throw new RuntimeException(e);
            }
        });
        return cf;
    }


    // ******************** Calculations **************************************
    public static final double mmolPerLiterToMgPerDeciliter(final double mmolPerLiter) {
        if (mmolPerLiter <= 0) { throw new IllegalArgumentException("mmolPerLiter must be greater than 0"); }
        return MMOL_CONVERTER.convert(mmolPerLiter, MILLIGRAM_PER_DECILITER);
    }

    public static final double mgPerDeciliterToMmolPerLiter(final double mgPerDeciliter) {
        if (mgPerDeciliter <= 0) { throw new IllegalArgumentException("mgPerDeciliter must be greater than 0"); }
        return MGDL_CONVERTER.convert(mgPerDeciliter, MILLIMOL_PER_LITER);
    }

    public static final double calcHbA1c(final List<Entry> entries) {
        if (null == entries) { throw new IllegalArgumentException("list of entries cannot be null"); }
        double hba1c = 0;
        if (!entries.isEmpty()) {
            double average = entries.stream().map(entry -> entry.sgv()).reduce(0.0, Double::sum).doubleValue() / entries.size();
            //hba1c   = (46.7 + average) / 28.7;  // formula from 2008
            hba1c   = (0.0296 * average) + 2.419; // formula from 2014 (https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4771657/)
        }
        return hba1c;
    }

    public static final Prediction predict(final List<Entry> entries) throws IllegalArgumentException {
        if (null == entries) { throw new IllegalArgumentException("list of entries cannot be null"); }
        if (entries.size() < 5) { throw new IllegalArgumentException("list must at least have 5 entries"); }
        final List<Entry> lastEntries = entries.stream().sorted(Comparator.comparingLong(Entry::datelong).reversed()).limit(5).collect(Collectors.toList());
        final Entry       lastEntry   = entries.stream().sorted(Comparator.comparingLong(Entry::datelong).reversed()).collect(Collectors.toList()).get(entries.size() - 1);
        final Map<Integer, Double> deltaMap = new HashMap<>();
        for (int i = 1 ; i  < 5 ; i++) {
            final Entry  entry1         = lastEntries.get(i - 1);
            final Entry  entry2         = lastEntries.get(i);

            final double sgv1           = entry1.sgv();
            final double sgv2           = entry2.sgv();
            final double deltaSGV       = sgv2 - sgv1;

            deltaMap.put(i, deltaSGV);
        }

        final double average = deltaMap.values().stream().mapToDouble(Double::valueOf).sum() / entries.size();
        final double lastSGV = lastEntry.sgv();
        if (lastSGV < MIN_CRITICAL) {
            return Prediction.SOON_TOO_LOW;
        } else if (lastSGV < MIN_ACCEPTABLE && average <= -5) {
            return Prediction.SOON_TOO_LOW;
        } else if (lastSGV < MIN_NORMAL && average <= -5) {
            return Prediction.SOON_LOW;
        } else if (lastSGV > MAX_NORMAL && average >= 5) {
            return Prediction.SOON_HIGH;
        } else if (lastSGV > MAX_ACCEPTABLE && average >= 5) {
            return Prediction.SOON_TOO_HIGH;
        } else if (lastSGV > TOO_HIGH) {
            return Prediction.TOO_HIGH;
        }
        return Prediction.NONE;
    }


    // ******************** Parsing *******************************************
    private static List<Entry> getEntriesFromJsonText(final String jsonText) throws IllegalArgumentException, AccessDeniedException {
        if (null == jsonText || jsonText.isEmpty()) { throw new IllegalArgumentException("jsonText cannot be null or empty"); }
        final List<Entry> entries     = new ArrayList<>();
        final Gson        gson        = new Gson();
        final JsonElement jsonElement = gson.fromJson(jsonText, JsonElement.class);

        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String msg = "";
            if (jsonObject.has("message")) { msg = jsonObject.get("message").getAsString(); }
            throw new AccessDeniedException(msg.isEmpty() ? "Access denied" : msg);
        } else {
            final JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (JsonElement jsonElmnt : jsonArray) {
                JsonObject json = jsonElmnt.getAsJsonObject();

                String         id         = json.get(FIELD_ID).getAsString();
                double         sgv        = json.has(FIELD_SGV) ? json.get(FIELD_SGV).getAsDouble() : 0;
                long           datelong   = json.has(FIELD_DATE) ? json.get(FIELD_DATE).getAsLong() / 1000 : 0;
                OffsetDateTime date       = OffsetDateTime.ofInstant(Instant.ofEpochSecond(datelong), ZoneId.systemDefault());
                String         dateString = json.has(FIELD_DATE_STRING) ? json.get(FIELD_DATE_STRING).getAsString() : "";
                Trend          trend      = json.has(FIELD_TREND) ? Trend.getFromText(json.get(FIELD_TREND).getAsString()) : Trend.NONE;
                String         direction  = json.has(FIELD_DIRECTION) ? json.get(FIELD_DIRECTION).getAsString() : "";
                String         device     = json.has(FIELD_DEVICE) ? json.get(FIELD_DEVICE).getAsString() : "";
                String         type       = json.has(FIELD_TYPE) ? json.get(FIELD_TYPE).getAsString() : "";
                int            utcOffset  = json.has(FIELD_UTC_OFFSET) ? json.get(FIELD_UTC_OFFSET).getAsInt() : 0;
                int            noise      = json.has(FIELD_NOISE) ? json.get(FIELD_NOISE).getAsInt() : 0;
                double         filtered   = json.has(FIELD_FILTERED) ? json.get(FIELD_FILTERED).getAsDouble() : 0;
                double         unfiltered = json.has(FIELD_UNFILTERED) ? json.get(FIELD_UNFILTERED).getAsDouble() : 0;
                int            rssi       = json.has(FIELD_RSSI) ? json.get(FIELD_RSSI).getAsInt() : 0;
                double         delta      = json.has(FIELD_DELTA) ? json.get(FIELD_DELTA).getAsDouble() : 0;
                String         sysTime    = json.has(FIELD_SYS_TIME) ? json.get(FIELD_SYS_TIME).getAsString() : "";
                entries.add(new Entry(id, sgv, datelong, date, dateString, trend, direction, device, type, utcOffset, noise, filtered, unfiltered, rssi, delta, sysTime));
            }
            return entries;
        }
    }

    private static List<Treatment> getTreatmentsFromJsonText(final String jsonText) throws IllegalArgumentException, AccessDeniedException {
        if (null == jsonText || jsonText.isEmpty()) { throw new IllegalArgumentException("jsonText cannot be null or empty"); }
        final List<Treatment> treatments  = new ArrayList<>();
        final Gson            gson        = new Gson();
        final JsonElement     jsonElement = gson.fromJson(jsonText, JsonElement.class);

        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            String msg = "";
            if (jsonObject.has("message")) { msg = jsonObject.get("message").getAsString(); }
            throw new AccessDeniedException(msg.isEmpty() ? "Access denied" : msg);
        } else {
            final JsonArray       jsonArray  = jsonElement.getAsJsonArray();
            for (JsonElement jsonElmnt : jsonArray) {
                JsonObject     json       = jsonElmnt.getAsJsonObject();

                String id            = json.get(FIELD_ID).getAsString();
                String eventType     = json.has(FIELD_EVENT_TYPE)     ? json.get(FIELD_EVENT_TYPE).getAsString()     : "";
                String createdAt     = json.has(FIELD_CREATED_AT)     ? json.get(FIELD_CREATED_AT).getAsString()     : "";
                String glucose       = json.has(FIELD_GLUCOSE)        ? json.get(FIELD_GLUCOSE).getAsString()        : "";
                String glucoseType   = json.has(FIELD_GLUCOSE_TYPE)   ? json.get(FIELD_GLUCOSE_TYPE).getAsString()   : "";
                double carbs         = json.has(FIELD_CARBS)          ? json.get(FIELD_CARBS).getAsDouble()          : 0.0;
                double protein       = json.has(FIELD_PROTEIN)        ? json.get(FIELD_PROTEIN).getAsDouble()        : 0.0;
                double fat           = json.has(FIELD_FAT)            ? json.get(FIELD_FAT).getAsDouble()            : 0.0;
                double insulin       = json.has(FIELD_INSULIN)        ? json.get(FIELD_INSULIN).getAsDouble()        : 0.0;
                String units         = json.has(FIELD_UNITS)          ? json.get(FIELD_UNITS).getAsString()          : "";
                String transmitterId = json.has(FIELD_TRANSMITTER_ID) ? json.get(FIELD_TRANSMITTER_ID).getAsString() : "";
                String sensorCode    = json.has(FIELD_SENSOR_CODE)    ? json.get(FIELD_SENSOR_CODE).getAsString()    : "";
                String notes         = json.has(FIELD_NOTES)          ? json.get(FIELD_NOTES).getAsString()          : "";
                String enteredBy     = json.has(FIELD_ENTERED_BY)     ? json.get(FIELD_ENTERED_BY).getAsString()     : "";

                treatments.add(new Treatment(id, eventType, createdAt, glucose, glucoseType, carbs, protein, fat, insulin, units, transmitterId, sensorCode, notes, enteredBy));
            }
            return treatments;
        }
    }


    // ******************** REST calls ****************************************
    private static HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                         .connectTimeout(Duration.ofSeconds(20))
                         .version(Version.HTTP_2)
                         .followRedirects(Redirect.NORMAL)
                         //.executor(Executors.newFixedThreadPool(4))
                         .build();
    }

    public static final HttpResponse<String> get(final String uri, final String apiSecret) {
        if (null == httpClient) { httpClient = createHttpClient(); }

        HttpRequest request = HttpRequest.newBuilder()
                                         .GET()
                                         .uri(URI.create(uri))
                                         .setHeader("Accept", "application/json")
                                         .setHeader("User-Agent", "Sweety")
                                         .setHeader("API_SECRET", apiSecret)
                                         .timeout(Duration.ofSeconds(60))
                                         .build();

        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response;
            } else {
                // Problem with url request
                return response;
            }
        } catch (CompletionException | InterruptedException | IOException e) {
            return null;
        }
    }

    public static final CompletableFuture<HttpResponse<String>> getAsync(final String uri, final String apiSecret) {
        if (null == httpClientAsync) { httpClientAsync = createHttpClient(); }

        final HttpRequest request = HttpRequest.newBuilder()
                                               .GET()
                                               .uri(URI.create(uri))
                                               .setHeader("Accept", "application/json")
                                               .setHeader("User-Agent", "Sweety")
                                               .setHeader("API_SECRET", apiSecret)
                                               .timeout(Duration.ofSeconds(60))
                                               .build();
        return httpClientAsync.sendAsync(request, BodyHandlers.ofString());
    }
}
