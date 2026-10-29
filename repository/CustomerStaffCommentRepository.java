package apu.repository;

import apu.model.CustomerStaffComments;
import apu.persistence.DataPaths;
import apu.persistence.TextLineFiles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CustomerStaffCommentRepository {
    private List<CustomerStaffComments> list = new ArrayList<>();

    public void load() throws IOException {
        list = new ArrayList<>();
        for (String raw : TextLineFiles.readNonCommentLines(DataPaths.customerStaffComments())) {
            String[] p = TextLineFiles.split(raw.trim());
            if (p.length < 7 || !"SC".equals(p[0])) {
                continue;
            }
            try {
                LocalDateTime created = p[6].isEmpty() ? LocalDateTime.now() : LocalDateTime.parse(p[6]);
                list.add(new CustomerStaffComments(p[1], p[2], p[3], p[4], p[5], created));
            } catch (Exception ignored) {
            }
        }
    }

    public void save() throws IOException {
        Files.createDirectories(DataPaths.root());
        List<String> lines = new ArrayList<>();
        lines.add("# customer_staff_comments | SC|id|appointmentId|customerId|commentCounter|commentTechnician|createdAt");
        for (CustomerStaffComments c : list) {
            lines.add(TextLineFiles.join(
                    "SC",
                    c.getId(),
                    c.getAppointmentId(),
                    c.getCustomerId(),
                    c.getCommentCounterStaff(),
                    c.getCommentTechnician(),
                    c.getCreatedAt().toString()
            ));
        }
        Files.write(DataPaths.customerStaffComments(), lines, StandardCharsets.UTF_8);
    }

    public void add(CustomerStaffComments c) throws IOException {
        list.add(c);
        save();
    }

    public void replace(CustomerStaffComments c) throws IOException {
        list.removeIf(x -> x.getId().equals(c.getId()));
        list.add(c);
        save();
    }

    public List<CustomerStaffComments> all() {
        return new ArrayList<>(list);
    }

    public List<CustomerStaffComments> byCustomer(String customerId) {
        return list.stream().filter(x -> x.getCustomerId().equals(customerId)).collect(Collectors.toList());
    }

    public Optional<CustomerStaffComments> findByAppointment(String appointmentId) {
        return list.stream().filter(x -> x.getAppointmentId().equals(appointmentId)).findFirst();
    }
}
