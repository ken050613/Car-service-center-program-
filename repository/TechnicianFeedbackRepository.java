package apu.repository;

import apu.model.TechnicianJobFeedback;
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

public class TechnicianFeedbackRepository {
    private List<TechnicianJobFeedback> list = new ArrayList<>();

    public void load() throws IOException {
        list = new ArrayList<>();
        for (String raw : TextLineFiles.readNonCommentLines(DataPaths.technicianFeedback())) {
            String[] p = TextLineFiles.split(raw.trim());
            if (p.length < 6 || !"TF".equals(p[0])) {
                continue;
            }
            try {
                LocalDateTime created = p[5].isEmpty() ? LocalDateTime.now() : LocalDateTime.parse(p[5]);
                list.add(new TechnicianJobFeedback(p[1], p[2], p[3], p[4], created));
            } catch (Exception ignored) {
            }
        }
    }

    public void save() throws IOException {
        Files.createDirectories(DataPaths.root());
        List<String> lines = new ArrayList<>();
        lines.add("# technician_feedback | TF|id|appointmentId|technicianId|comment|createdAt");
        for (TechnicianJobFeedback f : list) {
            lines.add(TextLineFiles.join(
                    "TF",
                    f.getId(),
                    f.getAppointmentId(),
                    f.getTechnicianId(),
                    f.getComment(),
                    f.getCreatedAt().toString()
            ));
        }
        Files.write(DataPaths.technicianFeedback(), lines, StandardCharsets.UTF_8);
    }

    public void add(TechnicianJobFeedback f) throws IOException {
        list.add(f);
        save();
    }

    public List<TechnicianJobFeedback> all() {
        return new ArrayList<>(list);
    }

    public List<TechnicianJobFeedback> byTechnician(String technicianId) {
        return list.stream().filter(x -> x.getTechnicianId().equals(technicianId)).collect(Collectors.toList());
    }

    public Optional<TechnicianJobFeedback> findByAppointmentAndTechnician(String appointmentId, String technicianId) {
        return list.stream()
                .filter(x -> x.getAppointmentId().equals(appointmentId) && x.getTechnicianId().equals(technicianId))
                .findFirst();
    }
}
