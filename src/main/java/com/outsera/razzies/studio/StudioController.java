package com.outsera.razzies.studio;

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
@RequestMapping("/api/v1/studios")
public class StudioController {

    private final StudioService service;

    public StudioController(StudioService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<NamedResource> list(@RequestParam(required = false) String name,
                                            @PageableDefault(size = 20, sort = "name") @ParameterObject Pageable pageable) {
        return PageResponse.of(service.list(name, pageable), StudioController::toResource);
    }

    @GetMapping("/{id}")
    public NamedResource get(@PathVariable long id) {
        return toResource(service.get(id));
    }

    private static NamedResource toResource(Studio studio) {
        return new NamedResource(studio.getId(), studio.getName());
    }
}
