package com.outsera.razzies.studio.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

interface StudioJpaRepository extends JpaRepository<StudioJpaEntity, Long> {

    List<StudioJpaEntity> findByNameIn(Collection<String> names);

    Page<StudioJpaEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
