package tech.zerofiltre.blog.infra.providers.database.article;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class DBArticleProviderTest {

    @Mock
    ArticleJPARepository articleJPARepository;

    @Test
    @DisplayName("Given start date, end date valid and user id 0 - When countPublishedArticlesByDatesAndUser - Then call countPublishedArticlesByDatesAndUser")
    void givenStartDateIsValidAndEndDateIsValidAndUserIdIsZero_whenCountPublishedArticlesByDatesAndUser_ThenCall_countPublishedArticlesByDatesAndUser() {

        Mockito.when(articleJPARepository.countPublishedArticlesByDatesAndUser(any(), any(), anyLong())).thenReturn(1);

        DBArticleProvider dbArticleProvider = new DBArticleProvider(articleJPARepository);
        dbArticleProvider.countPublishedArticlesByDatesAndUser(LocalDate.parse("2024-05-01"), LocalDate.parse("2024-06-01"), 0);

        Mockito.verify(articleJPARepository, Mockito.times(1)).countPublishedArticlesByDatesAndUser(any(), any(), anyLong());

    }

}
