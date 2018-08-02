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
package org.ow2.proactive.catalog.util;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.ow2.proactive.catalog.util.name.validator.BucketNameValidator;


/**
 * @author ActiveEon Team
 * @since 11/13/2017
 */
public class BucketNameValidatorTest {

    private BucketNameValidator bucketNameValidator = new BucketNameValidator();

    @Test
    public void testCheckBucketNameValid() {
        assertThat(bucketNameValidator.checkName("valid-bucket-1")).isTrue();
        assertThat(bucketNameValidator.checkName("bucket-name")).isTrue();
    }

    @Test
    public void testCheckBucketNameStartWithNumber() {
        assertThat(bucketNameValidator.checkName("1-bucket")).isFalse();
    }

    @Test
    public void testCheckBucketNameOnlyNumbers() {
        assertThat(bucketNameValidator.checkName("123")).isFalse();
    }

    @Test
    public void testCheckBucketNameWithCapitals() {
        assertThat(bucketNameValidator.checkName("bucketWithCapitals")).isFalse();
    }

    @Test
    public void testCheckBucketNameDashInEnd() {
        assertThat(bucketNameValidator.checkName("bucket-")).isFalse();
    }

    @Test
    public void testCheckBucketNameSmallLength() {
        assertThat(bucketNameValidator.checkName("bu")).isFalse();
    }

    @Test
    public void testCheckBucketNameWithDot() {
        assertThat(bucketNameValidator.checkName("bucket.my")).isFalse();
    }

    @Test
    public void testCheckBucketNameWithSpace() {
        assertThat(bucketNameValidator.checkName("bucket space")).isFalse();
    }

    @Test
    public void testCheckBucketNameWithSpecialSymbols() {
        assertThat(bucketNameValidator.checkName("bucket&test#'%new")).isFalse();
    }

    @Test
    public void testCheckBucketNameWithBackSlashSymbols() {
        assertThat(bucketNameValidator.checkName("bucket/new/my")).isFalse();
    }
}
