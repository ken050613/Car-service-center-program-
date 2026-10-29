package apu.repository;

import apu.model.Appointment;
import apu.model.AppointmentStatus;
import apu.model.ServiceKind;
import apu.persistence.DataPaths;
import apu.persistence.TextLineFiles;
import apu.util.ServiceCodeParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AppointmentRepository {
    private List<Appointment> list = new ArrayList<>();

    public void load() throws IOException {
        list = new ArrayList<>();
        for (String raw : TextLineFiles.readNonCommentLines(DataPaths.appointments())) {
            String[] p = TextLineFiles.split(raw.trim());
            if (p.length < 12 || !"A".equals(p[0])) {
                continue;
            }
            try {
                List<String> codes = ServiceCodeParser.parseCommaSeparated(p[4]);
                ServiceKind kind = p[5].isEmpty() ? ServiceKind.NORMAL : ServiceKind.valueOf(p[5]);
                AppointmentStatus st = AppointmentStatus.valueOf(p[6]);
                String tech = TextLineFiles.emptyToNull(p[7]);
                String counter = TextLineFiles.emptyToNull(p[8]);
                LocalDateTime sched = p[9].isEmpty() ? null : LocalDateTime.parse(p[9]);
                LocalDateTime created = p[11].isEmpty() ? LocalDateTime.now() : LocalDateTime.parse(p[11]);
                Appointment a = new Appointment(
                        p[1], p[2], p[3], codes, kind, st, tech, counter, sched, p[10], created
                );
                list.add(a);
            } catch (Exception ignored) {
            }
        }
    }

    public void save() throws IOException {
        Files.createDirectories(DataPaths.root());
        List<String> lines = new ArrayList<>();
        lines.add("# appointments | A|id|customerId|vehicle|codesComma|kind|status|techId|counterStaffId|scheduledAt|notes|createdAt");
        for (Appointment a : list) {
            String codes = String.join(",", a.getServiceCodes());
            String tech = a.getTechnicianId() != null ? a.getTechnicianId() : "";
            String counter = a.getCreatedByCounterStaffId() != null ? a.getCreatedByCounterStaffId() : "";
            String sched = a.getScheduledAt() != null ? a.getScheduledAt().toString() : "";
            lines.add(TextLineFiles.join(
                    "A",
                    a.getId(),
                    a.getCustomerId(),
                    a.getVehicleDescription(),
                    codes,
                    a.getPrimaryKind().name(),
                    a.getStatus().name(),
                    tech,
                    counter,
                    sched,
                    a.getNotes(),
                    a.getCreatedAt().toString()
            ));
        }
        Files.write(DataPaths.appointments(), lines, StandardCharsets.UTF_8);
    }

    public void add(Appointment a) throws IOException {
        list.add(a);
        save();
    }

    public void update(Appointment a) throws IOException {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(a.getId())) {
                list.set(i, a);
                save();
                return;
            }
        }
        throw new IllegalArgumentException("Appointment not found: " + a.getId());
    }

    public List<Appointment> all() {
        return new ArrayList<>(list);
    }

    public Optional<Appointment> findById(String id) {
        return list.stream().filter(a -> a.getId().equals(id)).findFirst();
    }

    public List<Appointment> byCustomer(String customerId) {
        return list.stream().filter(a -> a.getCustomerId().equals(customerId)).collect(Collectors.toList());
    }

    public List<Appointment> byTechnician(String technicianId) {
        return list.stream()
                .filter(a -> technicianId.equals(a.getTechnicianId()))
                .collect(Collectors.toList());
    }
}
