package tech.zerofiltre.blog.infra.providers.database.search;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.search.SearchProvider;
import tech.zerofiltre.blog.domain.search.model.SearchResult;
import tech.zerofiltre.blog.infra.providers.database.article.ArticleJPARepository;
import tech.zerofiltre.blog.infra.providers.database.article.model.ArticleJPA;
import tech.zerofiltre.blog.infra.providers.database.course.CourseJPARepository;
import tech.zerofiltre.blog.infra.providers.database.course.LessonJPARepository;
import tech.zerofiltre.blog.infra.providers.database.course.model.CourseJPA;
import tech.zerofiltre.blog.infra.providers.database.course.model.LessonWithCourseIdJPA;
import tech.zerofiltre.blog.infra.providers.database.search.mapper.SearchResultJpaMapper;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DBSearchProvider implements SearchProvider {

    private final ArticleJPARepository articleJPARepository;
    private final CourseJPARepository courseJPARepository;
    private final LessonJPARepository lessonJPARepository;
    private final SearchResultJpaMapper mapper;


    @Override
    @Cacheable(value = "search-results", key = "#keyword")
    public SearchResult search(String keyword) {
        List<ArticleJPA> foundArticles = articleJPARepository.findByKeyword(keyword, Status.PUBLISHED);
        List<CourseJPA> foundCourses = courseJPARepository.findByKeyword(keyword, Status.PUBLISHED);
        List<LessonWithCourseIdJPA> foundLessons = lessonJPARepository.findByKeyword(keyword, Status.PUBLISHED);
        return mapper.fromJPAs(foundArticles, foundCourses, foundLessons);
    }
}
