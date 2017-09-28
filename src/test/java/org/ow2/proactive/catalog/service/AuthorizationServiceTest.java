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
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;


/**
 * @author ActiveEon Team
 * @since 31/07/2017
 */
@RunWith(value = MockitoJUnitRunner.class)
public class AuthorizationServiceTest {

    @Spy
    @InjectMocks
    private AuthorizationService authorizationService;

    @Mock
    private OwnerGroupStringHelper ownerGroupStringHelper;

    @Test
    public void testUserNameIsNotMatchedAgainstGroup() {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                                                               .name("C3PO")
                                                               .groups(Arrays.asList("secret stuff", "robots"))
                                                               .build();

        assertThat(authorizationService.askUserAuthorizationByBucketOwner(authenticatedUser, "C3PO")).isFalse();
    }

    @Test
    public void testThatGroupPrefixIsCorrectlyRemoved() {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                                                               .name("C3PO")
                                                               .groups(Arrays.asList("secret stuff", "robots"))
                                                               .build();

        when(ownerGroupStringHelper.extractGroupFromBucketOwnerOrGroupString(OwnerGroupStringHelper.GROUP_PREFIX +
                                                                             "secret stuff")).thenReturn("secret stuff");

        assertThat(authorizationService.askUserAuthorizationByBucketOwner(authenticatedUser,
                                                                          OwnerGroupStringHelper.GROUP_PREFIX +
                                                                                             "secret stuff")).isTrue();
    }

    @Test
    public void testFalseIsReturnedIfNoMatchWithUserNameNorGroup() {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                                                               .name("C3PO")
                                                               .groups(Arrays.asList("secret stuff", "robots"))
                                                               .build();

        when(ownerGroupStringHelper.extractGroupFromBucketOwnerOrGroupString("not exist")).thenReturn("not exist");

        assertThat(authorizationService.askUserAuthorizationByBucketOwner(authenticatedUser, "not exist")).isFalse();
    }

    @Test
    public void testAgainstOwnerNullReturnsTrueBecauseNoSpecificAccessRequired() {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                                                               .name("C3PO")
                                                               .groups(Arrays.asList("secret stuff", "robots"))
                                                               .build();

        when(ownerGroupStringHelper.extractGroupFromBucketOwnerOrGroupString(null)).thenReturn(null);

        assertThat(authorizationService.askUserAuthorizationByBucketOwner(authenticatedUser, null)).isTrue();
    }

    @Test
    public void testAuthenticatedUserNullReturnsFalse() {
        assertThat(authorizationService.askUserAuthorizationByBucketOwner(null, "")).isFalse();
    }

    @Test
    public void testAuthenticatedUserAndOWnerNullReturnsFalse() {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                                                               .name("C3PO")
                                                               .groups(Arrays.asList("secret stuff", "robots"))
                                                               .build();

        when(ownerGroupStringHelper.extractGroupFromBucketOwnerOrGroupString(null)).thenReturn(null);

        assertThat(authorizationService.askUserAuthorizationByBucketOwner(null, null)).isFalse();
    }

    @Test
    public void testDefaultGroupReturnsTrue() {
        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                                                               .name("C3PO")
                                                               .groups(Arrays.asList("secret stuff", "robots"))
                                                               .build();

        when(ownerGroupStringHelper.extractGroupFromBucketOwnerOrGroupString(BucketService.DEFAULT_BUCKET_OWNER)).thenReturn("public-objects");

        assertThat(authorizationService.askUserAuthorizationByBucketOwner(authenticatedUser,
                                                                          BucketService.DEFAULT_BUCKET_OWNER)).isTrue();
    }

    @Test
    public void testPublicAccessOnlyAllowingPublicObjects() {

        assertThat(authorizationService.isPublicAccess(BucketService.DEFAULT_BUCKET_OWNER)).isTrue();
        assertThat(authorizationService.isPublicAccess("GROUP:public-objects")).isTrue();

        assertThat(authorizationService.isPublicAccess("public-objects")).isFalse();
        assertThat(authorizationService.isPublicAccess("GROUP:")).isFalse();
    }

}
