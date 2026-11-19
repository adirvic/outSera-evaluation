package com.outsera.razzies.studio.application.port.in;

import com.outsera.razzies.studio.domain.Studio;

import java.util.Collection;
import java.util.Map;

public interface ResolveStudiosUseCase {

    /**
     * Returns, keyed by normalized name and in the given order, the studios with the given names,
     * creating the ones that do not exist yet.
     */
    Map<String, Studio> resolveByNames(Collection<String> names);
}
