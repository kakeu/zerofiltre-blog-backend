package tech.zerofiltre.blog.domain.user.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.ReactionProvider;
import tech.zerofiltre.blog.domain.article.TagProvider;
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.article.model.Reaction;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.logging.LoggerProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.article.DBArticleProvider;
import tech.zerofiltre.blog.infra.providers.database.article.DBReactionProvider;
import tech.zerofiltre.blog.infra.providers.database.article.DBTagProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBVerificationTokenProvider;
import tech.zerofiltre.blog.infra.providers.logging.Slf4jLoggerProvider;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import({DBUserProvider.class, DBArticleProvider.class, DBTagProvider.class, DBReactionProvider.class, DBVerificationTokenProvider.class, DBArticleProvider.class, Slf4jLoggerProvider.class})
class DeleteArticleIT {

    private DeleteArticle deleteArticle;

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private ArticleProvider articleProvider;
    @Autowired
    private ReactionProvider reactionProvider;
    @Autowired
    private TagProvider tagProvider;
    @Autowired
    private LoggerProvider loggerProvider;


    @BeforeEach
    void init() {
        deleteArticle = new DeleteArticle(articleProvider, loggerProvider);
    }

    @Test
    @DisplayName("Deleting an article, deletes it from DB")
    void shouldDeleteProperly() throws ForbiddenActionException, ResourceNotFoundException {
        User currentUser = ZerofiltreUtils.createMockUser(false);
        currentUser = userProvider.save(currentUser);

        List<Tag> tags = ZerofiltreUtils.createMockTags(false);
        tags = tags.stream().map(tagProvider::save).collect(Collectors.toList());

        Article article = ZerofiltreUtils.createMockArticle(currentUser, tags, Collections.emptyList());
        article = articleProvider.save(article);

        List<Reaction> reactions = ZerofiltreUtils.createMockReactions(false, article.getId(),0, currentUser);
        reactions.forEach(reactionProvider::save);

        deleteArticle.execute(currentUser, article.getId());

        Optional<Article> deletedArticle = articleProvider.articleOfId(article.getId());
        assertThat(deletedArticle).isEmpty();
    }
}