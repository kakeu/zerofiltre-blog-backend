package tech.zerofiltre.blog.domain.purchase.model;

import lombok.Data;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.user.model.User;

import java.time.LocalDateTime;

@Data
public class Purchase {
    private long id;
    private User user;
    private Course course;
    private LocalDateTime at = LocalDateTime.now();

    public Purchase() {
    }

    public Purchase(User user, Course course) {
        this.user = user;
        this.course = course;
    }
}
