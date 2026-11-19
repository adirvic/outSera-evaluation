package com.outsera.razzies.producer.adapter.in.web;

import com.outsera.razzies.shared.adapter.in.web.NamedResource;
import com.outsera.razzies.shared.adapter.in.web.PageResponse;
import com.outsera.razzies.shared.adapter.in.web.WebPages;
import com.outsera.razzies.producer.application.port.in.FindProducersUseCase;
import com.outsera.razzies.producer.application.port.in.GetAwardIntervalsUseCase;
import com.outsera.razzies.producer.domain.Producer;
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

    private final FindProducersUseCase findProducers;
    private final GetAwardIntervalsUseCase getAwardIntervals;

    public ProducerController(FindProducersUseCase findProducers, GetAwardIntervalsUseCase getAwardIntervals) {
        this.findProducers = findProducers;
        this.getAwardIntervals = getAwardIntervals;
    }

    @GetMapping
    public PageResponse<NamedResource> list(@RequestParam(required = false) String name,
                                            @PageableDefault(size = 20, sort = "name") @ParameterObject Pageable pageable) {
        return PageResponse.of(findProducers.list(name, WebPages.toQuery(pageable)), ProducerController::toResource);
    }

    @GetMapping("/{id}")
    public NamedResource get(@PathVariable long id) {
        return toResource(findProducers.get(id));
    }

    /**
     * Producers with the shortest and the longest interval between two consecutive wins.
     * All producers tied at the minimum / maximum are returned.
     */
    @GetMapping("/award-intervals")
    public AwardIntervalsResponse awardIntervals() {
        return AwardIntervalsResponse.from(getAwardIntervals.awardIntervals());
    }

    static NamedResource toResource(Producer producer) {
        return new NamedResource(producer.id(), producer.name());
    }
}
