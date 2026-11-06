import java.util.Comparator;
public class AppointmentComparators {
    public static final Comparator<Appointment> BY_PATIENT_NAME =
            (a, b) -> a.getPatientName().compareToIgnoreCase(b.getPatientName());

    public static final Comparator<Appointment> BY_DOCTOR_NAME =
            (a, b) -> a.getDoctor().getName().compareToIgnoreCase(b.getDoctor().getName());

    public static final Comparator<Appointment> BY_STATUS_THEN_PATIENT = (a, b) -> {
        int result = a.getStatus().compareTo(b.getStatus());
        if (result != 0) {
            return result;
        }
        return a.getPatientName().compareToIgnoreCase(b.getPatientName());
    };
}
