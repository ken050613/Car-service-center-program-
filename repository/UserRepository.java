package apu.repository;

import apu.model.*;
import apu.persistence.DataPaths;
import apu.persistence.TextLineFiles;
import apu.persistence.UserDatabase;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class UserRepository {
    private UserDatabase db = new UserDatabase();

    public void load() throws IOException {
        db = new UserDatabase();
        var path = DataPaths.users();
        for (String raw : TextLineFiles.readNonCommentLines(path)) {
            String[] p = TextLineFiles.split(raw.trim());
            if (p.length < 2) {
                continue;
            }
            try {
                switch (p[0]) {
                    case "M" -> {
                        if (p.length >= 6) {
                            db.getManagers().add(new Manager(p[1], p[2], p[3], p[4], p[5]));
                        }
                    }
                    case "S" -> {
                        if (p.length >= 6) {
                            db.getCounterStaff().add(new CounterStaff(p[1], p[2], p[3], p[4], p[5]));
                        }
                    }
                    case "T" -> {
                        if (p.length >= 7) {
                            db.getTechnicians().add(new Technician(p[1], p[2], p[3], p[4], p[5], p[6]));
                        }
                    }
                    case "C" -> {
                        if (p.length >= 8) {
                            db.getCustomers().add(new Customer(p[1], p[2], p[3], p[4], p[5], p[6], p[7]));
                        }
                    }
                    default -> {
                    }
                }
            } catch (Exception ignored) {
                // skip malformed line
            }
        }
    }

    public void save() throws IOException {
        Files.createDirectories(DataPaths.root());
        List<String> lines = new ArrayList<>();
        lines.add("# APU-ASC users | type M=manager S=counter T=technician C=customer");
        for (Manager m : db.getManagers()) {
            lines.add(TextLineFiles.join("M", m.getId(), m.getUsername(), m.getPasswordHash(), m.getFullName(), m.getEmail()));
        }
        for (CounterStaff c : db.getCounterStaff()) {
            lines.add(TextLineFiles.join("S", c.getId(), c.getUsername(), c.getPasswordHash(), c.getFullName(), c.getEmail()));
        }
        for (Technician t : db.getTechnicians()) {
            lines.add(TextLineFiles.join("T", t.getId(), t.getUsername(), t.getPasswordHash(), t.getFullName(), t.getEmail(), t.getSpecialty()));
        }
        for (Customer c : db.getCustomers()) {
            lines.add(TextLineFiles.join("C", c.getId(), c.getUsername(), c.getPasswordHash(), c.getFullName(), c.getEmail(), c.getPhone(), c.getVehicleRegistration()));
        }
        Files.write(DataPaths.users(), lines, StandardCharsets.UTF_8);
    }

    public void add(User user) throws IOException {
        if (user instanceof Manager m) {
            db.getManagers().add(m);
        } else if (user instanceof CounterStaff c) {
            db.getCounterStaff().add(c);
        } else if (user instanceof Technician t) {
            db.getTechnicians().add(t);
        } else if (user instanceof Customer cu) {
            db.getCustomers().add(cu);
        } else {
            throw new IllegalArgumentException("Unknown user type");
        }
        save();
    }

    public void replace(User user) throws IOException {
        removeById(user.getId());
        add(user);
    }

    public void deleteById(String id) throws IOException {
        removeById(id);
        save();
    }

    private void removeById(String id) {
        db.getManagers().removeIf(u -> u.getId().equals(id));
        db.getCounterStaff().removeIf(u -> u.getId().equals(id));
        db.getTechnicians().removeIf(u -> u.getId().equals(id));
        db.getCustomers().removeIf(u -> u.getId().equals(id));
    }

    public List<Manager> managers() {
        return new ArrayList<>(db.getManagers());
    }

    public long managerCount() {
        return db.getManagers().size();
    }

    public boolean usernameTakenByOther(String username, String excludeUserId) {
        return allUsers().stream()
                .filter(u -> excludeUserId == null || !u.getId().equals(excludeUserId))
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
    }

    public Optional<User> findByUsername(String username) {
        return allUsers().stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst();
    }

    public Optional<User> findById(String id) {
        return allUsers().stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    public List<User> allUsers() {
        List<User> list = new ArrayList<>();
        list.addAll(db.getManagers());
        list.addAll(db.getCounterStaff());
        list.addAll(db.getTechnicians());
        list.addAll(db.getCustomers());
        return list;
    }

    public Stream<Technician> technicians() {
        return db.getTechnicians().stream();
    }

    public List<Customer> customers() {
        return new ArrayList<>(db.getCustomers());
    }

    public boolean usernameExists(String username) {
        return findByUsername(username).isPresent();
    }
}
