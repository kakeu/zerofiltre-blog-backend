package tech.zerofiltre.blog.infra.entrypoints.rest.tips;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.tips.TipsService;
import tech.zerofiltre.blog.domain.user.features.UserNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/tips")
public class TipsController {

    private final SecurityContextManager securityContextManager;
    private final TipsService tipsService;

    @GetMapping
    public String getTip(HttpServletRequest request) throws ZerofiltreException {
        User user = null;

        try {
            user = securityContextManager.getAuthenticatedUser();
        } catch (UserNotFoundException ignored) {
            log.info("User not found, getting a general tip.");
        }
        return tipsService.generateTip(user, request.getLocale());
    }

}
