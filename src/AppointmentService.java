import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.Set;

public class AppointmentService {

    private final Set<Doctor> doctors = new LinkedHashSet<>();
    private final List<Appointment> appointments = new ArrayList<>();

    private final Map<String, Doctor> doctorByName = new HashMap<>();
    private final Map<String, Appointment> appointmentById = new HashMap<>();
    private final Map<String, List<Appointment>> appointmentsByStatus = new HashMap<>();
    private final Map<String, List<Doctor>> doctorsBySpecialty = new HashMap<>();

    private final Map<String, Queue<Appointment>> queues = new HashMap<>();

    private final NotificationService notificationService = new NotificationService();
    private int nextNumber = 1;

    public AppointmentService() {
        appointmentsByStatus.put(Appointment.PENDING, new ArrayList<>());
        appointmentsByStatus.put(Appointment.COMPLETED, new ArrayList<>());
        appointmentsByStatus.put(Appointment.CANCELLED, new ArrayList<>());
    }

    //Doctors: CRUd

    public Doctor addDoctor(String name, String specialty) throws ClinicException {
        if (name == null || name.isEmpty() || specialty == null || specialty.isEmpty()) {
            throw new ClinicException("Name and specialty cannot be empty");
        }
        if (doctorByName.containsKey(name)) {
            throw new ClinicException("Doctor " + name + " already exists");
        }
        Doctor doctor = new Doctor(name, specialty);
        doctors.add(doctor);
        doctorByName.put(name, doctor);
        addToSpecialtyIndex(doctor);
        queues.put(name, new ArrayDeque<>());
        return doctor;
    }

    public Doctor findDoctor(String name) throws ClinicException {
        Doctor doctor = doctorByName.get(name);
        if (doctor == null) {
            throw new ClinicException("Doctor " + name + " not found");
        }
        return doctor;
    }
    public void updateSpecialty(String doctorName, String newSpecialty) throws ClinicException {
        if (newSpecialty == null || newSpecialty.isEmpty()) {
            throw new ClinicException("Specialty cannot be empty");
        }
        Doctor doctor = findDoctor(doctorName);
        removeFromSpecialtyIndex(doctor);
        doctor.setSpecialty(newSpecialty);
        addToSpecialtyIndex(doctor);
    }

    public void removeDoctor(String doctorName) throws ClinicException {
        Doctor doctor = findDoctor(doctorName);
        Queue<Appointment> queue = queues.remove(doctorName);
        for (Appointment appointment : queue) {
            changeStatus(appointment, Appointment.CANCELLED);
            appointment.notifyPatient(notificationService,
                    "Your appointment " + appointment.getAppointmentId() + " was cancelled: doctor left.");
        }
        doctors.remove(doctor);
        doctorByName.remove(doctorName);
        removeFromSpecialtyIndex(doctor);
    }

    public Appointment bookAppointment(String patientName, String doctorName, double fee)
            throws ClinicException {
        if (patientName == null || patientName.isEmpty()) {
            throw new ClinicException("Patient name cannot be empty");
        }
        if (fee < 0) {
            throw new ClinicException("Fee cannot be negative");
        }
        Doctor doctor = findDoctor(doctorName);
        String id = String.format("A%03d", nextNumber++);
        Appointment appointment = new GeneralConsultation(id, patientName, doctor, fee);

        appointments.add(appointment);
        appointmentById.put(id, appointment);
        appointmentsByStatus.get(Appointment.PENDING).add(appointment);
        queues.get(doctorName).offer(appointment);

        appointment.notifyPatient(notificationService,
                "Appointment " + id + " booked with " + doctor.getName() + ".");
        return appointment;
    }

    public void cancelAppointment(String id) throws ClinicException {
        Appointment appointment = findAppointment(id);
        if (!appointment.getStatus().equals(Appointment.PENDING)) {
            throw new ClinicException("Only PENDING appointments can be cancelled");
        }
        queues.get(appointment.getDoctor().getName()).remove(appointment);
        changeStatus(appointment, Appointment.CANCELLED);
        appointment.notifyPatient(notificationService, "Appointment " + id + " was cancelled.");
    }
    public Appointment processNext(String doctorName, String diagnosis, String prescription)
            throws ClinicException {
        findDoctor(doctorName);
        Appointment appointment = queues.get(doctorName).poll();
        if (appointment == null) {
            return null;
        }
        appointment.createMedicalRecord(diagnosis, prescription);
        changeStatus(appointment, Appointment.COMPLETED);
        appointment.notifyPatient(notificationService, "Your appointment " + appointment.getAppointmentId()
                + " is completed. Thank you!");
        if (appointment instanceof Billable) {
            ((Billable) appointment).generateInvoice();
        }
        return appointment;
    }
    public Appointment peekNext(String doctorName) throws ClinicException {
        findDoctor(doctorName);
        return queues.get(doctorName).peek();
    }

    public List<Appointment> viewQueue(String doctorName) throws ClinicException {
        findDoctor(doctorName);
        return new ArrayList<>(queues.get(doctorName));
    }

    //Appointments: CRUD

    public Appointment findAppointment(String id) throws ClinicException {
        Appointment appointment = appointmentById.get(id);         // O(1)
        if (appointment == null) {
            throw new ClinicException("Appointment " + id + " not found");
        }
        return appointment;
    }
    public void removeAppointment(String id) throws ClinicException {
        Appointment appointment = findAppointment(id);
        Queue<Appointment> queue = queues.get(appointment.getDoctor().getName());
        if (queue != null) {
            queue.remove(appointment);
        }
        appointments.remove(appointment);
        appointmentById.remove(id);
        appointmentsByStatus.get(appointment.getStatus()).remove(appointment);
    }
    public List<Appointment> findByPatientName(String part) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment appointment : appointments) {
            if (appointment.getPatientName().toLowerCase().contains(part.toLowerCase())) {
                result.add(appointment);
            }
        }
        return result;
    }
    public List<Appointment> getByStatus(String status) {
        List<Appointment> group = appointmentsByStatus.get(status.toUpperCase());
        if (group == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(group);
    }
    public List<Doctor> getDoctorsBySpecialty(String specialty) {
        List<Doctor> group = doctorsBySpecialty.get(specialty.toLowerCase());
        if (group == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(group);
    }
    public List<Appointment> getAppointmentsNaturalOrder() {
        List<Appointment> list = new ArrayList<>(appointments);
        Collections.sort(list);
        return list;
    }
    public List<Appointment> getAppointmentsSorted(Comparator<Appointment> comparator) {
        List<Appointment> list = new ArrayList<>(appointments);
        list.sort(comparator);
        return list;
    }
    public List<Doctor> getDoctorsSorted() {
        List<Doctor> list = new ArrayList<>(doctors);
        Collections.sort(list);
        return list;
    }
    public void printReport() {
        System.out.println("CLINIC REPORT");
        System.out.println("Doctors: " + doctors.size() + ", appointments: " + appointments.size());
        System.out.println("By status:");
        for (Map.Entry<String, List<Appointment>> entry : appointmentsByStatus.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue().size());
        }
        System.out.println("Doctors per specialty:");
        for (Map.Entry<String, List<Doctor>> entry : doctorsBySpecialty.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue().size());
        }
        System.out.println("Per doctor (waiting / completed):");
        Doctor busiest = null;
        int busiestCount = 0;
        for (Doctor doctor : doctors) {
            int waiting = queues.get(doctor.getName()).size();
            int completed = 0;
            for (Appointment appointment : appointmentsByStatus.get(Appointment.COMPLETED)) {
                if (appointment.getDoctor().equals(doctor)) {
                    completed++;
                }
            }
            System.out.println("  " + doctor + ": " + waiting + " / " + completed);
            if (completed > busiestCount) {
                busiestCount = completed;
                busiest = doctor;
            }
        }
        if (busiest != null) {
            System.out.println("Most productive doctor: " + busiest);
        }
        double revenue = 0;
        for (Appointment appointment : appointmentsByStatus.get(Appointment.COMPLETED)) {
            if (appointment instanceof Billable) {
                revenue += ((Billable) appointment).calculateCost();
            }
        }
        System.out.println("Total revenue (completed): " + revenue);
    }
    private void changeStatus(Appointment appointment, String newStatus) {
        appointmentsByStatus.get(appointment.getStatus()).remove(appointment);
        appointment.setStatus(newStatus);
        appointmentsByStatus.get(newStatus).add(appointment);
    }

    private void addToSpecialtyIndex(Doctor doctor) {
        String key = doctor.getSpecialty().toLowerCase();
        List<Doctor> group = doctorsBySpecialty.get(key);
        if (group == null) {
            group = new ArrayList<>();
            doctorsBySpecialty.put(key, group);
        }
        group.add(doctor);
    }
    private void removeFromSpecialtyIndex(Doctor doctor) {
        String key = doctor.getSpecialty().toLowerCase();
        List<Doctor> group = doctorsBySpecialty.get(key);
        if (group != null) {
            group.remove(doctor);
            if (group.isEmpty()) {
                doctorsBySpecialty.remove(key);
            }
        }
    }
}
