package tech.zerofiltre.blog.infra.entrypoints.rest.search;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.zerofiltre.blog.domain.search.SearchProvider;
import tech.zerofiltre.blog.domain.search.model.SearchResult;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Slf4j
@Validated
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchProvider searchProvider;

    @GetMapping
    public SearchResult search(@RequestParam @Valid @NotNull @NotBlank @Size(min = 3, message = "Query must be at least 5 characters long") String query) {
        return searchProvider.search(query);
    }
}
