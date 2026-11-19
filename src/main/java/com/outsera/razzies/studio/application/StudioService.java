package com.outsera.razzies.studio.application;

import com.outsera.razzies.shared.application.PageQuery;
import com.outsera.razzies.shared.application.PageResult;
import com.outsera.razzies.shared.domain.Names;
import com.outsera.razzies.shared.domain.ResourceNotFoundException;
import com.outsera.razzies.studio.application.port.in.FindStudiosUseCase;
import com.outsera.razzies.studio.application.port.in.ResolveStudiosUseCase;
import com.outsera.razzies.studio.application.port.out.StudioRepository;
import com.outsera.razzies.studio.domain.Studio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StudioService implements FindStudiosUseCase, ResolveStudiosUseCase {

    private final StudioRepository repository;

    public StudioService(StudioRepository repository) {
        this.repository = repository;
    }

    @Override
    public PageResult<Studio> list(String name, PageQuery page) {
        return name == null || name.isBlank()
                ? repository.findAll(page)
                : repository.findByNameContaining(name.strip(), page);
    }

    @Override
    public Studio get(long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Studio", id));
    }

    /** Issues one select plus one batched insert, regardless of the number of names. */
    @Override
    @Transactional
    public Map<String, Studio> resolveByNames(Collection<String> names) {
        List<String> normalized = Names.normalizeAll(names);
        if (normalized.isEmpty()) {
            return Map.of();
        }
        Map<String, Studio> existing = repository.findByNames(normalized).stream()
                .collect(Collectors.toMap(Studio::name, Function.identity()));
        List<Studio> missing = normalized.stream()
                .filter(name -> !existing.containsKey(name))
                .map(Studio::named)
                .toList();
        if (!missing.isEmpty()) {
            repository.saveAll(missing).forEach(studio -> existing.put(studio.name(), studio));
        }
        var resolved = new LinkedHashMap<String, Studio>();
        normalized.forEach(name -> resolved.put(name, existing.get(name)));
        return resolved;
    }
}
