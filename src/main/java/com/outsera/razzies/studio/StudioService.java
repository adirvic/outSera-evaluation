package com.outsera.razzies.studio;

import com.outsera.razzies.shared.Names;
import com.outsera.razzies.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class StudioService {

    private final StudioRepository repository;

    public StudioService(StudioRepository repository) {
        this.repository = repository;
    }

    public Page<Studio> list(String name, Pageable pageable) {
        return name == null || name.isBlank()
                ? repository.findAll(pageable)
                : repository.findByNameContainingIgnoreCase(name.strip(), pageable);
    }

    public Studio get(long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Studio", id));
    }

    /** Returns, keyed by normalized name, the studios with the given names, creating the ones that do not exist yet. */
    @Transactional
    public Map<String, Studio> resolveByNames(Collection<String> names) {
        List<String> normalized = Names.normalizeAll(names);
        if (normalized.isEmpty()) {
            return Map.of();
        }
        Map<String, Studio> existing = repository.findByNameIn(normalized).stream()
                .collect(Collectors.toMap(Studio::getName, Function.identity()));
        List<Studio> missing = normalized.stream()
                .filter(name -> !existing.containsKey(name))
                .map(Studio::new)
                .toList();
        repository.saveAll(missing).forEach(studio -> existing.put(studio.getName(), studio));
        var resolved = new LinkedHashMap<String, Studio>();
        normalized.forEach(name -> resolved.put(name, existing.get(name)));
        return resolved;
    }
}
