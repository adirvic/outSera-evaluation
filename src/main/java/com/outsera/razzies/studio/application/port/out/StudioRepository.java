package com.outsera.razzies.studio.application.port.out;

import com.outsera.razzies.shared.application.PageQuery;
import com.outsera.razzies.shared.application.PageResult;
import com.outsera.razzies.studio.domain.Studio;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Outbound port: storage of studios. */
public interface StudioRepository {

    PageResult<Studio> findAll(PageQuery page);

    PageResult<Studio> findByNameContaining(String name, PageQuery page);

    Optional<Studio> findById(long id);

    List<Studio> findByNames(Collection<String> names);

    /** Persists new studios and returns them with their ids. */
    List<Studio> saveAll(Collection<Studio> studios);
}
