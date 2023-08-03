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
}
