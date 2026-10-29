package com.apu.asc.repository;

import com.apu.asc.model.Feedback;
import com.apu.asc.persistence.DataPaths;
import com.apu.asc.persistence.TextLineFiles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FeedbackRepository {
    private List<Feedback> list = new ArrayList<>();

    public void load() throws IOException {
        list = new ArrayList<>();
        for (String raw : TextLineFiles.readNonCommentLines(DataPaths.feedback())) {
            String[] p = TextLineFiles.split(raw.trim());
            if (p.length < 7 || !"F".equals(p[0])) {
                continue;
            }
            try {
                int rating = Integer.parseInt(p[4]);
                LocalDateTime created = p[6].isEmpty() ? LocalDateTime.now() : LocalDateTime.parse(p[6]);
                list.add(new Feedback(p[1], p[2], p[3], rating, p[5], created));
            } catch (Exception ignored) {
            }
        }
    }

    public void save() throws IOException {
        Files.createDirectories(DataPaths.root());
        List<String> lines = new ArrayList<>();
        lines.add("# feedback | F|id|appointmentId|customerId|rating|comment|createdAt");
        for (Feedback f : list) {
            lines.add(TextLineFiles.join(
                    "F",
                    f.getId(),
                    f.getAppointmentId(),
                    f.getCustomerId(),
                    String.valueOf(f.getRating()),
                    f.getComment(),
                    f.getCreatedAt().toString()
            ));
        }
        Files.write(DataPaths.feedback(), lines, StandardCharsets.UTF_8);
    }

    public void add(Feedback f) throws IOException {
        list.add(f);
        save();
    }

    public List<Feedback> all() {
        return new ArrayList<>(list);
    }

    public List<Feedback> byCustomer(String customerId) {
        return list.stream().filter(x -> x.getCustomerId().equals(customerId)).collect(Collectors.toList());
    }

    public boolean existsForAppointment(String appointmentId) {
        return list.stream().anyMatch(x -> x.getAppointmentId().equals(appointmentId));
    }
}
