package com.outsera.razzies.movie.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface MovieJpaRepository extends JpaRepository<MovieJpaEntity, Long>, JpaSpecificationExecutor<MovieJpaEntity> {
}
