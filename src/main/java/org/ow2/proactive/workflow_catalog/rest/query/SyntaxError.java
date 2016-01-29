/*
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2016 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 * Initial developer(s):               The ProActive Team
 *                         http://proactive.inria.fr/team_members.htm
 */
package org.ow2.proactive.workflow_catalog.rest.query;

import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * @author ActiveEon Team
 */
public class SyntaxError {

    private final Recognizer<?, ?> recognizer;

    private final Object offendingSymbol;

    private final int line;

    private final int charPositionInLine;

    private final String message;

    private final RecognitionException recognitionException;

    public SyntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException recognitionException) {
        this.recognizer = recognizer;
        this.offendingSymbol = offendingSymbol;
        this.line = line;
        this.charPositionInLine = charPositionInLine;
        this.message = msg;
        this.recognitionException = recognitionException;
    }

    public Recognizer<?, ?> getRecognizer() {
        return recognizer;
    }

    public Object getOffendingSymbol() {
        return offendingSymbol;
    }

    public int getLine() {
        return line;
    }

    public int getCharPositionInLine() {
        return charPositionInLine;
    }

    public String getMessage() {
        return message;
    }

    public RecognitionException getException() {
        return recognitionException;
    }

    @Override
    public String toString() {
        String sourceName = recognizer.getInputStream().getSourceName();

        if (!sourceName.isEmpty()) {
            sourceName = sourceName + ": ";
        } else {
            sourceName = "";
        }

        return sourceName + "line " + line + ":" + charPositionInLine + " " + message;
    }

}