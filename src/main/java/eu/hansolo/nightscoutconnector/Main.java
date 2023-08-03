package eu.hansolo.nightscoutconnector;

import eu.hansolo.toolbox.evt.type.ListChangeEvt;
import eu.hansolo.toolbox.observables.ObservableList;
import eu.hansolo.toolbox.properties.DoubleProperty;
import eu.hansolo.toolbox.properties.ObjectProperty;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class Main {
    private final String                   nightscoutUrl;
    private final String                   apiSecret;
    private final String                   nightscoutToken;
    private final ObservableList<Entry>    entries;
    private final Interval                 interval;
    private final DoubleProperty           hba1c;
    private final ScheduledExecutorService executor;
    private       ObjectProperty<Entry>    currentEntry;


    public Main() {
        this.nightscoutUrl   = "https://glucose-anton.herokuapp.com";
        this.apiSecret       = "rt4wbwyAnuetp0tE";
        this.nightscoutToken = "";
        this.entries         = new ObservableList<>();
        this.interval        = Interval.LAST_90_DAYS;
        this.hba1c           = new DoubleProperty(0) {
            @Override public void didChange(final Double oldValue, final Double newValue) {
                System.out.println("Estimated HbA1c: " + String.format(Locale.US, "%.1f%%" , newValue));
            }
        };
        this.executor        = Executors.newSingleThreadScheduledExecutor();
        try {
            this.currentEntry    = new ObjectProperty<>(Helper.getCurrentEntry(nightscoutUrl, apiSecret, nightscoutToken));
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }

        registerListeners();

        // Initially load the entries from the last 90 days and after that schedule an update call once a minute
        final Runnable task = () -> {
            try {
                this.currentEntry.set(Helper.getCurrentEntry(nightscoutUrl, apiSecret, nightscoutToken));
            } catch (AccessDeniedException e) {
                throw new RuntimeException(e);
            }
        };
        try {
            System.out.println("Getting entries from nightscout...");
            long start = System.nanoTime();
            entries.addAll(Helper.getEntriesFromIntervalAsync(interval, nightscoutUrl, apiSecret, nightscoutToken).get());
            System.out.println("Fetched " + entries.size() + " entries in " + ((System.nanoTime() - start) / 1_000_000) + "ms");
            executor.scheduleAtFixedRate(task, Constants.INITIAL_DELAY_MS, Constants.INTERVAL_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        // Get entries between from and to async
        ZonedDateTime from = ZonedDateTime.now().minus(90, ChronoUnit.DAYS);
        ZonedDateTime to   = ZonedDateTime.now();
        try {
            List<Entry> customEntries = Helper.getEntriesFromToAsync(from, to, nightscoutUrl, apiSecret, nightscoutToken).get();
            double hba1c = Helper.calcHbA1c(customEntries);
            System.out.println("(Async from - to) Custom hba1c from " + customEntries.size() + " entries: " + String.format(Locale.US, "%.1f%%", hba1c));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        // Get last 10 entries async
        int noOfEntries = 10;
        try {
            List<Entry> nEntries = Helper.getLastNEntriesAsync(noOfEntries, nightscoutUrl, apiSecret, nightscoutToken).get();
            double hba1c = Helper.calcHbA1c(nEntries);
            System.out.println("(Async last 10) Custom hba1c from " + nEntries.size() + " entries: " + String.format(Locale.US, "%.1f%%", hba1c));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        // Get entries between from and to
        List<Entry> customEntries = Helper.getEntriesFromTo(from, to, nightscoutUrl, apiSecret, nightscoutToken);
        double hba1c = Helper.calcHbA1c(customEntries);
        System.out.println("(Sync from - to) Custom hba1c from " + customEntries.size() + " entries: " + String.format(Locale.US, "%.1f%%", hba1c));
    }


    private void registerListeners() {
        this.currentEntry.addObserver(evt -> {
            entries.add(currentEntry.get());
            System.out.println("Current entry changed to: " + evt.getValue());
            // Calculate HBa1C
            hba1c.set(Helper.calcHbA1c(this.entries));
        });


        this.entries.addListChangeObserver(ListChangeEvt.ADDED, e -> {
            // Remove entries older than 90 days
            long now           = Instant.now().getEpochSecond();
            long ninetyDaysAgo = now - interval.getSeconds();
            this.entries.removeAll(this.entries.parallelStream().filter(entry -> entry.datelong() < ninetyDaysAgo).collect(Collectors.toList()));
            // Calculate HBa1C
            hba1c.set(Helper.calcHbA1c(this.entries));
        });
        
        this.hba1c.addObserver(evt -> System.out.println("Estimated HbA1c: " + String.format(Locale.US, "%.1f%%" , evt.getValue())));
    }


    public static void main(String[] args) {
        new Main();
    }
}