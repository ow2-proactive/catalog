/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.catalog.service.helper;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.ow2.proactive.microservices.common.exception.ServerException;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import lombok.Getter;


public class CommonRestTemplate {

    @Getter
    private RestTemplate restTemplate;

    public CommonRestTemplate() {
        try (CloseableHttpClient httpClient = HttpClients.custom()
                                                         .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null,
                                                                                                                  (certificate,
                                                                                                                          authType) -> true)
                                                                                               .build())
                                                         .setSSLHostnameVerifier(new NoopHostnameVerifier())
                                                         .setConnectionManagerShared(true)
                                                         .build()) {
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
            restTemplate = new RestTemplate(requestFactory);
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException | IOException e) {
            new ServerException("Unable to set rest template for https request", e);
        }
    }

}
