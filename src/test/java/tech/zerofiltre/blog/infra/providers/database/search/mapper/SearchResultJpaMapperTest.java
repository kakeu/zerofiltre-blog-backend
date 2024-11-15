package tech.zerofiltre.blog.infra.providers.database.search.mapper;

import org.junit.jupiter.api.Test;
import tech.zerofiltre.blog.domain.search.model.SearchResult;
import tech.zerofiltre.blog.infra.providers.database.article.model.ArticleJPA;
import tech.zerofiltre.blog.infra.providers.database.course.model.CourseJPA;
import tech.zerofiltre.blog.infra.providers.database.course.model.LessonJPA;
import tech.zerofiltre.blog.infra.providers.database.course.model.LessonWithCourseIdJPA;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SearchResultJpaMapperTest {

    private final SearchResultJpaMapper mapper = new SearchResultJpaMapper();

    private static List<LessonWithCourseIdJPA> initFoundLessons() {
        List<LessonWithCourseIdJPA> foundLessons = new ArrayList<>();
        LessonJPA lesson = new LessonJPA();
        lesson.setId(3);
        lesson.setTitle("Lesson Title");
        lesson.setContent("Lesson Content longer than 100 characters to test substring functionality in the method used to extract anotherLesson data");
        LessonWithCourseIdJPA lessonWithCourseIdJPA = new LessonWithCourseIdJPA();
        lessonWithCourseIdJPA.setCourseId(10L);
        lessonWithCourseIdJPA.setLesson(lesson);

        LessonJPA anotherLesson = new LessonJPA();
        anotherLesson.setId(4);
        anotherLesson.setTitle("Another Lesson Title");
        anotherLesson.setContent("Lesson Content shorter than 100 characters");
        LessonWithCourseIdJPA anotherLessonWithCourseIdJPA = new LessonWithCourseIdJPA();
        anotherLessonWithCourseIdJPA.setCourseId(11L);
        anotherLessonWithCourseIdJPA.setLesson(anotherLesson);

        foundLessons.add(lessonWithCourseIdJPA);
        foundLessons.add(anotherLessonWithCourseIdJPA);

        return foundLessons;
    }

    @Test
    void testFromJPAs() {
        // Arrange
        List<ArticleJPA> foundArticles = new ArrayList<>();
        ArticleJPA article = new ArticleJPA();
        article.setId(1);
        article.setTitle("Article Title");
        article.setSummary("Article Summary");
        foundArticles.add(article);

        List<CourseJPA> foundCourses = new ArrayList<>();
        CourseJPA course = new CourseJPA();
        course.setId(2);
        course.setTitle("Course Title");
        course.setSummary("Course Summary");
        foundCourses.add(course);

        List<LessonWithCourseIdJPA> foundLessons = initFoundLessons();

        // Act
        SearchResult result = mapper.fromJPAs(foundArticles, foundCourses, foundLessons);

        // Assert
        assertNotNull(result);

        // Check articles
        assertNotNull(result.getArticles());
        assertEquals(1, result.getArticles().size());
        assertEquals(1, result.getArticles().get(0).getId());
        assertEquals("Article Title", result.getArticles().get(0).getTitle());
        assertEquals("Article Summary", result.getArticles().get(0).getSummary());

        // Check courses
        assertNotNull(result.getCourses());
        assertEquals(1, result.getCourses().size());
        assertEquals(2, result.getCourses().get(0).getId());
        assertEquals("Course Title", result.getCourses().get(0).getTitle());
        assertEquals("Course Summary", result.getCourses().get(0).getSummary());

        // Check lessons
        assertNotNull(result.getLessons());
        assertEquals(2, result.getLessons().size());

        assertEquals(3, result.getLessons().get(0).getId());
        assertEquals(10, result.getLessons().get(0).getCourseId());
        assertEquals("Lesson Title", result.getLessons().get(0).getTitle());
        assertEquals("Lesson Content longer than 100 characters to test substring functionality in the method used to extr...",
                result.getLessons().get(0).getContent());

        assertEquals(4, result.getLessons().get(1).getId());
        assertEquals(11, result.getLessons().get(1).getCourseId());
        assertEquals("Another Lesson Title", result.getLessons().get(1).getTitle());
        assertEquals("Lesson Content shorter than 100 characters",
                result.getLessons().get(1).getContent());


    }

}