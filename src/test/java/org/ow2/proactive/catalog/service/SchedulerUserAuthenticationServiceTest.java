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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.authentication.UserData;
import org.ow2.proactive.catalog.service.exception.NotAuthenticatedException;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.ow2.proactive_grid_cloud_portal.common.SchedulerRestInterface;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.SchedulerRestClient;


/**
 * @author ActiveEon Team
 * @since 02/08/2017
 */
@RunWith(MockitoJUnitRunner.class)
public class SchedulerUserAuthenticationServiceTest {

    @InjectMocks
    SchedulerUserAuthenticationService schedulerUserAuthenticationService;

    @Mock
    SchedulerRestClientCreator schedulerRestClientCreator;

    @Mock
    SchedulerRestInterface schedulerRestInterfaceMock;

    @Mock
    SchedulerRestClient schedulerRestClientMock;

    @Before
    public void setUpMocks() {
        UserData userData = new UserData();
        userData.setUserName("testUser");
        userData.setGroups(new HashSet<>(Arrays.asList("user", "technical")));
        when(schedulerRestInterfaceMock.getUserDataFromSessionId(any())).thenReturn(userData);
        when(schedulerRestClientMock.getScheduler()).thenReturn(schedulerRestInterfaceMock);
        when(schedulerRestClientCreator.getNewClientInitializedWithSchedulerRestUrl()).thenReturn(schedulerRestClientMock);
    }

    @Test(expected = NotAuthenticatedException.class)
    public void testThatEmptyUsernameThrowsException() throws NotAuthenticatedException {
        when(schedulerRestInterfaceMock.getUserDataFromSessionId(any())).thenReturn(new UserData());
        schedulerUserAuthenticationService.authenticateBySessionId("any");
    }

    @Test(expected = NotAuthenticatedException.class)
    public void testThatNullUserDataThrowsException() throws NotAuthenticatedException {
        when(schedulerRestInterfaceMock.getUserDataFromSessionId(any())).thenReturn(null);
        schedulerUserAuthenticationService.authenticateBySessionId("any");
    }

    @Test(expected = NotAuthenticatedException.class)
    public void testAnyExceptionFromSchedulerRestClientThrowsNotAuthenticatedException()
            throws NotAuthenticatedException {
        when(schedulerRestInterfaceMock.getUserDataFromSessionId(any())).thenThrow(NullPointerException.class);
        schedulerUserAuthenticationService.authenticateBySessionId("any");
    }

    @Test
    public void testThatCorrectUsernameAndGroupAreInsideReturnObject() throws NotAuthenticatedException {
        AuthenticatedUser authenticatedUser = schedulerUserAuthenticationService.authenticateBySessionId("any");
        assertThat(authenticatedUser.getName()).isEqualTo("testUser");
        assertThat(authenticatedUser.getGroups()).containsExactly("user", "technical");
    }

}
