package tech.zerofiltre.blog.domain.payment.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.zerofiltre.blog.domain.user.model.User;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    public static final String COMPLETED = "completed";
    public static final String INITIALIZED = "initialized";
    public static final String YEAR = "year";
    public static final String MONTH = "month";

    private User user;
    private String reference;
    private long id;
    private LocalDateTime at = LocalDateTime.now();
    private String status = INITIALIZED;
    private String recurringInterval = MONTH;

}
