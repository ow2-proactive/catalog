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
import org.ow2.proactive.catalog.util.name.validator.KindAndContentTypeValidator;
import org.ow2.proactive.catalog.util.name.validator.NameValidator;


/**
 * @author ActiveEon Team
 * @since 11/13/2017
 */
public class KindAndContentTypeValidatorTest {

    private NameValidator kindAndContentTypeNameValidator = new KindAndContentTypeValidator();

    @Test
    public void testCheckNameValidContentType() {
        assertThat(kindAndContentTypeNameValidator.isValid("application/json;charset=UTF-8")).isTrue();
        assertThat(kindAndContentTypeNameValidator.isValid("application/vnd.openxmlformats-officedocument.presentationml.presentation")).isTrue();
        assertThat(kindAndContentTypeNameValidator.isValid("application/vnd.ms-excel.template.macroEnabled.12")).isTrue();
        assertThat(kindAndContentTypeNameValidator.isValid("application/atom+xml")).isTrue();
    }

    @Test
    public void testCheckNameValid() {
        assertThat(kindAndContentTypeNameValidator.isValid("valid-kind-1")).isTrue();
        assertThat(kindAndContentTypeNameValidator.isValid("kind-name.my")).isTrue();
        assertThat(kindAndContentTypeNameValidator.isValid("kind-name.my/n_ew/my test")).isTrue();
        assertThat(kindAndContentTypeNameValidator.isValid("1-kind_my/mine")).isTrue();
    }

    @Test
    public void testCheckNameStartWithUnderscores() {
        assertThat(kindAndContentTypeNameValidator.isValid("_kind_m")).isFalse();
        assertThat(kindAndContentTypeNameValidator.isValid("_kind_")).isFalse();
    }

    @Test
    public void testCheckNameWithQuotes() {
        assertThat(kindAndContentTypeNameValidator.isValid("kind'm")).isFalse();
        assertThat(kindAndContentTypeNameValidator.isValid("kind\"b")).isFalse();
    }

    @Test
    public void testCheckNameStartWithNumber() {
        assertThat(kindAndContentTypeNameValidator.isValid("1-kind")).isTrue();
    }

    @Test
    public void testCheckNameOnlyNumbers() {
        assertThat(kindAndContentTypeNameValidator.isValid("123")).isTrue();
    }

    @Test
    public void testCheckNameWithCapitals() {
        assertThat(kindAndContentTypeNameValidator.isValid("kindWithCapitals")).isTrue();
    }

    @Test
    public void testCheckNameDashInEnd() {
        assertThat(kindAndContentTypeNameValidator.isValid("kind-")).isFalse();
        assertThat(kindAndContentTypeNameValidator.isValid("-kind")).isFalse();
    }

    @Test
    public void testCheckNameSmallLength() {
        assertThat(kindAndContentTypeNameValidator.isValid("bu")).isFalse();
        assertThat(kindAndContentTypeNameValidator.isValid("k")).isFalse();
    }

    @Test
    public void testCheckNameWithDot() {
        assertThat(kindAndContentTypeNameValidator.isValid("kind.my")).isTrue();
    }

    @Test
    public void testCheckNameWithSpace() {
        assertThat(kindAndContentTypeNameValidator.isValid("kind space/")).isTrue();
        assertThat(kindAndContentTypeNameValidator.isValid("kind s")).isTrue();
        assertThat(kindAndContentTypeNameValidator.isValid("   kind")).isFalse();
        assertThat(kindAndContentTypeNameValidator.isValid("kind   ")).isFalse();
    }

    @Test
    public void testCheckNameWithSpecialSymbols() {
        assertThat(kindAndContentTypeNameValidator.isValid("kind&test#'%new")).isFalse();
    }

    @Test
    public void testCheckNameWithBackSlashSymbols() {
        assertThat(kindAndContentTypeNameValidator.isValid("kind/new/my-k")).isTrue();
        assertThat(kindAndContentTypeNameValidator.isValid("kind/new/my-k/")).isTrue();
    }

    @Test
    public void testCheckNameWithForwardSlashSymbols() {
        assertThat(kindAndContentTypeNameValidator.isValid("kind\\new")).isFalse();
        assertThat(kindAndContentTypeNameValidator.isValid("kind\\df\\")).isFalse();
    }

    @Test
    public void testCheckNameWithBackSlashSymbolsWrong() {
        assertThat(kindAndContentTypeNameValidator.isValid("/kind/new/my")).isFalse();
        assertThat(kindAndContentTypeNameValidator.isValid("kind//new/my/")).isFalse();
    }
}
