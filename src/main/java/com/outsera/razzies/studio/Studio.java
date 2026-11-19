package com.outsera.razzies.studio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "studio")
public class Studio {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "studio_seq")
    @SequenceGenerator(name = "studio_seq", sequenceName = "studio_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    protected Studio() {
        // required by JPA
    }

    public Studio(String name) {
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
        if (!(o instanceof Studio other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
