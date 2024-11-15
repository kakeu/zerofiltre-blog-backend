package tech.zerofiltre.blog.domain.user.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.logging.LoggerProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.logging.Slf4jLoggerProvider;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(Slf4jLoggerProvider.class)
class DeleteArticleTest {

    private DeleteArticle deleteArticle;

    @MockBean
    private ArticleProvider articleProvider;

    @Autowired
    private LoggerProvider loggerProvider;


    @BeforeEach
    void init() {
        deleteArticle = new DeleteArticle(articleProvider, loggerProvider);
    }

    @Test
    @DisplayName("A non admin user but owner can delete its own article")
    void deleteFromNonAdminButAuthor_isOK() {

        //ARRANGE

        User currentUser = ZerofiltreUtils.createMockUser(false);
        currentUser.setId(10);
        Article article = ZerofiltreUtils.createMockArticle(currentUser, Collections.emptyList(), Collections.emptyList());
        when(articleProvider.articleOfId(anyLong())).thenReturn(Optional.of(article));


        //ACT & ASSERT
        org.assertj.core.api.Assertions.assertThatNoException().isThrownBy(() -> deleteArticle.execute(currentUser, article.getId()));
    }


    @Test
    @DisplayName("A non admin user, not owner of an article can't delete it")
    void deleteFromNonAdminAndNonAuthor_isKO() {

        User currentUser = ZerofiltreUtils.createMockUser(false);
        currentUser.setId(10);
        Article article = ZerofiltreUtils.createMockArticle(false);
        when(articleProvider.articleOfId(anyLong())).thenReturn(Optional.of(article));


        org.assertj.core.api.Assertions.assertThatExceptionOfType(ForbiddenActionException.class).isThrownBy(() -> deleteArticle.execute(currentUser, article.getId()));
    }


    @Test
    @DisplayName("Deleting an article will delete in the repository")
    void deleteFromNonAdminButAuthor_DeletesArticle() throws ForbiddenActionException, ResourceNotFoundException {

        //ARRANGE

        User currentUser = ZerofiltreUtils.createMockUser(false);
        currentUser.setId(10);
        Article article = ZerofiltreUtils.createMockArticle(currentUser, Collections.emptyList(), Collections.emptyList());
        when(articleProvider.articleOfId(anyLong())).thenReturn(Optional.of(article));

        //ACT
        deleteArticle.execute(currentUser, article.getId());


        //ASSERT
        verify(articleProvider, times(1)).delete(article);
    }
}