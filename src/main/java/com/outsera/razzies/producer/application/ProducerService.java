package com.outsera.razzies.producer.application;

import com.outsera.razzies.shared.application.PageQuery;
import com.outsera.razzies.shared.application.PageResult;
import com.outsera.razzies.shared.domain.Names;
import com.outsera.razzies.shared.domain.ResourceNotFoundException;
import com.outsera.razzies.producer.application.port.in.FindProducersUseCase;
import com.outsera.razzies.producer.application.port.in.ResolveProducersUseCase;
import com.outsera.razzies.producer.application.port.out.ProducerRepository;
import com.outsera.razzies.producer.domain.Producer;
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
public class ProducerService implements FindProducersUseCase, ResolveProducersUseCase {

    private final ProducerRepository repository;

    public ProducerService(ProducerRepository repository) {
        this.repository = repository;
    }

    @Override
    public PageResult<Producer> list(String name, PageQuery page) {
        return name == null || name.isBlank()
                ? repository.findAll(page)
                : repository.findByNameContaining(name.strip(), page);
    }

    @Override
    public Producer get(long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Producer", id));
    }

    /** Issues one select plus one batched insert, regardless of the number of names. */
    @Override
    @Transactional
    public Map<String, Producer> resolveByNames(Collection<String> names) {
        List<String> normalized = Names.normalizeAll(names);
        if (normalized.isEmpty()) {
            return Map.of();
        }
        Map<String, Producer> existing = repository.findByNames(normalized).stream()
                .collect(Collectors.toMap(Producer::name, Function.identity()));
        List<Producer> missing = normalized.stream()
                .filter(name -> !existing.containsKey(name))
                .map(Producer::named)
                .toList();
        if (!missing.isEmpty()) {
            repository.saveAll(missing).forEach(producer -> existing.put(producer.name(), producer));
        }
        var resolved = new LinkedHashMap<String, Producer>();
        normalized.forEach(name -> resolved.put(name, existing.get(name)));
        return resolved;
    }
}
