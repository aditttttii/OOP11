import java.util.Objects;

public class MedicalRecord {
    private final String diagnosis;
    private final String prescription;

    public MedicalRecord(String diagnosis, String prescription) {
        this.diagnosis = diagnosis;
        this.prescription = prescription;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public String getPrescription() {
        return prescription;
    }

    public void displayRecord() {
        System.out.println("   Diagnosis: " + diagnosis + " | Prescription: " + prescription);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicalRecord other = (MedicalRecord) o;
        return Objects.equals(diagnosis, other.diagnosis)
                && Objects.equals(prescription, other.prescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diagnosis, prescription);
    }
}