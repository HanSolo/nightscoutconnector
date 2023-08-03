package eu.hansolo.nightscoutconnector;

import java.time.OffsetDateTime;
import java.util.Objects;


public record Entry(String id, double sgv, long datelong, OffsetDateTime date, String dateString, Trend trend, String direction, String device, String type, int utcOffset, int noise, double filtered, double unfiltered, int rssi, double delta, String sysTime) implements Comparable<Entry> {
    // dateString: must be ISO 8601
    // type      : sgv, mbg, cal, etc
    // sgv       : only available if type == sgv
    // direction : only available if type == sgv
    // noise     : only available if type == sgv
    // filtered  : only available if type == sgv
    // unfiltered: only available if type == sgv
    // rssi      : only available if type == sgv

    @Override public int compareTo(final Entry other) { return Long.compare(datelong, other.datelong()); }

    @Override public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Entry that = (Entry) o;
        return Double.compare(that.sgv, sgv) == 0 && datelong == that.datelong && utcOffset == that.utcOffset && noise == that.noise && Double.compare(that.filtered, filtered) == 0 && Double.compare(that.unfiltered, unfiltered) == 0 &&
               rssi == that.rssi && Double.compare(that.delta, delta) == 0 && id.equals(that.id) && Objects.equals(date, that.date) && Objects.equals(dateString, that.dateString) && trend == that.trend &&
               Objects.equals(direction, that.direction) && Objects.equals(device, that.device) && Objects.equals(type, that.type) && Objects.equals(sysTime, that.sysTime);
    }

    @Override public int hashCode() {
        return Objects.hash(id, sgv, datelong, date, dateString, trend, direction, device, type, utcOffset, noise, filtered, unfiltered, rssi, delta, sysTime);
    }

    @Override public String toString() {
        return new StringBuilder().append("{")
                                  .append("\"type\":\"").append(type).append("\",")
                                  .append("\"date\":\"").append(Constants.DTF.format(date)).append("\",")
                                  .append("\"sgv\":").append(sgv)
                                  .append("}")
                                  .toString();
    }
}
