package eu.hansolo.nightscoutconnector;

import eu.hansolo.toolbox.evt.type.ListChangeEvt;
import eu.hansolo.toolbox.observables.ObservableList;
import eu.hansolo.toolbox.properties.ObjectProperty;
import org.junit.jupiter.api.Test;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class LibTest {
    private static final String nightscoutUrl   = "https://glucose-anton.herokuapp.com";
    private static final String apiSecret       = "";
    private static final String nightscoutToken = "";

    @Test
    void testCurrentEntry() {
        ObjectProperty<Entry> currentEntry = new ObjectProperty<>();

        currentEntry.addObserver(evt -> { assert currentEntry.get().sgv() > 0; });

        try {
            currentEntry.set(Connector.getCurrentEntry(nightscoutUrl, apiSecret, nightscoutToken));
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testEntriesFromInterval() {
        Interval              interval = Interval.LAST_7_DAYS;
        ObservableList<Entry> entries  = new ObservableList<>();

        entries.addListChangeObserver(ListChangeEvt.ADDED, e -> {
            assert entries.size() > 0;
        });

        entries.addAll(Connector.getEntriesFromInterval(interval, nightscoutUrl, apiSecret, nightscoutToken));
    }

    @Test
    void testEntriesFromTo() {
        ZonedDateTime         from    = ZonedDateTime.now().minus(10, ChronoUnit.DAYS);
        ZonedDateTime         to      = ZonedDateTime.now();
        ObservableList<Entry> entries = new ObservableList<>();

        entries.addListChangeObserver(ListChangeEvt.ADDED, e -> {
            assert entries.size() > Interval.LAST_7_DAYS.getNoOfEntries();
        });

        entries.addAll(Connector.getEntriesFromTo(from, to, nightscoutUrl, apiSecret, nightscoutToken));
    }

    @Test
    void testLast10Entries() {
        int noOfEntries = 10;
        ObservableList<Entry> entries = new ObservableList<>();

        entries.addListChangeObserver(ListChangeEvt.ADDED, e -> {
            assert entries.size() == noOfEntries;
        });

        entries.addAll(Connector.getLastNEntries(noOfEntries, nightscoutUrl, apiSecret, nightscoutToken));
    }

    @Test
    void testLastEntryAsync() {
        int noOfEntries = 1;
        ObservableList<Entry> entries = new ObservableList<>();

        entries.addListChangeObserver(ListChangeEvt.ADDED, e -> {
            assert entries.size() == noOfEntries;
        });
    }

    @Test
    void testEntriesFromIntervalAsync() {
        Interval              interval = Interval.LAST_7_DAYS;
        ObservableList<Entry> entries  = new ObservableList<>();

        entries.addListChangeObserver(ListChangeEvt.ADDED, e -> {
            assert entries.size() > 0;
        });

        try {
            entries.addAll(Connector.getEntriesFromIntervalAsync(interval, nightscoutUrl, apiSecret, nightscoutToken).get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testEntriesFromToAsync() {
        ZonedDateTime         from    = ZonedDateTime.now().minus(10, ChronoUnit.DAYS);
        ZonedDateTime         to      = ZonedDateTime.now();
        ObservableList<Entry> entries = new ObservableList<>();

        entries.addListChangeObserver(ListChangeEvt.ADDED, e -> {
            assert entries.size() > Interval.LAST_7_DAYS.getNoOfEntries();
        });

        try {
            entries.addAll(Connector.getEntriesFromToAsync(from, to, nightscoutUrl, apiSecret, nightscoutToken).get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testLast10EntriesAsync() {
        int noOfEntries = 10;
        ObservableList<Entry> entries = new ObservableList<>();

        entries.addListChangeObserver(ListChangeEvt.ADDED, e -> {
            assert entries.size() == noOfEntries;
        });

        try {
            entries.addAll(Connector.getLastNEntriesAsync(noOfEntries, nightscoutUrl, apiSecret, nightscoutToken).get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testHbAc1() {
        Interval              interval = Interval.LAST_7_DAYS;
        ObservableList<Entry> entries  = new ObservableList<>();

        entries.addListChangeObserver(ListChangeEvt.ADDED, e -> {
            assert Connector.calcHbA1c(entries) > 5;
        });

        entries.addAll(Connector.getEntriesFromInterval(interval, nightscoutUrl, apiSecret, nightscoutToken));
    }

    @Test
    void testPrediction() {
        long    now        = Instant.now().getEpochSecond();
        long    min5       = now -  300;
        long    min10      = now - 300 * 2;
        long    min15      = now - 300 * 3;
        long    min20      = now - 300 * 4;
        long    min25      = now - 300 * 5;
        Instant nowMinus5  = Instant.ofEpochSecond(min5);
        Instant nowMinus10 = Instant.ofEpochSecond(min10);
        Instant nowMinus15 = Instant.ofEpochSecond(min15);
        Instant nowMinus20 = Instant.ofEpochSecond(min20);
        Instant nowMinus25 = Instant.ofEpochSecond(min25);

        List<Entry> entries;

        Entry entry1 = new Entry("1", 100, min5, OffsetDateTime.ofInstant(nowMinus5, ZoneId.systemDefault()), "", Trend.FLAT, "", "", "", 2, 0, 0, 0, 0, 0, "");
        Entry entry2 = new Entry("2", 110, min10, OffsetDateTime.ofInstant(nowMinus10, ZoneId.systemDefault()), "", Trend.FLAT, "", "", "", 2, 0, 0, 0, 0, 0, "");
        Entry entry3 = new Entry("3", 130, min10, OffsetDateTime.ofInstant(nowMinus15, ZoneId.systemDefault()), "", Trend.FLAT, "", "", "", 2, 0, 0, 0, 0, 0, "");
        Entry entry4 = new Entry("4", 155, min10, OffsetDateTime.ofInstant(nowMinus20, ZoneId.systemDefault()), "", Trend.FLAT, "", "", "", 2, 0, 0, 0, 0, 0, "");
        Entry entry5 = new Entry("5", 185, min10, OffsetDateTime.ofInstant(nowMinus25, ZoneId.systemDefault()), "", Trend.FLAT, "", "", "", 2, 0, 0, 0, 0, 0, "");
        entries = List.of(entry1, entry2, entry3, entry4, entry5);

        assert Connector.predict(entries) == Prediction.SOON_HIGH;

        Entry entry6  = new Entry("6", 100, min5, OffsetDateTime.ofInstant(nowMinus5, ZoneId.systemDefault()), "", Trend.FLAT, "", "", "", 2, 0, 0, 0, 0, 0, "");
        Entry entry7  = new Entry("7", 95, min10, OffsetDateTime.ofInstant(nowMinus10, ZoneId.systemDefault()), "", Trend.FLAT, "", "", "", 2, 0, 0, 0, 0, 0, "");
        Entry entry8  = new Entry("8", 85, min10, OffsetDateTime.ofInstant(nowMinus15, ZoneId.systemDefault()), "", Trend.FLAT, "", "", "", 2, 0, 0, 0, 0, 0, "");
        Entry entry9  = new Entry("9", 70, min10, OffsetDateTime.ofInstant(nowMinus20, ZoneId.systemDefault()), "", Trend.FLAT, "", "", "", 2, 0, 0, 0, 0, 0, "");
        Entry entry10 = new Entry("10", 60, min10, OffsetDateTime.ofInstant(nowMinus25, ZoneId.systemDefault()), "", Trend.FLAT, "", "", "", 2, 0, 0, 0, 0, 0, "");
        entries = List.of(entry6, entry7, entry8, entry9, entry10);

        assert Connector.predict(entries) == Prediction.SOON_TOO_LOW;
    }

    @Test
    void testParseEntries() {
        String entry1 = "[{\"_id\":\"64e114641ace7961997a8950\",\"device\":\"bubble\",\"date\":1692472374395,\"dateString\":\"2023-08-19T19:12:54.395Z\",\"sgv\":223,\"delta\":719175.346,\"direction\":\"SingleUp\",\"type\":\"sgv\",\"filtered\":223000,\"unfiltered\":223000,\"rssi\":1,\"noise\":1,\"sysTime\":\"2023-08-19T19:12:54.395Z\",\"dataType\":0,\"recordNumber\":19980,\"uploadDatatype\":\"upload1\",\"utcOffset\":120}]";
        try {
            List<Entry> entries = Connector.getEntriesFromJsonText(entry1);
            assert entries.get(0).sgv() == 223;
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }

        String entry2 = "[{\"_id\":\"64e30abd35608dfc20c1f45f\",\"device\":\"xDrip-LimiTTer\",\"type\":\"cal\",\"date\":1692601019196,\"dateString\":\"2023-08-21T06:56:59.196Z\",\"slope\":666.6666666666666,\"intercept\":43719.04479166667,\"scale\":1,\"sysTime\":\"2023-08-21T06:56:59.196Z\",\"utcOffset\":180}]";
        try {
            List<Entry> entries = Connector.getEntriesFromJsonText(entry2);
            assert entries.get(0).type().equals("cal");
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }

        String entry3 = "[{\"_id\":\"64e30997533cf5d7beb9ca14\",\"sgv\":119,\"date\":1692600707100,\"dateString\":\"2023-08-21T06:51:47.100Z\",\"trend\":4,\"direction\":\"Flat\",\"device\":\"go-away-please\",\"type\":\"sgv\",\"utcOffset\":0,\"sysTime\":\"2023-08-21T06:51:47.100Z\"}]";
        try {
            List<Entry> entries = Connector.getEntriesFromJsonText(entry3);
            assert entries.get(0).sgv() == 119;
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }
    }
}
