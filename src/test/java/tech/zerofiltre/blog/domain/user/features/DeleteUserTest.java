package tech.zerofiltre.blog.domain.user.features;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.ReactionProvider;
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.article.model.Reaction;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.logging.LoggerProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.VerificationTokenProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.model.VerificationToken;
import tech.zerofiltre.blog.doubles.Found_Published_With49Reactions_CourseProvider_Spy;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class DeleteUserTest {

    @MockBean
    LoggerProvider loggerProvider;
    LocalDateTime expiryDate = LocalDateTime.now().plusDays(1);
    User currentUser = new User();
    User foundUser = new User();
    @MockBean
    private UserProvider userProvider;
    @MockBean
    private VerificationTokenProvider tokenProvider;
    @MockBean
    private ReactionProvider reactionProvider;
    @MockBean
    private ArticleProvider articleProvider;

    private final CourseProvider courseProvider = new Found_Published_With49Reactions_CourseProvider_Spy();
    private DeleteUser deleteUser;

    @BeforeEach
    void setUp() {
        deleteUser = new DeleteUser(userProvider, articleProvider, tokenProvider, reactionProvider, courseProvider, loggerProvider);
        doNothing().when(loggerProvider).log(any());
    }

    @Test
    void deleteUser_MustDeleteTheUserReactionsAndToken() {
        //ARRANGE
        Reaction reaction = new Reaction();
        VerificationToken token = new VerificationToken(foundUser, "token", expiryDate);

        currentUser.setRoles(Collections.singleton("ROLE_ADMIN"));
        foundUser.setId(10);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(foundUser));
        when(reactionProvider.ofUser(any())).thenReturn(Collections.singletonList(reaction));
        when(tokenProvider.ofUser(any())).thenReturn(Optional.of(token));

        doNothing().when(userProvider).deleteUser(any());
        doNothing().when(reactionProvider).delete(any());
        doNothing().when(tokenProvider).delete(any());


        //ACT & ASSERT
        Assertions.assertThatNoException().isThrownBy(() -> deleteUser.execute(currentUser, 10));

        verify(tokenProvider, times(1)).ofUser(foundUser);
        verify(tokenProvider, times(1)).delete(token);
        verify(reactionProvider, times(1)).ofUser(foundUser);
        verify(reactionProvider, times(1)).delete(reaction);
        verify(userProvider, times(1)).deleteUser(foundUser);


    }

    @Test
    void deleteUser_MustThrowExceptionOnResourceNotFound() {
        //ARRANGE
        currentUser.setRoles(Collections.singleton("ROLE_ADMIN"));
        foundUser.setId(10);
        doNothing().when(userProvider).deleteUser(any());
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.empty());

        //ACT & ASSERT
        Assertions.assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> deleteUser.execute(currentUser, 10));
        verify(userProvider, times(1)).userOfId(10);
        verify(userProvider, times(0)).deleteUser(foundUser);

    }

    @Test
    @DisplayName("DeleteUser must throw ForbiddenActionException if the connected user is neither" +
            " the one to be deleted nor an administrator")
    void deleteUser_MustThrowForbiddenActionException_OnCurrentUserNotAdmin_andNotTheDeletedUser() {
        //ARRANGE
        currentUser.setRoles(Collections.singleton("ROLE_USER"));
        foundUser.setId(9);
        doNothing().when(userProvider).deleteUser(any());
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(foundUser));

        //ACT & ASSERT
        Assertions.assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> deleteUser.execute(currentUser, 10));
        verify(userProvider, times(1)).userOfId(10);
        verify(userProvider, times(0)).deleteUser(foundUser);

    }

    @Test
    @DisplayName("Deleting a user that has articles deactivates the user and deletes his sensible information")
    void deleteUser_WithArticles_deactivatesAndDeletesInfo() throws ForbiddenActionException, ResourceNotFoundException {
        //ARRANGE
        currentUser.setId(10);
        foundUser.setId(10);
        Article draftArticle = new Article();
        Article publishedArticle = new Article();
        List<Article> userArticles = Arrays.asList(draftArticle, publishedArticle);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(foundUser));
        when(articleProvider.articlesOf(foundUser)).thenReturn(userArticles);

        //ACT
        deleteUser.execute(currentUser, 10);

        verify(articleProvider, times(1)).articlesOf(foundUser);
        verify(userProvider, times(0)).deleteUser(foundUser);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userProvider, times(1)).save(captor.capture());
        User updatedUser = captor.getValue();
        assertThat(updatedUser.isExpired()).isTrue();


    }
}