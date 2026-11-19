package com.outsera.razzies.producer;

import com.outsera.razzies.shared.web.NamedResource;
import com.outsera.razzies.shared.web.PageResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/producers")
public class ProducerController {

    private final ProducerService service;

    public ProducerController(ProducerService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<NamedResource> list(@RequestParam(required = false) String name,
                                            @PageableDefault(size = 20, sort = "name") @ParameterObject Pageable pageable) {
        return PageResponse.of(service.list(name, pageable), ProducerController::toResource);
    }

    @GetMapping("/{id}")
    public NamedResource get(@PathVariable long id) {
        return toResource(service.get(id));
    }

    /**
     * Producers with the shortest and the longest interval between two consecutive wins.
     * All producers tied at the minimum / maximum are returned.
     */
    @GetMapping("/award-intervals")
    public AwardIntervals awardIntervals() {
        return service.awardIntervals();
    }

    private static NamedResource toResource(Producer producer) {
        return new NamedResource(producer.getId(), producer.getName());
    }
}
