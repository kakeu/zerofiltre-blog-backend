package tech.zerofiltre.blog.infra.providers.database.course.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import tech.zerofiltre.blog.domain.course.model.Review;
import tech.zerofiltre.blog.infra.providers.database.course.model.ChapterJPA;
import tech.zerofiltre.blog.infra.providers.database.course.model.ReviewJPA;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserJPA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mapper(uses = {CourseJPAMapper.class, ChapterJPAMapper.class})
public interface ReviewJPAMapper {

    @Mapping(target = "chapter", source = "chapterId", qualifiedByName = "ReviewJPAMapper_chapterFromId")
    @Mapping(target = "user", source = "reviewAuthorId", qualifiedByName = "ReviewJPAMapper_userFromId")
    @Mapping(target = "courseId")
    @Mapping(target = "favoriteLearningToolOfTheChapter", qualifiedByName = "ReviewJPAMapper_stringFromListOfString")
    ReviewJPA toJPA(Review review);

    @Mapping(target = "chapterId", source = "chapter.id")
    @Mapping(target = "reviewAuthorId", source = "user.id")
    @Mapping(target = "courseId")
    @Mapping(target = "favoriteLearningToolOfTheChapter", qualifiedByName = "ReviewJPAMapper_stringToListOfString")
    Review fromJPA(ReviewJPA reviewJPA);

    @Named("ReviewJPAMapper_chapterFromId")
    default ChapterJPA chapterFromId(long chapterId) {
        ChapterJPA chapterJPA = new ChapterJPA();
        chapterJPA.setId(chapterId);
        return chapterJPA;
    }

    @Named("ReviewJPAMapper_userFromId")
    default UserJPA userFromId(long userId) {
        UserJPA userJPA = new UserJPA();
        userJPA.setId(userId);
        return userJPA;
    }

    @Named("ReviewJPAMapper_stringFromListOfString")
    default String stringFromListOfString(List<String> stringList) {
        if (stringList == null || stringList.isEmpty())
            return "";

        return String.join(",", stringList);
    }

    @Named("ReviewJPAMapper_stringToListOfString")
    default List<String> stringToListOfString(String stringsSeparatedByComma) {
        if(stringsSeparatedByComma.isEmpty())
            return new ArrayList<>();

        return Arrays.asList(stringsSeparatedByComma.split(",", -1));
    }
}
