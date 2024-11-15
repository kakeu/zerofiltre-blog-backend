package tech.zerofiltre.blog.infra.providers.database.search.mapper;

import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.search.model.SearchResult;
import tech.zerofiltre.blog.infra.providers.database.article.model.ArticleJPA;
import tech.zerofiltre.blog.infra.providers.database.course.model.CourseJPA;
import tech.zerofiltre.blog.infra.providers.database.course.model.LessonJPA;
import tech.zerofiltre.blog.infra.providers.database.course.model.LessonWithCourseIdJPA;

import java.util.ArrayList;
import java.util.List;

@Component
public class SearchResultJpaMapper {


    private static SearchResult.LessonSearchResult getLessonSearchResult(LessonJPA lessonJPA, long courseId) {
        SearchResult.LessonSearchResult lessonSearchResult = new SearchResult.LessonSearchResult();
        lessonSearchResult.setId(lessonJPA.getId());
        lessonSearchResult.setTitle(lessonJPA.getTitle());
        String resultContent = lessonJPA.getContent() == null || lessonJPA.getContent().length() < 100 ? lessonJPA.getContent() : lessonJPA.getContent().substring(0, 100) + "...";
        lessonSearchResult.setContent(resultContent);
        lessonSearchResult.setCourseId(courseId);
        return lessonSearchResult;
    }

    public SearchResult fromJPAs(List<ArticleJPA> foundArticles, List<CourseJPA> foundCourses, List<LessonWithCourseIdJPA> foundLessons) {
        SearchResult result = new SearchResult();
        for (ArticleJPA articleJPA : foundArticles) {
            if (result.getArticles() == null) result.setArticles(new ArrayList<>());
            SearchResult.ArticleSearchResult articleSearchResult = new SearchResult.ArticleSearchResult();
            articleSearchResult.setId(articleJPA.getId());
            articleSearchResult.setTitle(articleJPA.getTitle());
            articleSearchResult.setSummary(articleJPA.getSummary());
            result.getArticles().add(articleSearchResult);
        }
        for (CourseJPA courseJPA : foundCourses) {
            if (result.getCourses() == null) result.setCourses(new ArrayList<>());
            SearchResult.CourseSearchResult courseSearchResult = new SearchResult.CourseSearchResult();
            courseSearchResult.setId(courseJPA.getId());
            courseSearchResult.setTitle(courseJPA.getTitle());
            courseSearchResult.setSummary(courseJPA.getSummary());
            result.getCourses().add(courseSearchResult);
        }
        for (LessonWithCourseIdJPA lessonWithCourseIdJPA : foundLessons) {
            long courseId = lessonWithCourseIdJPA.getCourseId();
            LessonJPA lessonJPA = lessonWithCourseIdJPA.getLesson();
            if (result.getLessons() == null) result.setLessons(new ArrayList<>());
            SearchResult.LessonSearchResult lessonSearchResult = getLessonSearchResult(lessonJPA, courseId);
            result.getLessons().add(lessonSearchResult);
        }
        return result;
    }
}
