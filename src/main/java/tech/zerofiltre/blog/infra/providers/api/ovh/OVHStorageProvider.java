package tech.zerofiltre.blog.infra.providers.api.ovh;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.storage.StorageProvider;
import tech.zerofiltre.blog.infra.InfraProperties;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OVHStorageProvider implements StorageProvider {

    private final OVHTokenProvider tokenProvider;
    private final RestTemplate restTemplate;
    private final InfraProperties infraProperties;
    private final RetryTemplate retryTemplate;


    @Override
    public void store(byte[] data, String path) throws ZerofiltreException {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("X-Auth-Token", tokenProvider.getToken().getAccessToken());
        retryTemplate.execute(retryContext -> {
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(data, headers);
            restTemplate.exchange(infraProperties.getOvhBucketUrl() + "/" + path, HttpMethod.PUT, requestEntity, Void.class);
            return null;
        });

    }

    @Override
    public Optional<byte[]> get(String path) {
        return retryTemplate.execute(retryContext -> {
            try {
                HttpEntity<Object> requestEntity = new HttpEntity<>(null, new LinkedMultiValueMap<>());
                ResponseEntity<byte[]> response = restTemplate.exchange(infraProperties.getOvhBucketUrl() + "/" + path, HttpMethod.GET, requestEntity, byte[].class);
                return Optional.ofNullable(response.getBody());
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) return Optional.empty();
                throw e;
            }
        });
    }

    @Override
    public void delete(String path) throws ZerofiltreException {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("X-Auth-Token", tokenProvider.getToken().getAccessToken());

        retryTemplate.execute(retryContext -> {
            HttpEntity<Object> requestEntity = new HttpEntity<>(null, headers);
            restTemplate.exchange(infraProperties.getOvhBucketUrl() + "/" + path, HttpMethod.DELETE, requestEntity, String.class);
            return null;
        });

    }
}
