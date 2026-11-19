package com.outsera.razzies.studio.adapter.in.web;

import com.outsera.razzies.shared.adapter.in.web.NamedResource;
import com.outsera.razzies.shared.adapter.in.web.PageResponse;
import com.outsera.razzies.shared.adapter.in.web.WebPages;
import com.outsera.razzies.studio.application.port.in.FindStudiosUseCase;
import com.outsera.razzies.studio.domain.Studio;
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

    private final FindStudiosUseCase findStudios;

    public StudioController(FindStudiosUseCase findStudios) {
        this.findStudios = findStudios;
    }

    @GetMapping
    public PageResponse<NamedResource> list(@RequestParam(required = false) String name,
                                            @PageableDefault(size = 20, sort = "name") @ParameterObject Pageable pageable) {
        return PageResponse.of(findStudios.list(name, WebPages.toQuery(pageable)), StudioController::toResource);
    }

    @GetMapping("/{id}")
    public NamedResource get(@PathVariable long id) {
        return toResource(findStudios.get(id));
    }

    static NamedResource toResource(Studio studio) {
        return new NamedResource(studio.id(), studio.name());
    }
}
