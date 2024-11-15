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
class DBArticleViewProviderTest {

    @Mock
    ArticleViewJPARepository articleViewJPARepository;

    @Test
    @DisplayName("Given start date, end date valid and user id 0 - When countArticlesReadByDatesAndUser - Then call countViewedIdByDatesAndViewerId")
    void givenStartDateIsValidAndEndDateIsValidAndUserIdIsZero_whenCountArticlesReadByDatesAndUser_ThenCall_countViewedIdByDatesAndViewerId() {

        Mockito.when(articleViewJPARepository.countViewedIdByDatesAndViewerId(any(), any(), anyLong())).thenReturn(1);

        DBArticleViewProvider dbArticleViewProvider = new DBArticleViewProvider(articleViewJPARepository);
        dbArticleViewProvider.countArticlesReadByDatesAndUser(LocalDate.parse("2024-05-01"), LocalDate.parse("2024-06-01"), 0);

        Mockito.verify(articleViewJPARepository, Mockito.times(1)).countViewedIdByDatesAndViewerId(any(), any(), anyLong());

    }

}
