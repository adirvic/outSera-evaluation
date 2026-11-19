package com.outsera.razzies.producer.adapter.out.persistence;

import com.outsera.razzies.shared.adapter.out.persistence.PersistencePages;
import com.outsera.razzies.shared.application.PageQuery;
import com.outsera.razzies.shared.application.PageResult;
import com.outsera.razzies.producer.application.port.out.ProducerRepository;
import com.outsera.razzies.producer.domain.Producer;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
class ProducerPersistenceAdapter implements ProducerRepository {

    private final ProducerJpaRepository jpa;

    ProducerPersistenceAdapter(ProducerJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public PageResult<Producer> findAll(PageQuery page) {
        return PersistencePages.fetch(() -> jpa.findAll(PersistencePages.toPageable(page)), ProducerPersistenceAdapter::toDomain);
    }

    @Override
    public PageResult<Producer> findByNameContaining(String name, PageQuery page) {
        return PersistencePages.fetch(() -> jpa.findByNameContainingIgnoreCase(name, PersistencePages.toPageable(page)),
                ProducerPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Producer> findById(long id) {
        return jpa.findById(id).map(ProducerPersistenceAdapter::toDomain);
    }

    @Override
    public List<Producer> findByNames(Collection<String> names) {
        return jpa.findByNameIn(names).stream().map(ProducerPersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<Producer> saveAll(Collection<Producer> producers) {
        List<ProducerJpaEntity> entities = producers.stream().map(producer -> new ProducerJpaEntity(producer.name())).toList();
        return jpa.saveAll(entities).stream().map(ProducerPersistenceAdapter::toDomain).toList();
    }

    static Producer toDomain(ProducerJpaEntity entity) {
        return new Producer(entity.getId(), entity.getName());
    }
}
