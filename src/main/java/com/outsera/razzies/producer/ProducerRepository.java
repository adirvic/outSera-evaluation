package com.outsera.razzies.producer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ProducerRepository extends JpaRepository<Producer, Long> {

    List<Producer> findByNameIn(Collection<String> names);

    Page<Producer> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /** One row per (producer, winning movie): only the two columns needed, in a single query. */
    @Query("""
            select new com.outsera.razzies.producer.ProducerWin(p.name, m.year)
            from Movie m join m.producers p
            where m.winner = true
            """)
    List<ProducerWin> findAllWins();
}
