package tech.zerofiltre.blog.domain.user.features;

import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.ReactionProvider;
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.logging.LoggerProvider;
import tech.zerofiltre.blog.domain.logging.model.LogEntry;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.VerificationTokenProvider;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.List;

public class DeleteUser {

    private final UserProvider userProvider;
    private final ArticleProvider articleProvider;
    private final VerificationTokenProvider tokenProvider;
    private final ReactionProvider reactionProvider;
    private final CourseProvider courseProvider;
    private final LoggerProvider loggerProvider;

    public DeleteUser(UserProvider userProvider, ArticleProvider articleProvider, VerificationTokenProvider tokenProvider, ReactionProvider reactionProvider, CourseProvider courseProvider, LoggerProvider loggerProvider) {
        this.userProvider = userProvider;
        this.articleProvider = articleProvider;
        this.tokenProvider = tokenProvider;
        this.reactionProvider = reactionProvider;
        this.courseProvider = courseProvider;
        this.loggerProvider = loggerProvider;
    }

    public void execute(User currentUser, long userIdToDelete) throws ResourceNotFoundException, ForbiddenActionException {

        User foundUser = userProvider.userOfId(userIdToDelete).orElseThrow(() ->
                new ResourceNotFoundException("We could not find the user you want to delete", String.valueOf(userIdToDelete)));

        if (!currentUser.getRoles().contains("ROLE_ADMIN") && currentUser.getId() != foundUser.getId())
            throw new ForbiddenActionException("You can only delete your own account");

        List<Article> userArticles = articleProvider.articlesOf(foundUser);
        List<Course> userCourses = courseProvider.courseOf(foundUser);
        if (userArticles.isEmpty() && userCourses.isEmpty()) {
            tokenProvider.ofUser(foundUser).ifPresent(tokenProvider::delete);
            reactionProvider.ofUser(foundUser).forEach(reactionProvider::delete);
            userProvider.deleteUser(foundUser);
            LogEntry logEntry = new LogEntry(LogEntry.Level.INFO, "Deleting the user " + userIdToDelete + " for done", null, DeleteUser.class);
            loggerProvider.log(logEntry);
        } else {
            foundUser.setExpired(true);
            userProvider.save(foundUser);
            LogEntry logEntry = new LogEntry(LogEntry.Level.INFO, "Deactivating the user as it has articles or owns courses", null, DeleteUser.class);
            loggerProvider.log(logEntry);

        }


    }
}
