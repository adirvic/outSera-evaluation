package com.outsera.razzies.movie.adapter.out.persistence;

import com.outsera.razzies.producer.adapter.out.persistence.ProducerJpaEntity;
import com.outsera.razzies.studio.adapter.out.persistence.StudioJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "Movie")
@Table(name = "movie", indexes = {
        @Index(name = "idx_movie_year", columnList = "release_year"),
        @Index(name = "idx_movie_winner", columnList = "winner")
})
public class MovieJpaEntity {

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
    @OrderColumn(name = "position")
    private List<ProducerJpaEntity> producers = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "movie_studio",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "studio_id"))
    @OrderColumn(name = "position")
    private List<StudioJpaEntity> studios = new ArrayList<>();

    protected MovieJpaEntity() {
        // required by JPA
    }

    MovieJpaEntity(int year, String title, boolean winner) {
        update(year, title, winner, List.of(), List.of());
    }

    final void update(int year, String title, boolean winner,
                      List<ProducerJpaEntity> producers, List<StudioJpaEntity> studios) {
        this.year = year;
        this.title = title;
        this.winner = winner;
        this.producers.clear();
        this.producers.addAll(producers);
        this.studios.clear();
        this.studios.addAll(studios);
    }

    Long getId() {
        return id;
    }

    int getYear() {
        return year;
    }

    String getTitle() {
        return title;
    }

    boolean isWinner() {
        return winner;
    }

    List<ProducerJpaEntity> getProducers() {
        return producers;
    }

    List<StudioJpaEntity> getStudios() {
        return studios;
    }
}
