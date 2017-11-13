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


/**
 * @author ActiveEon Team
 * @since 11/13/2017
 */
public class BucketNameValidatorTest {

    //    protected final static String VALID_BUCKET_NAME_PATTERN = "[a-z][a-z0-9-]{1,61}[a-z0-9]";
    //
    //    private final static Pattern validBucketNamePattern = Pattern.compile(VALID_BUCKET_NAME_PATTERN);
    //
    //    /**
    //     * According to this check: the bucket name can be between 3 and 63 characters long, and can contain only lower-case characters, numbers, and dashes.
    //     A bucket name must start with a lowercase letter and cannot terminate with a dash.
    //     *
    //     * @param bucketNameForCheck
    //     * @return true result if bucket name is valid
    //     */
    //    public static boolean checkBucketName(String bucketNameForCheck) {
    //        Matcher matcher = validBucketNamePattern.matcher(bucketNameForCheck);
    //        return matcher.matches();
    //    }

    private BucketNameValidator bucketNameValidator = new BucketNameValidator();

    @Test
    public void testCheckBucketNameValid() {
        assertThat(bucketNameValidator.checkBucketName("valid-bucket-1")).isTrue();
        assertThat(bucketNameValidator.checkBucketName("bucket-name")).isTrue();
    }

    @Test
    public void testCheckBucketNameStartWithNumber() {
        assertThat(bucketNameValidator.checkBucketName("1-bucket")).isFalse();
    }

    @Test
    public void testCheckBucketNameOnlyNumbers() {
        assertThat(bucketNameValidator.checkBucketName("123")).isFalse();
    }

    @Test
    public void testCheckBucketNameWithCapitals() {
        assertThat(bucketNameValidator.checkBucketName("bucketWithCapitals")).isFalse();
    }

    @Test
    public void testCheckBucketNameDashInEnd() {
        assertThat(bucketNameValidator.checkBucketName("bucket-")).isFalse();
    }

    @Test
    public void testCheckBucketNameSmallLength() {
        assertThat(bucketNameValidator.checkBucketName("bu")).isFalse();
    }

    @Test
    public void testCheckBucketNameWithDot() {
        assertThat(bucketNameValidator.checkBucketName("bucket.my")).isFalse();
    }

    @Test
    public void testCheckBucketNameWithSpace() {
        assertThat(bucketNameValidator.checkBucketName("bucket space")).isFalse();
    }

    @Test
    public void testCheckBucketNameWithSpecialSymbols() {
        assertThat(bucketNameValidator.checkBucketName("bucket&test#'%new")).isFalse();
    }

    @Test
    public void testCheckBucketNameWithBackSlashSymbols() {
        assertThat(bucketNameValidator.checkBucketName("bucket/new/my")).isFalse();
    }
}
