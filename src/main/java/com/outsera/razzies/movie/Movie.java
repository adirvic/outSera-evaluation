package com.outsera.razzies.movie;

import com.outsera.razzies.producer.Producer;
import com.outsera.razzies.studio.Studio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "movie", indexes = {
        @Index(name = "idx_movie_year", columnList = "release_year"),
        @Index(name = "idx_movie_winner", columnList = "winner")
})
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "movie_seq")
    @SequenceGenerator(name = "movie_seq", sequenceName = "movie_seq", allocationSize = 50)
    private Long id;

    // "year" is a reserved word in H2, hence the explicit column name.
    @Column(name = "release_year", nullable = false)
    private int year;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private boolean winner;

    @ManyToMany
    @JoinTable(name = "movie_producer",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "producer_id"))
    private Set<Producer> producers = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "movie_studio",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "studio_id"))
    private Set<Studio> studios = new LinkedHashSet<>();

    protected Movie() {
        // required by JPA
    }

    public Movie(int year, String title, boolean winner,
                 Collection<Producer> producers, Collection<Studio> studios) {
        update(year, title, winner, producers, studios);
    }

    public final void update(int year, String title, boolean winner,
                             Collection<Producer> producers, Collection<Studio> studios) {
        this.year = year;
        this.title = Objects.requireNonNull(title, "title");
        this.winner = winner;
        this.producers.clear();
        this.producers.addAll(producers);
        this.studios.clear();
        this.studios.addAll(studios);
    }

    public Long getId() {
        return id;
    }

    public int getYear() {
        return year;
    }

    public String getTitle() {
        return title;
    }

    public boolean isWinner() {
        return winner;
    }

    public Set<Producer> getProducers() {
        return Set.copyOf(producers);
    }

    public Set<Studio> getStudios() {
        return Set.copyOf(studios);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Movie other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
