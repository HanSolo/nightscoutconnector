package eu.hansolo.nightscoutconnector;

import java.time.format.DateTimeFormatter;


public interface TimeInterval {
    int getNoOfEntries();

    int getHours();

    long getSeconds();

    DateTimeFormatter getFormatter();
}
