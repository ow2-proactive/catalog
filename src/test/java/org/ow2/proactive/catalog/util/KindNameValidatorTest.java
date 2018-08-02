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
import org.ow2.proactive.catalog.util.name.validator.KindNameValidator;
import org.ow2.proactive.catalog.util.name.validator.NameValidator;


/**
 * @author ActiveEon Team
 * @since 11/13/2017
 */
public class KindNameValidatorTest {

    private NameValidator kindNameValidator = new KindNameValidator();

    @Test
    public void testCheckNameValid() {
        assertThat(kindNameValidator.checkName("valid-kind-1")).isTrue();
        assertThat(kindNameValidator.checkName("kind-name.my")).isTrue();
        assertThat(kindNameValidator.checkName("kind-name.my/n_ew")).isTrue();
    }

    @Test
    public void testCheckNameStartWithUnderscores() {
        assertThat(kindNameValidator.checkName("1-kind_my/m")).isTrue();
        assertThat(kindNameValidator.checkName("_kind_m")).isTrue();
        assertThat(kindNameValidator.checkName("_kind_")).isTrue();
    }

    @Test
    public void testCheckNameWithQuotes() {
        assertThat(kindNameValidator.checkName("kind'm")).isFalse();
        assertThat(kindNameValidator.checkName("kind\"b")).isFalse();
    }

    @Test
    public void testCheckNameStartWithNumber() {
        assertThat(kindNameValidator.checkName("1-kind")).isTrue();
    }

    @Test
    public void testCheckNameOnlyNumbers() {
        assertThat(kindNameValidator.checkName("123")).isTrue();
    }

    @Test
    public void testCheckNameWithCapitals() {
        assertThat(kindNameValidator.checkName("kindWithCapitals")).isTrue();
    }

    @Test
    public void testCheckNameDashInEnd() {
        assertThat(kindNameValidator.checkName("kind-")).isTrue();
    }

    @Test
    public void testCheckNameSmallLength() {
        assertThat(kindNameValidator.checkName("bu")).isTrue();
        assertThat(kindNameValidator.checkName("k")).isTrue();
    }

    @Test
    public void testCheckNameWithDot() {
        assertThat(kindNameValidator.checkName("kind.my")).isTrue();
    }

    @Test
    public void testCheckNameWithSpace() {
        assertThat(kindNameValidator.checkName("kind space")).isFalse();
        assertThat(kindNameValidator.checkName("  kind s")).isFalse();
    }

    @Test
    public void testCheckNameWithSpecialSymbols() {
        assertThat(kindNameValidator.checkName("kind&test#'%new")).isFalse();
    }

    @Test
    public void testCheckNameWithBackSlashSymbols() {
        assertThat(kindNameValidator.checkName("kind/new/my")).isTrue();
        assertThat(kindNameValidator.checkName("kind/new/my/")).isTrue();
    }

    @Test
    public void testCheckNameWithForwardSlashSymbols() {
        assertThat(kindNameValidator.checkName("kind\\new")).isFalse();
        assertThat(kindNameValidator.checkName("kind\\df\\")).isFalse();
    }

    @Test
    public void testCheckNameWithBackSlashSymbolsWrong() {
        assertThat(kindNameValidator.checkName("/kind/new/my")).isFalse();
        assertThat(kindNameValidator.checkName("kind//new/my/")).isFalse();
    }
}
