import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final AppointmentService service = new AppointmentService();

    public static void main(String[] args) {
        loadDemoData();
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("    HOSPITAL APPOINTMENT SCHEDULER  ");
            System.out.println("1. Admin");
            System.out.println("2. Client (patient)");
            System.out.println("3. Doctor");
            System.out.println("4. Lists and reports");
            System.out.println("0. Exit");
            int choice = readInt("Choose role: ");
            try {
                switch (choice) {
                    case 1: adminMenu(); break;
                    case 2: clientMenu(); break;
                    case 3: doctorMenu(); break;
                    case 4: reportsMenu(); break;
                    case 0: running = false; break;
                    default: System.out.println("Unknown option.");
                }
            } catch (ClinicException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        System.out.println("Goodbye!");
    }

    // Admin info
    private static void adminMenu() throws ClinicException {
        System.out.println("-- Admin --");
        System.out.println("1. Add doctor");
        System.out.println("2. Change doctor specialty");
        System.out.println("3. Remove doctor");
        System.out.println("4. Remove appointment");
        int choice = readInt("Choose: ");
        switch (choice) {
            case 1:
                String name = readString("Doctor name: ");
                String specialty = readString("Specialty: ");
                System.out.println("Added: " + service.addDoctor(name, specialty));
                break;
            case 2:
                service.updateSpecialty(readString("Doctor name: "), readString("New specialty: "));
                System.out.println("Updated.");
                break;
            case 3:
                service.removeDoctor(readString("Doctor name: "));
                System.out.println("Doctor removed, waiting appointments cancelled.");
                break;
            case 4:
                service.removeAppointment(readString("Appointment id: "));
                System.out.println("Appointment removed.");
                break;
            default:
                System.out.println("Unknown option.");
        }
    }

    //Client info
    private static void clientMenu() throws ClinicException {
        System.out.println(" Client ");
        System.out.println("1. Book appointment");
        System.out.println("2. Cancel appointment");
        System.out.println("3. Find my appointments");
        int choice = readInt("Choose: ");
        switch (choice) {
            case 1:
                String patient = readString("Your name: ");
                printDoctors(service.getDoctorsSorted());
                String doctor = readString("Doctor name: ");
                double fee = readDouble("Fee: ");
                Appointment a = service.bookAppointment(patient, doctor, fee);
                System.out.println("Booked. Your id: " + a.getAppointmentId());
                break;
            case 2:
                service.cancelAppointment(readString("Appointment id: "));
                break;
            case 3:
                printAppointments(service.findByPatientName(readString("Your name: ")));
                break;
            default:
                System.out.println("Unknown option.");
        }
    }

    // Doctor info
    private static void doctorMenu() throws ClinicException {
        System.out.println("Doctor ");
        System.out.println("1. View my queue");
        System.out.println("2. Take next patient");
        int choice = readInt("Choose: ");
        String doctorName = readString("Your name: ");
        switch (choice) {
            case 1:
                printAppointments(service.viewQueue(doctorName));
                break;
            case 2:
                Appointment next = service.peekNext(doctorName);
                if (next == null) {
                    System.out.println("Queue is empty.");
                    break;
                }
                System.out.println("Next: " + next.getPatientName());
                String diagnosis = readString("Diagnosis: ");
                String prescription = readString("Prescription: ");
                Appointment done = service.processNext(doctorName, diagnosis, prescription);
                done.displayDetails();
                break;
            default:
                System.out.println("Unknown option.");
        }
    }
    private static void reportsMenu() {
        System.out.println("Lists and reports ");
        System.out.println("1. All appointments (by id)");
        System.out.println("2. All appointments by patient name");
        System.out.println("3. All appointments by doctor name");
        System.out.println("4. All appointments by status, then patient");
        System.out.println("5. Appointments with a status");
        System.out.println("6. Doctors (A-Z)");
        System.out.println("7. Doctors by specialty");
        System.out.println("8. Report");
        int choice = readInt("Choose: ");
        switch (choice) {
            case 1: printAppointments(service.getAppointmentsNaturalOrder()); break;
            case 2: printAppointments(service.getAppointmentsSorted(AppointmentComparators.BY_PATIENT_NAME)); break;
            case 3: printAppointments(service.getAppointmentsSorted(AppointmentComparators.BY_DOCTOR_NAME)); break;
            case 4: printAppointments(service.getAppointmentsSorted(AppointmentComparators.BY_STATUS_THEN_PATIENT)); break;
            case 5: printAppointments(service.getByStatus(readString("Status (PENDING/COMPLETED/CANCELLED): "))); break;
            case 6: printDoctors(service.getDoctorsSorted()); break;
            case 7: printDoctors(service.getDoctorsBySpecialty(readString("Specialty: "))); break;
            case 8: service.printReport(); break;
            default: System.out.println("Unknown option.");
        }
    }
    private static void printAppointments(List<Appointment> list) {
        if (list.isEmpty()) {
            System.out.println("Nothing found.");
            return;
        }
        for (Appointment appointment : list) {
            appointment.displayDetails();
        }
    }

    private static void printDoctors(List<Doctor> list) {
        if (list.isEmpty()) {
            System.out.println("Nothing found.");
            return;
        }
        for (Doctor doctor : list) {
            System.out.println(doctor);
        }
    }
    private static String readString(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }
    private static int readInt(String message) {
        while (true) {
            System.out.print(message);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }
    private static double readDouble(String message) {
        while (true) {
            System.out.print(message);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    private static void loadDemoData() {
        try {
            service.addDoctor("Adylova", "Therapist");
            service.addDoctor("Petrova", "Surgeon");
            service.addDoctor("Ahunbaev", "Therapist");
            service.bookAppointment("Aigul", "Ahunbaev", 1500);
            service.bookAppointment("Bakyt", "Adylova", 1500);
            service.bookAppointment("Chyngyz", "Petrova", 2500);
            System.out.println("(demo data loaded)");
        } catch (ClinicException e) {
            System.out.println("Demo data error: " + e.getMessage());
        }
    }
}