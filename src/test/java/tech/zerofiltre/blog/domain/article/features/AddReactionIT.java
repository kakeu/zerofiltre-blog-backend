package tech.zerofiltre.blog.domain.article.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.ReactionProvider;
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.article.model.Reaction;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.article.DBArticleProvider;
import tech.zerofiltre.blog.infra.providers.database.article.DBReactionProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBCourseProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static tech.zerofiltre.blog.domain.article.model.Reaction.Action.CLAP;
import static tech.zerofiltre.blog.domain.article.model.Reaction.Action.FIRE;

@DataJpaTest
@Import({DBArticleProvider.class, DBUserProvider.class, DBReactionProvider.class, DBCourseProvider.class})
class AddReactionIT {

    @Autowired
    private ArticleProvider articleProvider;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private ReactionProvider reactionProvider;

    @Autowired
    CourseProvider courseProvider;

    AddReaction addReaction;

    @BeforeEach
    void init() {
        addReaction = new AddReaction(articleProvider, courseProvider);
    }

    @Test
    void execute_mustSaveReactionsProperly() throws ForbiddenActionException, ResourceNotFoundException {
        //ARRANGE
        User currentUser = ZerofiltreUtils.createMockUser(false);
        currentUser = userProvider.save(currentUser);
        long currentUserId = currentUser.getId();
        Article article = ZerofiltreUtils.createMockArticle(currentUser, new ArrayList<>(), new ArrayList<>());
        article.setStatus(Status.PUBLISHED);
        article = articleProvider.save(article);

        Reaction reaction = new Reaction();
        reaction.setAuthorId(currentUser.getId());
        long articleId = article.getId();
        reaction.setArticleId(articleId);
        reaction.setAction(CLAP);

        //ACT
        List<Reaction> reactions = addReaction.execute(reaction);

        //ASSERT
        assertThat(reactions).isNotNull();
        assertThat(reactions.size()).isNotZero();
        reactions.forEach(aReaction -> {
            assertThat(aReaction.getAction()).isEqualTo(CLAP);
            assertThat(aReaction.getArticleId()).isEqualTo(articleId);
            assertThat(aReaction.getAuthorId()).isEqualTo(currentUserId);
        });

    }

    @Test
    void execute_mustSaveReactionsOnCourseProperly() throws ForbiddenActionException, ResourceNotFoundException {
        //ARRANGE
        User currentUser = ZerofiltreUtils.createMockUser(false);
        currentUser = userProvider.save(currentUser);
        long currentUserId = currentUser.getId();

        Course course = ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, currentUser, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);
        long courseId = course.getId();

        Reaction reaction = new Reaction();
        reaction.setAuthorId(currentUser.getId());
        reaction.setCourseId(courseId);
        reaction.setAction(CLAP);

        //ACT
        List<Reaction> reactions = addReaction.execute(reaction);

        //ASSERT
        assertThat(reactions).isNotNull();
        assertThat(reactions.size()).isNotZero();
        reactions.forEach(aReaction -> {
            assertThat(aReaction.getAction()).isEqualTo(CLAP);
            assertThat(aReaction.getCourseId()).isEqualTo(courseId);
            assertThat(aReaction.getAuthorId()).isEqualTo(currentUserId);
        });

    }

    @Test
    void execute_returnsAllArticleReactions() throws ForbiddenActionException, ResourceNotFoundException {
        //ARRANGE
        User currentUser = ZerofiltreUtils.createMockUser(false);
        currentUser = userProvider.save(currentUser);
        long currentUserId = currentUser.getId();


        Article article = ZerofiltreUtils.createMockArticle(currentUser, new ArrayList<>(), new ArrayList<>());
        article = articleProvider.save(article);
        article.setStatus(Status.PUBLISHED);
        long articleId = article.getId();

        Reaction previousReaction = new Reaction();
        previousReaction.setAuthorId(currentUserId);
        previousReaction.setAction(Reaction.Action.FIRE);
        previousReaction.setArticleId(articleId);
        reactionProvider.save(previousReaction);

        article.getReactions().add(previousReaction);
        articleProvider.save(article);

        Reaction reaction = new Reaction();
        reaction.setAuthorId(currentUser.getId());
        reaction.setArticleId(articleId);
        reaction.setAction(CLAP);

        //ACT
        List<Reaction> reactions = addReaction.execute(reaction);

        //ASSERT
        assertThat(reactions).isNotNull();
        assertThat(reactions.size()).isEqualTo(2);


        assertThat(reactions.stream().allMatch(aReaction ->
                aReaction.getAction().equals(CLAP) || aReaction.getAction().equals(FIRE)
        )).isTrue();

        assertThat(reactions.stream().allMatch(aReaction ->
                aReaction.getArticleId() == articleId &&
                        aReaction.getAuthorId() == currentUserId
        )).isTrue();

    }

    @Test
    void execute_returnsAllCourseReactions() throws ForbiddenActionException, ResourceNotFoundException {
        //ARRANGE
        User currentUser = ZerofiltreUtils.createMockUser(false);
        currentUser = userProvider.save(currentUser);
        long currentUserId = currentUser.getId();


        Course course = ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, currentUser, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);
        long courseId = course.getId();

        Reaction previousReaction = new Reaction();
        previousReaction.setAuthorId(currentUserId);
        previousReaction.setAction(Reaction.Action.FIRE);
        previousReaction.setCourseId(courseId);
        reactionProvider.save(previousReaction);

        course.getReactions().add(previousReaction);
        courseProvider.save(course);

        Reaction reaction = new Reaction();
        reaction.setAuthorId(currentUser.getId());
        reaction.setCourseId(courseId);
        reaction.setAction(CLAP);

        //ACT
        List<Reaction> reactions = addReaction.execute(reaction);

        //ASSERT
        assertThat(reactions).isNotNull();
        assertThat(reactions.size()).isEqualTo(2);


        assertThat(reactions.stream().allMatch(aReaction ->
                aReaction.getAction().equals(CLAP) || aReaction.getAction().equals(FIRE)
        )).isTrue();

        assertThat(reactions.stream().allMatch(aReaction ->
                aReaction.getCourseId() == courseId &&
                        aReaction.getAuthorId() == currentUserId
        )).isTrue();

    }
}
