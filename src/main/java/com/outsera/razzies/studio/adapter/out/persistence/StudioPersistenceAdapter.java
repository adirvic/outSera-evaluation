package com.outsera.razzies.studio.adapter.out.persistence;

import com.outsera.razzies.shared.adapter.out.persistence.PersistencePages;
import com.outsera.razzies.shared.application.PageQuery;
import com.outsera.razzies.shared.application.PageResult;
import com.outsera.razzies.studio.application.port.out.StudioRepository;
import com.outsera.razzies.studio.domain.Studio;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
class StudioPersistenceAdapter implements StudioRepository {

    private final StudioJpaRepository jpa;

    StudioPersistenceAdapter(StudioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public PageResult<Studio> findAll(PageQuery page) {
        return PersistencePages.fetch(() -> jpa.findAll(PersistencePages.toPageable(page)), StudioPersistenceAdapter::toDomain);
    }

    @Override
    public PageResult<Studio> findByNameContaining(String name, PageQuery page) {
        return PersistencePages.fetch(() -> jpa.findByNameContainingIgnoreCase(name, PersistencePages.toPageable(page)),
                StudioPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Studio> findById(long id) {
        return jpa.findById(id).map(StudioPersistenceAdapter::toDomain);
    }

    @Override
    public List<Studio> findByNames(Collection<String> names) {
        return jpa.findByNameIn(names).stream().map(StudioPersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<Studio> saveAll(Collection<Studio> studios) {
        List<StudioJpaEntity> entities = studios.stream().map(studio -> new StudioJpaEntity(studio.name())).toList();
        return jpa.saveAll(entities).stream().map(StudioPersistenceAdapter::toDomain).toList();
    }

    static Studio toDomain(StudioJpaEntity entity) {
        return new Studio(entity.getId(), entity.getName());
    }
}
