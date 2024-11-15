package tech.zerofiltre.blog.domain.user.features;

import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.logging.LoggerProvider;
import tech.zerofiltre.blog.domain.logging.model.LogEntry;
import tech.zerofiltre.blog.domain.user.model.User;

public class DeleteArticle {

    private final ArticleProvider articleProvider;
    private final LoggerProvider loggerProvider;

    public DeleteArticle(ArticleProvider articleProvider, LoggerProvider loggerProvider) {
        this.articleProvider = articleProvider;
        this.loggerProvider = loggerProvider;
    }

    public void execute(User currentUser, long articleIdToDelete) throws ResourceNotFoundException, ForbiddenActionException {


        Article foundArticle = articleProvider.articleOfId(articleIdToDelete).orElseThrow(() ->
                new ResourceNotFoundException("We couldn't find the article you want to delete", String.valueOf(articleIdToDelete)));


        if (!currentUser.getRoles().contains("ROLE_ADMIN") && currentUser.getId() != foundArticle.getAuthor().getId())
            throw new ForbiddenActionException("You can only delete your own article");

        articleProvider.delete(foundArticle);

        LogEntry logEntry = new LogEntry(LogEntry.Level.INFO, "Deleting article " + articleIdToDelete + " for done", null, DeleteArticle.class);
        loggerProvider.log(logEntry);
    }
}
