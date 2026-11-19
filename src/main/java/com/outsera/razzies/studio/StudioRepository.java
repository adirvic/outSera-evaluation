package com.outsera.razzies.studio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface StudioRepository extends JpaRepository<Studio, Long> {

    List<Studio> findByNameIn(Collection<String> names);

    Page<Studio> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
