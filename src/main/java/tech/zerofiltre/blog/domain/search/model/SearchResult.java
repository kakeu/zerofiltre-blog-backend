package tech.zerofiltre.blog.domain.search.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult implements Serializable {

    private List<CourseSearchResult> courses;
    private List<ArticleSearchResult> articles;
    private List<LessonSearchResult> lessons;


    @Getter
    @Setter
    public static class CourseSearchResult implements Serializable {
        long id;
        String title;
        String subTitle;
        String summary;
    }

    @Getter
    @Setter
    public static class ArticleSearchResult implements Serializable {
        long id;
        String title;
        String summary;
    }

    @Getter
    @Setter
    public static class LessonSearchResult implements Serializable {
        long id;
        String title;
        String content;
        long courseId;
    }
}
