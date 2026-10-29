package com.apu.asc.repository;

import com.apu.asc.model.ServiceItem;
import com.apu.asc.model.ServiceKind;
import com.apu.asc.persistence.DataPaths;
import com.apu.asc.persistence.TextLineFiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CatalogRepository {
    private Map<String, ServiceItem> items = new LinkedHashMap<>();

    public void load() throws IOException {
        items.clear();
        for (String raw : TextLineFiles.readNonCommentLines(DataPaths.catalog())) {
            String[] p = TextLineFiles.split(raw.trim());
            if (p.length >= 3) {
                try {
                    ServiceItem s = new ServiceItem(p[0], p[1], new BigDecimal(p[2]));
                    items.put(s.getCode(), s);
                } catch (Exception ignored) {
                }
            }
        }
    }

    public void save() throws IOException {
        Files.createDirectories(DataPaths.root());
        List<String> lines = new ArrayList<>();
        lines.add("# catalog | code|description|unitPrice");
        for (ServiceItem s : items.values()) {
            lines.add(TextLineFiles.join(s.getCode(), s.getDescription(), s.getUnitPrice().toPlainString()));
        }
        Files.write(DataPaths.catalog(), lines, StandardCharsets.UTF_8);
    }

    public List<ServiceItem> all() {
        return new ArrayList<>(items.values());
    }

    public Optional<ServiceItem> find(String code) {
        return Optional.ofNullable(items.get(code));
    }

    public void put(ServiceItem item) throws IOException {
        items.put(item.getCode(), item);
        save();
    }

    public void seedDefaultsIfEmpty() throws IOException {
        if (!items.isEmpty()) {
            return;
        }
        put(new ServiceItem(ServiceKind.NORMAL.catalogCode(), "Normal service (1 hour slot)", new BigDecimal("150.00")));
        put(new ServiceItem(ServiceKind.MAJOR.catalogCode(), "Major service (3 hour slot)", new BigDecimal("450.00")));
        put(new ServiceItem("OIL", "Oil change add-on", new BigDecimal("120.00")));
        put(new ServiceItem("ALIGN", "Wheel alignment add-on", new BigDecimal("80.00")));
        put(new ServiceItem("BRAKE", "Brake inspection add-on", new BigDecimal("200.00")));
    }
}
