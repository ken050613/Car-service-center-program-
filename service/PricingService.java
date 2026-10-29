package apu.service;

import apu.model.ServiceItem;
import apu.repository.CatalogRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/** Managers set catalogue prices. */
public class PricingService {
    private final CatalogRepository catalog;

    public PricingService(CatalogRepository catalog) {
        this.catalog = catalog;
    }

    public List<ServiceItem> listPrices() {
        return catalog.all();
    }

    public void setOrUpdatePrice(String code, String description, BigDecimal price) throws IOException {
        var opt = catalog.find(code);
        if (opt.isPresent()) {
            ServiceItem existing = opt.get();
            existing.setDescription(description);
            existing.setUnitPrice(price);
            catalog.put(existing);
        } else {
            catalog.put(new ServiceItem(code, description, price));
        }
    }
}
