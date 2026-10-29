package apu.repository;

import apu.model.Payment;
import apu.model.PaymentMethod;
import apu.persistence.DataPaths;
import apu.persistence.TextLineFiles;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PaymentRepository {
    private List<Payment> list = new ArrayList<>();

    public void load() throws IOException {
        list = new ArrayList<>();
        for (String raw : TextLineFiles.readNonCommentLines(DataPaths.payments())) {
            String[] p = TextLineFiles.split(raw.trim());
            if (p.length < 8 || !"P".equals(p[0])) {
                continue;
            }
            try {
                Payment pay = new Payment(
                        p[1],
                        p[2],
                        p[3],
                        new BigDecimal(p[4]),
                        PaymentMethod.valueOf(p[5]),
                        TextLineFiles.emptyToNull(p[6]),
                        LocalDateTime.parse(p[7])
                );
                list.add(pay);
            } catch (Exception ignored) {
            }
        }
    }

    public void save() throws IOException {
        Files.createDirectories(DataPaths.root());
        List<String> lines = new ArrayList<>();
        lines.add("# payments | P|id|appointmentId|customerId|amount|method|receivedByStaffId|paidAt");
        for (Payment p : list) {
            String staff = p.getReceivedByStaffId() != null ? p.getReceivedByStaffId() : "";
            lines.add(TextLineFiles.join(
                    "P",
                    p.getId(),
                    p.getAppointmentId(),
                    p.getCustomerId(),
                    p.getAmount().toPlainString(),
                    p.getMethod().name(),
                    staff,
                    p.getPaidAt().toString()
            ));
        }
        Files.write(DataPaths.payments(), lines, StandardCharsets.UTF_8);
    }

    public void add(Payment p) throws IOException {
        list.add(p);
        save();
    }

    public List<Payment> all() {
        return new ArrayList<>(list);
    }

    public Optional<Payment> findByAppointmentId(String appointmentId) {
        return list.stream().filter(p -> p.getAppointmentId().equals(appointmentId)).findFirst();
    }

    public List<Payment> byCustomer(String customerId) {
        return list.stream().filter(p -> p.getCustomerId().equals(customerId)).collect(Collectors.toList());
    }
}
