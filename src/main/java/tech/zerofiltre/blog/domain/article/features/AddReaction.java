package tech.zerofiltre.blog.domain.article.features;

import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.article.model.Reaction;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class AddReaction {

    private final ArticleProvider articleProvider;
    private final CourseProvider courseProvider;

    public AddReaction(ArticleProvider articleProvider, CourseProvider courseProvider) {
        this.articleProvider = articleProvider;
        this.courseProvider = courseProvider;
    }


    private static void checkReactions(List<Reaction> existingReactions, Reaction reaction, String message) throws ForbiddenActionException {
        long currentUserReactionsCount = existingReactions.stream()
                .filter(aReaction -> aReaction.getAuthorId() == reaction.getAuthorId())
                .count();
        if (currentUserReactionsCount >= 49)
            throw new ForbiddenActionException(message);
    }

    public List<Reaction> execute(Reaction reaction) throws ResourceNotFoundException, ForbiddenActionException {
        List<Reaction> result = new ArrayList<>();
        long articleId = reaction.getArticleId();
        long courseId = reaction.getCourseId();

        if (courseId != 0) {
            Course course = courseProvider.courseOfId(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "We couldn't find the course you are trying to react on",
                            String.valueOf(articleId)
                    ));
            if (course.getStatus().compareTo(Status.PUBLISHED) < 0)
                throw new ForbiddenActionException("You can not react on an unpublished course");

            List<Reaction> reactions = course.getReactions();
            checkReactions(reactions, reaction, "You can not react on a course more that 50 times");

            reactions.add(reaction);
            course = courseProvider.save(course);
            result = course.getReactions();
        }

        if (articleId != 0) {
            Article article = articleProvider.articleOfId(articleId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "We couldn't find the article you are trying to react on",
                            String.valueOf(articleId)
                    ));
            if (article.getStatus().compareTo(Status.PUBLISHED) < 0)
                throw new ForbiddenActionException("You can not react on an unpublished article");

            List<Reaction> reactions = article.getReactions();
            checkReactions(reactions, reaction, "You can not react on an article more that 50 times");

            reactions.add(reaction);
            article = articleProvider.save(article);
            result = article.getReactions();
        }
        return result;
    }
}
