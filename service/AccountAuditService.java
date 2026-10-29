package apu.service;

import apu.model.User;
import apu.persistence.DataPaths;
import apu.persistence.TextLineFiles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Append-only customer account actions for manager review. */
public class AccountAuditService {

    public record Line(
            String timestamp,
            String action,
            String actorId,
            String actorRole,
            String customerId,
            String username,
            String detail,
            String reason
    ) {}

    private void ensureHeader(Path p) throws IOException {
        Files.createDirectories(DataPaths.root());
        if (!Files.exists(p) || Files.size(p) == 0) {
            Files.writeString(
                    p,
                    "# account_audit | ts|ACTION|actorId|actorRole|customerId|username|detail|reason\n",
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        }
    }

    public void log(User actor, String action, String customerId, String username, String detail, String reason)
            throws IOException {
        Path p = DataPaths.accountAudit();
        ensureHeader(p);
        String line = TextLineFiles.join(
                LocalDateTime.now().toString(),
                action,
                actor.getId(),
                actor.getRole().name(),
                customerId,
                username,
                detail != null ? detail : "",
                reason != null ? reason : ""
        );
        Files.writeString(p, line + "\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public List<Line> readNewestFirst() throws IOException {
        Path p = DataPaths.accountAudit();
        if (!Files.isRegularFile(p)) {
            return List.of();
        }
        List<Line> out = new ArrayList<>();
        for (String raw : Files.readAllLines(p, StandardCharsets.UTF_8)) {
            String t = raw.trim();
            if (t.isEmpty() || t.startsWith("#")) {
                continue;
            }
            String[] f = TextLineFiles.split(raw);
            if (f.length < 8) {
                continue;
            }
            out.add(new Line(f[0], f[1], f[2], f[3], f[4], f[5], f[6], f[7]));
        }
        Collections.reverse(out);
        return out;
    }
}
