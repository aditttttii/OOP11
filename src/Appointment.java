import java.util.Objects;

public abstract class Appointment implements Comparable<Appointment> {
    public static final String PENDING = "PENDING";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELLED = "CANCELLED";

    private final String appointmentId;
    private final String patientName;
    private String status;

    private final Doctor doctor;
    private MedicalRecord medicalRecord;

    public Appointment(String appointmentId, String patientName, Doctor doctor) {
        this.appointmentId = appointmentId;
        this.patientName = patientName;
        this.doctor = doctor;
        this.status = PENDING;
    }

    public abstract String getAppointmentType();

    public void notifyPatient(NotificationService service, String message) {
        service.sendSMS(patientName, message);
    }

    public void displayDetails() {
        System.out.println(appointmentId + " | " + getAppointmentType() + " | patient: " + patientName
                + " | " + doctor + " | " + status);
        if (medicalRecord != null) {
            medicalRecord.displayRecord();
        }
    }
    public void createMedicalRecord(String diagnosis, String prescription) {
        this.medicalRecord = new MedicalRecord(diagnosis, prescription);
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public MedicalRecord getMedicalRecord() {
        return medicalRecord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Appointment)) return false;
        Appointment other = (Appointment) o;
        return appointmentId.equals(other.appointmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appointmentId);
    }

    @Override
    public int compareTo(Appointment other) {
        return this.appointmentId.compareTo(other.appointmentId);
    }
}
