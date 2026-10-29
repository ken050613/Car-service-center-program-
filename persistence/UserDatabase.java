package apu.persistence;

import apu.model.CounterStaff;
import apu.model.Customer;
import apu.model.Manager;
import apu.model.Technician;

import java.util.ArrayList;
import java.util.List;

/** In-memory aggregate of all user subtypes (persisted as plain text in {@link apu.repository.UserRepository}). */
public class UserDatabase {
    private List<Manager> managers = new ArrayList<>();
    private List<CounterStaff> counterStaff = new ArrayList<>();
    private List<Technician> technicians = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();

    public List<Manager> getManagers() {
        return managers;
    }

    public void setManagers(List<Manager> managers) {
        this.managers = managers != null ? managers : new ArrayList<>();
    }

    public List<CounterStaff> getCounterStaff() {
        return counterStaff;
    }

    public void setCounterStaff(List<CounterStaff> counterStaff) {
        this.counterStaff = counterStaff != null ? counterStaff : new ArrayList<>();
    }

    public List<Technician> getTechnicians() {
        return technicians;
    }

    public void setTechnicians(List<Technician> technicians) {
        this.technicians = technicians != null ? technicians : new ArrayList<>();
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers != null ? customers : new ArrayList<>();
    }
}
