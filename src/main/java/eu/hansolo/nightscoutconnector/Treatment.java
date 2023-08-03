package eu.hansolo.nightscoutconnector;

import java.util.Objects;


public record Treatment(String id, String eventType, String created_at, String glucose, String glucoseType, double carbs, double protein, double fat, double insulin, String units, String transmitterId, String sensorCode, String notes, String enteredBy) {

    @Override public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Treatment treatment = (Treatment) o;
        return Double.compare(treatment.carbs, carbs) == 0 && Double.compare(treatment.protein, protein) == 0 && Double.compare(treatment.fat, fat) == 0 && Double.compare(treatment.insulin, insulin) == 0 &&
               Objects.equals(id, treatment.id) && Objects.equals(eventType, treatment.eventType) && Objects.equals(created_at, treatment.created_at) && Objects.equals(glucose, treatment.glucose) &&
               Objects.equals(glucoseType, treatment.glucoseType) && Objects.equals(units, treatment.units) && Objects.equals(transmitterId, treatment.transmitterId) && Objects.equals(sensorCode, treatment.sensorCode) &&
               Objects.equals(notes, treatment.notes) && Objects.equals(enteredBy, treatment.enteredBy);
    }

    @Override public int hashCode() {
        return Objects.hash(id, eventType, created_at, glucose, glucoseType, carbs, protein, fat, insulin, units, transmitterId, sensorCode, notes, enteredBy);
    }

    @Override public String toString() {
        return new StringBuilder().append("{")
                                  .append("\"created_at\":\"").append(created_at).append("\",")
                                  .append("\"carbs\":").append(carbs).append(",")
                                  .append("\"insulin\":").append(insulin).append(",")
                                  .append("}")
                                  .toString();
    }
}
