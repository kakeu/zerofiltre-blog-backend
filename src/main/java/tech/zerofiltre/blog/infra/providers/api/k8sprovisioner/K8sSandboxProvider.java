package tech.zerofiltre.blog.infra.providers.api.k8sprovisioner;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.sandbox.SandboxProvider;
import tech.zerofiltre.blog.domain.sandbox.model.Sandbox;
import tech.zerofiltre.blog.domain.sandbox.model.SandboxCreatedEvent;
import tech.zerofiltre.blog.domain.user.UserNotificationProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.model.UserActionEvent;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Locale;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class K8sSandboxProvider implements SandboxProvider {
    private final RestTemplate restTemplate;
    private final InfraProperties infraProperties;
    private final UserProvider userProvider;
    private final UserNotificationProvider notificationProvider;


    @Override
    public Sandbox initialize(String fullName, String email) throws ZerofiltreException {

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", infraProperties.getK8sProvisionerToken());
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");

        Body body = new Body();
        body.setFullName(fullName);
        body.setEmail(email);

        try {
            String bodyAsJson = new ObjectMapper().writeValueAsString(body);
            log.debug("Initializing a k8s sandbox for user {} with request body: \n {}", fullName, bodyAsJson);
            String url = infraProperties.getK8sProvisionerUrl() + "/provisioner";
            HttpEntity<String> requestEntity = new HttpEntity<>(bodyAsJson, headers);
            ResponseEntity<Sandbox> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Sandbox.class);
            Sandbox result = response.getBody();
            notifyUser(email, result);
            log.info("K8s sandbox for user {} initialized: {}", fullName, result);
            return result;
        } catch (Exception e) {
            throw new ZerofiltreException("We couldn't init k8s sandbox for user " + fullName + "/" + email, e);
        }
    }

    private void notifyUser(String email, Sandbox result) {
        if (result != null) {
            //TODO API SHOULD RETURN SANDBOX TYPE IN BODY
            result.setType(Sandbox.Type.K8S);
            Optional<User> user = userProvider.userOfEmail(email);
            //TODO if user if empty try to find him by payment email
            user.ifPresent(foundUser -> {
                String appUrl = ZerofiltreUtils.getOriginUrl(infraProperties.getEnv());
                UserActionEvent event = new SandboxCreatedEvent(appUrl, Locale.forLanguageTag(foundUser.getLanguage()), foundUser, result);
                notificationProvider.notify(event);
            });
        }
    }

    @Override
    public void destroy(String fullName, String email) throws ZerofiltreException {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", infraProperties.getK8sProvisionerToken());
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
        Body body = new Body();
        body.setFullName(fullName);
        body.setEmail(email);

        try {
            String bodyAsJson = new ObjectMapper().writeValueAsString(body);
            log.debug("Body as json: {}", bodyAsJson);

            String url = infraProperties.getK8sProvisionerUrl() + "/provisioner";
            HttpEntity<String> requestEntity = new HttpEntity<>(bodyAsJson, headers);
            ResponseEntity<Sandbox> response = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Sandbox.class);
            Sandbox result = response.getBody();
            log.info("K8s sandbox for user {} destroyed: {}", fullName, result);
        } catch (Exception e) {
            throw new ZerofiltreException("We couldn't delete k8s sandbox for user " + fullName, e);
        }
    }

    @Data
    static class Body {
        @JsonProperty(value = "full_name")
        String fullName;
        String email;
    }
}
