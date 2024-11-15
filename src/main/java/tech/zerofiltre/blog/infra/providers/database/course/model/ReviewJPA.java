package tech.zerofiltre.blog.infra.providers.database.course.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import tech.zerofiltre.blog.infra.providers.database.BaseEntityJPA;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserJPA;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "reviews")
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class ReviewJPA extends BaseEntityJPA {

    private String chapterExplanations;
    private int chapterSatisfactionScore;
    private int chapterUnderstandingScore;
    private String recommendCourse;
    private int overallChapterSatisfaction;
    private String chapterImpressions;
    private String whyRecommendingThisCourse;
    private String reasonFavoriteLearningToolOfTheChapter;
    private String improvementSuggestion;
    private long courseId;

    private String favoriteLearningToolOfTheChapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJPA user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private ChapterJPA chapter;

}
