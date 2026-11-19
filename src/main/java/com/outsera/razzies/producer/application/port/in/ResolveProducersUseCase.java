package com.outsera.razzies.producer.application.port.in;

import com.outsera.razzies.producer.domain.Producer;

import java.util.Collection;
import java.util.Map;

public interface ResolveProducersUseCase {

    /**
     * Returns, keyed by normalized name and in the given order, the producers with the given names,
     * creating the ones that do not exist yet.
     */
    Map<String, Producer> resolveByNames(Collection<String> names);
}
