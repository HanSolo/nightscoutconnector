package eu.hansolo.nightscoutconnector;

import java.time.format.DateTimeFormatter;


public class Constants {
    public static final long              INITIAL_DELAY_MS     = 10_000;
    public static final long              INTERVAL_MS          = 60_000;
    public static final String            CURRENT_JSON         = "/api/v2/entries/current.json";
    public static final String            ENTRIES_JSON         = "/api/v2/entries.json";
    public static final String            TREATMENTS_JSON      = "/api/v2/treatments.json";
    public static final DateTimeFormatter DTF                  = DateTimeFormatter.ofPattern("dd/MM/YY HH:mm");
    public static final String            FIELD_ID             = "_id";
    public static final String            FIELD_SGV            = "sgv";
    public static final String            FIELD_DATE           = "date";
    public static final String            FIELD_DATE_STRING    = "dateString";
    public static final String            FIELD_TREND          = "trend";
    public static final String            FIELD_DIRECTION      = "direction";
    public static final String            FIELD_DEVICE         = "device";
    public static final String            FIELD_TYPE           = "type";
    public static final String            FIELD_UTC_OFFSET     = "utcOffset";
    public static final String            FIELD_NOISE          = "noise";
    public static final String            FIELD_FILTERED       = "filtered";
    public static final String            FIELD_UNFILTERED     = "unfiltered";
    public static final String            FIELD_RSSI           = "rssi";
    public static final String            FIELD_DELTA          = "delta";
    public static final String            FIELD_SYS_TIME       = "sysTime";

    public static final String            FIELD_EVENT_TYPE     = "eventType";
    public static final String            FIELD_CREATED_AT     = "created_at";
    public static final String            FIELD_GLUCOSE        = "glucose";
    public static final String            FIELD_GLUCOSE_TYPE   = "glucoseType";
    public static final String            FIELD_CARBS          = "carbs";
    public static final String            FIELD_PROTEIN        = "protein";
    public static final String            FIELD_FAT            = "fat";
    public static final String            FIELD_INSULIN        = "insulin";
    public static final String            FIELD_UNITS          = "units";
    public static final String            FIELD_TRANSMITTER_ID = "transmitterId";
    public static final String            FIELD_SENSOR_CODE    = "sensorCode";
    public static final String            FIELD_NOTES          = "notes";
    public static final String            FIELD_ENTERED_BY     = "enteredBy";
}
