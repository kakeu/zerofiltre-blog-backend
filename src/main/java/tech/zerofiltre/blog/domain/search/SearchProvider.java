package tech.zerofiltre.blog.domain.search;

import tech.zerofiltre.blog.domain.search.model.SearchResult;

public interface SearchProvider {

    SearchResult search(String keyword);
}
