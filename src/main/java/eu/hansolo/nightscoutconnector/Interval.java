package eu.hansolo.nightscoutconnector;

import java.time.format.DateTimeFormatter;


public enum Interval implements TimeInterval {
    LAST_90_DAYS(25_920, 2_160, 7_776_000, DateTimeFormatter.ofPattern("DD")),
    LAST_30_DAYS( 8_640,   720, 2_592_000, DateTimeFormatter.ofPattern("DD")),
    LAST_14_DAYS( 4_032,   336, 1_209_600, DateTimeFormatter.ofPattern("DD")),
    LAST_7_DAYS(  2_016,   168,   604_800, DateTimeFormatter.ofPattern("DD")),
    LAST_3_DAYS(    864,    72,   259_200, DateTimeFormatter.ofPattern("HH")),
    LAST_48_HOURS(  576,    48,   172_800, DateTimeFormatter.ofPattern("HH")),
    LAST_24_HOURS(  288,    24,    86_400, DateTimeFormatter.ofPattern("HH")),
    LAST_12_HOURS(  144,    12,    43_200, DateTimeFormatter.ofPattern("HH")),
    LAST_6_HOURS(    72,     6,    21_600, DateTimeFormatter.ofPattern("HH:mm")),
    LAST_3_HOURS(    36,     3,    10_800, DateTimeFormatter.ofPattern("HH:mm"));

    private final int               noOfEntries;
    private final int               hours;
    private final long              seconds;
    private final DateTimeFormatter formatter;


    // ******************** Constructors **************************************
    Interval(final int noOfEntries, final int hours, final int seconds, final DateTimeFormatter formatter) {
        this.noOfEntries = noOfEntries;
        this.hours       = hours;
        this.seconds     = seconds;
        this.formatter   = formatter;
    }


    // ******************** Methods *******************************************
    public String getUiString() {
        switch(this) {
            case LAST_90_DAYS  -> {return "90 days"; }
            case LAST_30_DAYS  -> { return "30 days"; }
            case LAST_7_DAYS   -> { return "7 days"; }
            case LAST_3_DAYS   -> { return "3 days"; }
            case LAST_48_HOURS -> { return "48 hours"; }
            case LAST_24_HOURS -> { return "24 hours"; }
            case LAST_12_HOURS -> { return "12 hours"; }
            case LAST_6_HOURS  -> { return "6 hours"; }
            case LAST_3_HOURS  -> { return "3 hours"; }
            default            -> { return ""; }
        }
    }

    @Override public int getNoOfEntries() { return noOfEntries; }

    @Override public int getHours() { return hours; }

    @Override public long getSeconds() { return seconds; }

    @Override public DateTimeFormatter getFormatter() { return formatter; }
}
