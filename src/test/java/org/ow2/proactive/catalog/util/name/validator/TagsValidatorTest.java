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


public class TagsValidatorTest {

    private TagsValidator tagsValidator = new TagsValidator();

    @Test
    public void testCheckNameTagsValid() {
        assertThat(tagsValidator.isValid("tag")).isTrue();
        assertThat(tagsValidator.isValid("tag1,tag2")).isTrue();
        assertThat(tagsValidator.isValid("tag-1,tag.2")).isTrue();
        assertThat(tagsValidator.isValid("tag.1,tag/2")).isTrue();
    }

    @Test
    public void testCheckTagsWithSpecialSymbols() {
        assertThat(tagsValidator.isValid("tag&")).isFalse();
        assertThat(tagsValidator.isValid("tag#")).isFalse();
        assertThat(tagsValidator.isValid("ta*g")).isFalse();
        assertThat(tagsValidator.isValid("ta;g")).isFalse();
    }

    @Test
    public void testCheckTagsStartWithNumber() {
        assertThat(tagsValidator.isValid("1tag")).isFalse();
        assertThat(tagsValidator.isValid("tag,2tag")).isFalse();
    }

    @Test
    public void testCheckTagsEndWithComma() {
        assertThat(tagsValidator.isValid("tag,")).isFalse();
        assertThat(tagsValidator.isValid("tag,tag2,")).isFalse();
    }

    @Test
    public void testCheckTagsWithShortLength() {
        assertThat(tagsValidator.isValid("t")).isFalse();
        assertThat(tagsValidator.isValid("tag,t")).isFalse();
    }

    @Test
    public void testCheckTagsWithWhiteSpace() {
        assertThat(tagsValidator.isValid("t a g")).isTrue();
        assertThat(tagsValidator.isValid("tag,big data")).isTrue();
        assertThat(tagsValidator.isValid("tag, big  data,")).isFalse();
    }


}
