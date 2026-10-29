package apu.model;

public class Manager extends User {
    public Manager(String id, String username, String passwordHash, String fullName, String email) {
        super(id, username, passwordHash, fullName, email);
    }

    @Override
    public Role getRole() {
        return Role.MANAGER;
    }
}
