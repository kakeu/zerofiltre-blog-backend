package tech.zerofiltre.blog.infra.providers.notification.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.payment.PaymentException;
import tech.zerofiltre.blog.domain.payment.model.ChargeRequest;
import tech.zerofiltre.blog.domain.payment.model.Payment;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.api.notchpay.NotchPayProvider;
import tech.zerofiltre.blog.infra.providers.notification.user.model.Email;
import tech.zerofiltre.blog.infra.security.config.EmailValidator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static tech.zerofiltre.blog.domain.payment.model.Payment.*;
import static tech.zerofiltre.blog.util.ZerofiltreUtils.sleep;

@Slf4j
@Component
@RequiredArgsConstructor
public class MobilePaymentReminder {

    private final UserProvider userProvider;
    private final ZerofiltreEmailSender emailSender;
    private final NotchPayProvider notchPayProvider;


    @Scheduled(cron = "${zerofiltre.infra.mobile.payments.reminder.cron}")
    public void sendStats() throws PaymentException {

        List<Payment> payments = notchPayProvider.payments();

        for (Payment payment : payments) {
            User user = payment.getUser();
            if (!user.isPro()) continue;

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime monthlyRenewDate = payment.getAt().plusMonths(1);
            LocalDateTime yearlyRenewDate = payment.getAt().plusYears(1);

            if (MONTH.equals(payment.getRecurringInterval())) {
                Duration between = Duration.between(now, monthlyRenewDate);
                long daysBeforeRenewal = between.toDays();
                notify(payment, daysBeforeRenewal);

            }
            if (YEAR.equals(payment.getRecurringInterval()) && now.isAfter(yearlyRenewDate)) {
                Duration between = Duration.between(now, monthlyRenewDate);
                long daysBeforeRenewal = between.toDays();
                notify(payment, daysBeforeRenewal);
            }
            sleep(10_000);
        }
    }

    private void notify(Payment payment, long daysBeforeRenewal) throws PaymentException {
        if (daysBeforeRenewal >= 5) return;
        User user = payment.getUser();
        ChargeRequest chargeRequest = new ChargeRequest();
        chargeRequest.setPaymentEmail(user.getPaymentEmail());
        chargeRequest.setCurrency(ChargeRequest.Currency.XAF);
        chargeRequest.setRecurringInterval(payment.getRecurringInterval());
        String paymentLink = notchPayProvider.createSession(user, null, chargeRequest);
        if (daysBeforeRenewal > 0) {
            notchPayProvider.delete(INITIALIZED, user.getId());
            String message = "Votre abonnement prendra fin dans " + daysBeforeRenewal + " jour(s). " +
                    "Afin de continuer à bénéficier de nos parcours et articles premium, cliquez sur le lien suivant: "
                    + paymentLink;
            String subject = "Votre abonnement prend fin dans " + daysBeforeRenewal + " jour(s). ";
            notifyUser(user, subject, message);
        } else if (user.isPro()) {
            String message = "Votre abonnement a pris fin ce jour. " +
                    "Afin de continuer à bénéficier de nos parcours et articles premium, cliquez sur le lien suivant: "
                    + paymentLink;
            String subject = "[Urgent] Votre accès a été suspendu ";
            notifyUser(user, subject, message);
            user.setPlan(User.Plan.BASIC);
            userProvider.save(user);
        }


    }

    void notifyUser(User user, String subject, String message) {
        boolean validEmail = user.getEmail() != null && EmailValidator.validateEmail(user.getEmail());
        String emailAddress = validEmail ? user.getEmail() : user.getPaymentEmail();
        if (emailAddress != null) {
            Email email = new Email();
            email.setRecipients(Collections.singletonList(emailAddress));
            email.setSubject(subject);
            email.setReplyTo("info@zerofiltre.tech");
            email.setContent(message);
            emailSender.send(email, false);
        }
    }
}