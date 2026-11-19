package com.outsera.razzies.producer.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

interface ProducerJpaRepository extends JpaRepository<ProducerJpaEntity, Long> {

    List<ProducerJpaEntity> findByNameIn(Collection<String> names);

    Page<ProducerJpaEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
