package apu.model;

public class Technician extends User {
    private String specialty;

    public Technician(String id, String username, String passwordHash, String fullName, String email, String specialty) {
        super(id, username, passwordHash, fullName, email);
        this.specialty = specialty != null ? specialty : "";
    }

    @Override
    public Role getRole() {
        return Role.TECHNICIAN;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }
}
