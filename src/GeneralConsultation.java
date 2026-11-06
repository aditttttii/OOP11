public class GeneralConsultation extends Appointment implements Billable {
    private double fee;
    public GeneralConsultation(String appointmentId, String patientName, Doctor doctor, double fee) {
        super(appointmentId, patientName, doctor);
        this.fee = fee;
    }
    @Override
    public String getAppointmentType() {
        return "General Consultation";
    }
    @Override
    public double calculateCost() {
        return fee;
    }
    @Override
    public void generateInvoice() {
        System.out.println("   INVOICE: " + getAppointmentId() + " | " + getPatientName()
                + " | total: " + calculateCost());
    }
}
