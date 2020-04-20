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
package org.ow2.proactive.catalog.util.name.validator;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;


/**
 * @author ActiveEon Team
 * @since 17/04/2020
 */
public class ObjectNameValidatorTest {

    private ObjectNameValidator objectNameValidator = new ObjectNameValidator();

    @Test
    public void testCheckObjectNameValid() {
        assertThat(objectNameValidator.isValid("valid-name-1")).isTrue();
        assertThat(objectNameValidator.isValid("name-name")).isTrue();
        assertThat(objectNameValidator.isValid("name name")).isTrue();
    }

    @Test
    public void testCheckObjectNameStartWithNumber() {
        assertThat(objectNameValidator.isValid("1-name")).isTrue();
    }

    @Test
    public void testCheckObjectNameOnlyNumbers() {
        assertThat(objectNameValidator.isValid("123")).isTrue();
    }

    @Test
    public void testCheckObjectNameWithCapitals() {
        assertThat(objectNameValidator.isValid("nameWithCapitals")).isTrue();
        assertThat(objectNameValidator.isValid("NAME")).isTrue();
    }

    @Test
    public void testCheckObjectNameDashInEnd() {
        assertThat(objectNameValidator.isValid("name-")).isTrue();
        assertThat(objectNameValidator.isValid("-name")).isTrue();
    }

    @Test
    public void testCheckObjectNameSmallLength() {
        assertThat(objectNameValidator.isValid("bu")).isTrue();
        assertThat(objectNameValidator.isValid("a")).isTrue();
    }

    @Test
    public void testCheckObjectNameWithDot() {
        assertThat(objectNameValidator.isValid("name.my")).isTrue();
    }

    @Test
    public void testCheckObjectEmptyName() {
        assertThat(objectNameValidator.isValid("")).isFalse();
    }

    @Test
    public void testCheckObjectNameWithSpace() {
        assertThat(objectNameValidator.isValid("name space")).isTrue();
        assertThat(objectNameValidator.isValid("  ")).isFalse();
        assertThat(objectNameValidator.isValid("  name space before")).isFalse();
        assertThat(objectNameValidator.isValid("name space after   ")).isFalse();
        assertThat(objectNameValidator.isValid(" beforeANDafter   ")).isFalse();
    }

    @Test
    public void testCheckObjectNameWithSpecialSymbols() {
        assertThat(objectNameValidator.isValid("name&test#'%new")).isTrue();
        assertThat(objectNameValidator.isValid("name${v}")).isTrue();
        assertThat(objectNameValidator.isValid("?.:+=%``")).isTrue();
        assertThat(objectNameValidator.isValid("$*^¨_-°")).isTrue();
        assertThat(objectNameValidator.isValid(")@#&é\\'")).isTrue();
        assertThat(objectNameValidator.isValid("?.:+=%``$*^¨_-°)@#&é\\';")).isTrue();
        assertThat(objectNameValidator.isValid("[$&+:;=?@#|'<>.-^*()%!]\"{}§è!à")).isTrue();
        assertThat(objectNameValidator.isValid("workflow$with&specific&symbols+in name:$&%[$&+:;=?@#|'<>.-^*()%!]\"{}§è!à ae.extension")).isTrue();
    }

    @Test
    public void testCheckObjectNameWithForwardSlashSymbols() {
        assertThat(objectNameValidator.isValid("name/new/my")).isFalse();
        assertThat(objectNameValidator.isValid("name new/my")).isFalse();
        assertThat(objectNameValidator.isValid("name/")).isFalse();
        assertThat(objectNameValidator.isValid("/name")).isFalse();
        assertThat(objectNameValidator.isValid("/")).isFalse();
    }

    @Test
    public void testCheckObjectNameWithCommaSymbols() {
        assertThat(objectNameValidator.isValid("name,new,y")).isFalse();
        assertThat(objectNameValidator.isValid("name,")).isFalse();
        assertThat(objectNameValidator.isValid(",name")).isFalse();
        assertThat(objectNameValidator.isValid(",")).isFalse();
    }
}
