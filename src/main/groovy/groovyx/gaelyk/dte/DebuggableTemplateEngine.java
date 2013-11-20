/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.gaelyk.dte;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport;
import org.codehaus.groovy.runtime.IOGroovyMethods;

/**
 * {@link DebuggableTemplateEngine} is variation on {@link SimpleTemplateEngine}
 * with better error reporting.
 * 
 * Processes template source files substituting variables and expressions into
 * placeholders in a template source text to produce the desired output.
 * <p>
 * The template engine uses JSP style &lt;% %&gt; script and &lt;%= %&gt; expression syntax
 * or GString style expressions. The variable '<code>out</code>' is bound to the writer that the template
 * is being written to.
 * <p>
 * Frequently, the template source will be in a file but here is a simple
 * example providing the template as a string:
 * <pre>
 * def binding = [
 *     firstname : "Grace",
 *     lastname  : "Hopper",
 *     accepted  : true,
 *     title     : 'Groovy for COBOL programmers'
 * ]
 * def engine = new groovy.text.SimpleTemplateEngine()
 * def text = '''\
 * Dear &lt;%= firstname %&gt; $lastname,
 *
 * We &lt;% if (accepted) print 'are pleased' else print 'regret' %&gt; \
 * to inform you that your paper entitled
 * '$title' was ${ accepted ? 'accepted' : 'rejected' }.
 *
 * The conference committee.
 * '''
 * def template = engine.createTemplate(text).make(binding)
 * println template.toString()
 * </pre>
 * This example uses a mix of the JSP style and GString style placeholders
 * but you can typically use just one style if you wish. Running this
 * example will produce this output:
 * <pre>
 * Dear Grace Hopper,
 *
 * We are pleased to inform you that your paper entitled
 * 'Groovy for COBOL programmers' was accepted.
 *
 * The conference committee.
 * </pre>
 * The template engine can also be used as the engine for {@link groovy.servlet.TemplateServlet} by placing the
 * following in your <code>web.xml</code> file (plus a corresponding servlet-mapping element):
 * <pre>
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;SimpleTemplate&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;groovy.servlet.TemplateServlet&lt;/servlet-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;template.engine&lt;/param-name&gt;
 *     &lt;param-value&gt;groovy.text.SimpleTemplateEngine&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 * &lt;/servlet&gt;
 * </pre>
 * In this case, your template source file should be HTML with the appropriate embedded placeholders.
 *
 * @author sam
 * @author Christian Stein
 * @author Paul King
 * @author Alex Tkachman
 * @author Vladimir Orany
 */
public class DebuggableTemplateEngine extends TemplateEngine {
    private boolean verbose;
    private static int counter = 1;

    private GroovyShell groovyShell;

    public DebuggableTemplateEngine() {
        this(GroovyShell.class.getClassLoader());
    }

    public DebuggableTemplateEngine(boolean verbose) {
        this(GroovyShell.class.getClassLoader());
        setVerbose(verbose);
    }

    public DebuggableTemplateEngine(ClassLoader parentLoader) {
        this(new GroovyShell(parentLoader));
    }

    public DebuggableTemplateEngine(GroovyShell groovyShell) {
        this.groovyShell = groovyShell;
    }

    @Override public Template createTemplate(File file) throws CompilationFailedException, ClassNotFoundException, IOException {
        Reader reader = new FileReader(file);
        try {
            return createTemplate(reader, file.getName());
        } finally {
            DefaultGroovyMethodsSupport.closeWithWarning(reader);
        }
    }
    
    public DebuggableTemplate createTemplate(Reader reader) throws CompilationFailedException, IOException {
        return createTemplate(reader, "DebuggableTemplateScript" + counter++ + ".groovy");
    }
    
    public DebuggableTemplate createTemplate(Reader reader, String fileName) throws CompilationFailedException, IOException {
        DebuggableTemplate template = new DebuggableTemplate();
        String text = IOGroovyMethods.getText(reader);
        String script = template.parse(new StringReader(text));
        if (verbose) {
            System.out.println("\n-- script source --");
            System.out.print(script);
            System.out.println("\n-- script end --\n");
        }
        try {
            template.setFileName(fileName);
            template.setScript(groovyShell.parse(script, fileName));
        } catch (MultipleCompilationErrorsException e) {
            throw new TemplateParsingException(text, script, template.getPositionsMap(), e);
        } catch (Exception e) {
            throw new GroovyRuntimeException("Failed to parse template script (your template may contain an error or be trying to use expressions not currently supported): " + e.getMessage());
        }
        return template;
    }

    /**
     * @param verbose true if you want the engine to display the template source file for debugging purposes
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isVerbose() {
        return verbose;
    }
}