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
package org.ow2.proactive.catalog.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.ow2.proactive.microservices.common.exception.NotAuthenticatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;


/**
 * @author ActiveEon Team
 * @since 27/07/2017
 */
@Component
public class SchedulerUserAuthenticationService {

    private final SchedulerRestClientCreator schedulerRestClientCreator;

    @Value("${pa.catalog.sessionId.timeout.minutes}")
    private int cacheTimeoutValue;

    // Cache used for future planning calculations
    private Cache<String, UserData> userDataCache = null;

    private Cache<String, UserData> getCache() {
        synchronized (SchedulerUserAuthenticationService.class) {
            if (userDataCache == null) {
                userDataCache = CacheBuilder.newBuilder().expireAfterWrite(cacheTimeoutValue, TimeUnit.MINUTES).build();
            }
            return userDataCache;
        }
    }

    @Autowired
    public SchedulerUserAuthenticationService(SchedulerRestClientCreator schedulerRestClientCreator) {
        this.schedulerRestClientCreator = schedulerRestClientCreator;
    }

    public AuthenticatedUser authenticateBySessionId(String sessionId) throws NotAuthenticatedException {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new NotAuthenticatedException("Could not validate empty sessionId");
        }

        UserData userData = getCache().getIfPresent(sessionId);

        if (userData == null) {
            try {
                userData = this.schedulerRestClientCreator.getNewClientInitializedWithSchedulerRestUrl()
                                                          .getScheduler()
                                                          .getUserDataFromSessionId(sessionId);
            } catch (Exception exception) {
                throw new NotAuthenticatedException("Could not validate sessionId, validation returned: " +
                                                    exception.getMessage(), exception);
            }

            if (userData == null || StringUtils.isEmpty(userData.getUserName())) {
                throw new NotAuthenticatedException("SessionId is invalid");
            }
            getCache().put(sessionId, userData);
        }

        return AuthenticatedUser.builder()
                                .name(userData.getUserName())
                                .groups(new ArrayList<String>(userData.getGroups()))
                                .build();
    }
}
