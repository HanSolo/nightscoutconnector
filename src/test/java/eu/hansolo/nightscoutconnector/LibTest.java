package eu.hansolo.nightscoutconnector;

import eu.hansolo.toolbox.evt.type.ListChangeEvt;
import eu.hansolo.toolbox.observables.ObservableList;
import eu.hansolo.toolbox.properties.ObjectProperty;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;


public class LibTest {
    private static final String nightscoutUrl   = "https://glucose-anton.herokuapp.com";
    private static final String apiSecret       = "";
    private static final String nightscoutToken = "";

    @Test
    void testCurrentEntry() {
        ObjectProperty<Entry> currentEntry = new ObjectProperty<>();
        currentEntry.addObserver(evt -> { assert currentEntry.get().sgv() > 0; });
    }

    @Test
    void testEntriesFromInterval() {
        Interval              interval = Interval.LAST_7_DAYS;
        ObservableList<Entry> entries  = new ObservableList<>();

        entries.addListChangeObserver(ListChangeEvt.ADDED, e -> {
            assert entries.size() > 0;
        });

        entries.addAll(Helper.getEntriesFromInterval(interval, nightscoutUrl, apiSecret, nightscoutToken));
    }

    @Test
    void testEntriesFromTo() {
        ZonedDateTime         from    = ZonedDateTime.now().minus(10, ChronoUnit.DAYS);
        ZonedDateTime         to      = ZonedDateTime.now();
        ObservableList<Entry> entries = new ObservableList<>();

        entries.addListChangeObserver(ListChangeEvt.ADDED, e -> {
            assert entries.size() > Interval.LAST_7_DAYS.getNoOfEntries();
        });

        entries.addAll(Helper.getEntriesFromTo(from, to, nightscoutUrl, apiSecret, nightscoutToken));
    }

    @Test
    void testLast10Entries() {
        int noOfEntries = 10;
        ObservableList<Entry> entries = new ObservableList<>();

        entries.addListChangeObserver(ListChangeEvt.ADDED, e -> {
            assert entries.size() == noOfEntries;
        });

        entries.addAll(Helper.getLastNEntries(noOfEntries, nightscoutUrl, apiSecret, nightscoutToken));
    }

    @Test
    void testEntriesFromIntervalAsync() {
        Interval              interval = Interval.LAST_7_DAYS;
        ObservableList<Entry> entries  = new ObservableList<>();

        entries.addListChangeObserver(ListChangeEvt.ADDED, e -> {
            assert entries.size() > 0;
        });

        try {
            entries.addAll(Helper.getEntriesFromIntervalAsync(interval, nightscoutUrl, apiSecret, nightscoutToken).get());
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
            entries.addAll(Helper.getEntriesFromToAsync(from, to, nightscoutUrl, apiSecret, nightscoutToken).get());
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
            entries.addAll(Helper.getLastNEntriesAsync(noOfEntries, nightscoutUrl, apiSecret, nightscoutToken).get());
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
            assert Helper.calcHbA1c(entries) > 5;
        });

        entries.addAll(Helper.getEntriesFromInterval(interval, nightscoutUrl, apiSecret, nightscoutToken));
    }
}
