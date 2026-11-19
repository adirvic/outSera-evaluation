package com.outsera.razzies.producer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "producer")
public class Producer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "producer_seq")
    @SequenceGenerator(name = "producer_seq", sequenceName = "producer_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    protected Producer() {
        // required by JPA
    }

    public Producer(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Producer other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
