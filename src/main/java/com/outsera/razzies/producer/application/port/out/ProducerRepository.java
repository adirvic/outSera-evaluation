package com.outsera.razzies.producer.application.port.out;

import com.outsera.razzies.shared.application.PageQuery;
import com.outsera.razzies.shared.application.PageResult;
import com.outsera.razzies.producer.domain.Producer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Outbound port: storage of producers. */
public interface ProducerRepository {

    PageResult<Producer> findAll(PageQuery page);

    PageResult<Producer> findByNameContaining(String name, PageQuery page);

    Optional<Producer> findById(long id);

    List<Producer> findByNames(Collection<String> names);

    /** Persists new producers and returns them with their ids. */
    List<Producer> saveAll(Collection<Producer> producers);
}
