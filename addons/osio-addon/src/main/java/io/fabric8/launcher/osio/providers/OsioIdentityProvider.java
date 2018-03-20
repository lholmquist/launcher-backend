package io.fabric8.launcher.osio.providers;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.HttpHeaders;

import io.fabric8.launcher.base.http.ExternalRequest;
import io.fabric8.launcher.base.identity.Identity;
import io.fabric8.launcher.base.identity.IdentityFactory;
import io.fabric8.launcher.core.spi.Application;
import io.fabric8.launcher.core.spi.IdentityProvider;
import io.fabric8.launcher.osio.EnvironmentVariables;
import okhttp3.Request;

import static io.fabric8.launcher.base.identity.IdentityFactory.createFromToken;
import static io.fabric8.launcher.base.identity.IdentityHelper.removeBearerPrefix;
import static io.fabric8.launcher.core.spi.Application.ApplicationType.OSIO;

/**
 * @author <a href="mailto:ggastald@redhat.com">George Gastaldi</a>
 */
@Application(OSIO)
@ApplicationScoped
public class OsioIdentityProvider implements IdentityProvider {

    @Override
    public Optional<Identity> getIdentity(String service, String authorization) {
        switch (service) {
            case ServiceType.GITHUB:
                Request gitHubTokenRequest = new Request.Builder()
                        .url(EnvironmentVariables.ExternalServices.getGithubTokenURL())
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .build();
                return ExternalRequest.readJson(gitHubTokenRequest, tree -> tree.get("access_token").asText())
                        .map(IdentityFactory::createFromToken);
            case ServiceType.OPENSHIFT:
                return Optional.of(createFromToken(removeBearerPrefix(authorization)));
            default:
                Request tokenRequest = new Request.Builder()
                        .url(EnvironmentVariables.ExternalServices.getTokenForURL() + service)
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .build();
                return ExternalRequest.readJson(tokenRequest, tree -> tree.get("access_token").asText())
                        .map(IdentityFactory::createFromToken);
        }
    }
}
