package com.outsera.razzies.producer;

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
public class ProducerService {

    private final ProducerRepository repository;
    private final AwardIntervalCalculator calculator;

    public ProducerService(ProducerRepository repository, AwardIntervalCalculator calculator) {
        this.repository = repository;
        this.calculator = calculator;
    }

    public Page<Producer> list(String name, Pageable pageable) {
        return name == null || name.isBlank()
                ? repository.findAll(pageable)
                : repository.findByNameContainingIgnoreCase(name.strip(), pageable);
    }

    public Producer get(long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Producer", id));
    }

    public AwardIntervals awardIntervals() {
        return calculator.calculate(repository.findAllWins());
    }

    /**
     * Returns, keyed by normalized name, the producers with the given names,
     * creating the ones that do not exist yet.
     * Issues one select plus one batched insert, regardless of the number of names.
     */
    @Transactional
    public Map<String, Producer> resolveByNames(Collection<String> names) {
        List<String> normalized = Names.normalizeAll(names);
        if (normalized.isEmpty()) {
            return Map.of();
        }
        Map<String, Producer> existing = repository.findByNameIn(normalized).stream()
                .collect(Collectors.toMap(Producer::getName, Function.identity()));
        List<Producer> missing = normalized.stream()
                .filter(name -> !existing.containsKey(name))
                .map(Producer::new)
                .toList();
        repository.saveAll(missing).forEach(producer -> existing.put(producer.getName(), producer));
        var resolved = new LinkedHashMap<String, Producer>();
        normalized.forEach(name -> resolved.put(name, existing.get(name)));
        return resolved;
    }
}
