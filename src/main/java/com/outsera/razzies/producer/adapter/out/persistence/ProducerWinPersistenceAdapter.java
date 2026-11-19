package com.outsera.razzies.producer.adapter.out.persistence;

import com.outsera.razzies.producer.application.port.out.ProducerWinRepository;
import com.outsera.razzies.producer.domain.ProducerWin;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class ProducerWinPersistenceAdapter implements ProducerWinRepository {

    // Only the two columns needed, in a single query over the movie / producer association.
    private static final String WINS = """
            select new com.outsera.razzies.producer.domain.ProducerWin(p.name, m.year)
            from Movie m join m.producers p
            where m.winner = true
            """;

    private final EntityManager entityManager;

    ProducerWinPersistenceAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<ProducerWin> findAllWins() {
        return entityManager.createQuery(WINS, ProducerWin.class).getResultList();
    }
}
