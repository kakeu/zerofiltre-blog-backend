package tech.zerofiltre.blog.domain.article.features;

import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.user.model.User;

import java.time.LocalDateTime;

public class InitArticle {

    private final ArticleProvider articleProvider;


    public InitArticle(ArticleProvider articleProvider) {
        this.articleProvider = articleProvider;

    }


    public Article execute(String title, User user) {
        LocalDateTime now = LocalDateTime.now();
        Article article = new Article();
        article.setTitle(title);
        article.setCreatedAt(now);
        article.setLastSavedAt(now);
        article.setStatus(Status.DRAFT);
        article.setAuthor(user);
        return articleProvider.save(article);
    }

}
