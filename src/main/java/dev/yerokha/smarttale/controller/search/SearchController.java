package dev.yerokha.smarttale.controller.search;

import dev.yerokha.smarttale.dto.CustomPage;
import dev.yerokha.smarttale.service.SearchService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public CustomPage search(Authentication authentication,
                             @RequestParam String q,
                             @RequestParam(required = false) String con,
                             @RequestParam(defaultValue = "true") boolean iDD,
                             @RequestParam(required = false, defaultValue = "0") int page,
                             @RequestParam(required = false, defaultValue = "5") int size) {
        return searchService.search(authentication, q, con, iDD, page, size);
    }
}
