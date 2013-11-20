package groovyx.gaelyk.util;

import groovy.lang.Binding;
import groovy.lang.Script;
import groovy.lang.Writable;
import groovy.text.Template;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.codehaus.groovy.runtime.InvokerHelper;

public class DebuggableTemplate implements Template {

    DebuggableTemplate(){
        // disallow creation outside this package
    }
    
    private Script script;
    private String source;
    private Map<Position, Position> positionsMap;

    public Writable make() {
        return make(null);
    }

    public Writable make(@SuppressWarnings("rawtypes") final Map map) {
        return new Writable() {
            /**
             * Write the template document with the set binding applied to the writer.
             *
             * @see groovy.lang.Writable#writeTo(java.io.Writer)
             */
            public Writer writeTo(Writer writer) {
                Binding binding;
                if (map == null)
                    binding = new Binding();
                else
                    binding = new Binding(map);
                Script scriptObject = InvokerHelper.createScript(script.getClass(), binding);
                PrintWriter pw = new PrintWriter(writer);
                scriptObject.setProperty("out", pw);
                scriptObject.run();
                pw.flush();
                return writer;
            }

            /**
             * Convert the template and binding into a result String.
             *
             * @see java.lang.Object#toString()
             */
            public String toString() {
                StringWriter sw = new StringWriter();
                writeTo(sw);
                return sw.toString();
            }
        };
    }

    /**
     * Parse the text document looking for <% or <%= and then call out to the appropriate handler, otherwise copy the text directly
     * into the script while escaping quotes.
     *
     * @param reader a reader for the template text
     * @return the parsed text
     * @throws IOException if something goes wrong
     */
    String parse(Reader r) throws IOException {
        LineAndColumnNumberReader reader = new LineAndColumnNumberReader(r);
        
        DebuggableStringWriter sw = new DebuggableStringWriter(reader);
        startScript(sw);
        int c;
        while ((c = reader.read()) != -1) {
            if (c == '<') {
                reader.mark(1);
                c = reader.read();
                if (c != '%') {
                    sw.write('<');
                    reader.reset();
                } else {
                    reader.mark(1);
                    c = reader.read();
                    if (c == '=') {
                        groovyExpression(reader, sw);
                    } else {
                        reader.reset();
                        groovySection(reader, sw);
                    }
                }
                continue; // at least '<' is consumed ... read next chars.
            }
            if (c == '$') {
                reader.mark(1);
                c = reader.read();
                if (c != '{') {
                    sw.write('$');
                    reader.reset();
                } else {
                    reader.mark(1);
                    sw.write("${");
                    processGSstring(reader, sw);
                }
                continue; // at least '$' is consumed ... read next chars.
            }
            if (c == '\"') {
                sw.write('\\');
            }
            /*
             * Handle raw new line characters.
             */
            if (c == '\n' || c == '\r') {
                if (c == '\r') { // on Windows, "\r\n" is a new line.
                    reader.mark(1);
                    c = reader.read();
                    if (c != '\n') {
                        reader.reset();
                    }
                }
                sw.write("\n");
                continue;
            }
            sw.write(c);
        }
        endScript(sw);
        positionsMap = sw.getPositionsMap();
        return source = sw.toString();
    }

    private void startScript(StringWriter sw) {
        sw.write("out.print(\"\"\"");
    }

    private void endScript(StringWriter sw) {
        sw.write("\"\"\");\n");
        sw.write("\n/* Generated by SimpleTemplateEngine */");
    }

    private void processGSstring(Reader reader, StringWriter sw) throws IOException {
        int c;
        while ((c = reader.read()) != -1) {
            if (c != '\n' && c != '\r') {
                sw.write(c);
            }
            if (c == '}') {
                break;
            }
        }
    }

    /**
     * Closes the currently open write and writes out the following text as a GString expression until it reaches an end %>.
     *
     * @param reader a reader for the template text
     * @param sw     a StringWriter to write expression content
     * @throws IOException if something goes wrong
     */
    private void groovyExpression(Reader reader, StringWriter sw) throws IOException {
        sw.write("${");
        int c;
        while ((c = reader.read()) != -1) {
            if (c == '%') {
                c = reader.read();
                if (c != '>') {
                    sw.write('%');
                } else {
                    break;
                }
            }
            if (c != '\n' && c != '\r') {
                sw.write(c);
            }
        }
        sw.write("}");
    }

    /**
     * Closes the currently open write and writes the following text as normal Groovy script code until it reaches an end %>.
     *
     * @param reader a reader for the template text
     * @param sw     a StringWriter to write expression content
     * @throws IOException if something goes wrong
     */
    private void groovySection(Reader reader, StringWriter sw) throws IOException {
        sw.write("\"\"\");");
        int c;
        while ((c = reader.read()) != -1) {
            if (c == '%') {
                c = reader.read();
                if (c != '>') {
                    sw.write('%');
                } else {
                    break;
                }
            }
            /* Don't eat EOL chars in sections - as they are valid instruction separators.
             * See http://jira.codehaus.org/browse/GROOVY-980
             */
            // if (c != '\n' && c != '\r') {
            sw.write(c);
            //}
        }
        sw.write(";\nout.print(\"\"\"");
    }
    
    /**
     * Returns the source of the generated script.
     * 
     * {@link #parse(Reader)} must be called before this method.
     * 
     * @return the source of the generated script
     */
    public String getSource() {
        if (source == null) {
            throw new IllegalStateException("Parse method hasn't been called yet!");
        }
        return source;
    }
    
    /**
     * Returns map translating compiled positions into the original ones.
     * @return map translating compiled positions into the original ones
     */
    public Map<Position, Position> getPositionsMap() {
        if (positionsMap == null) {
            throw new IllegalStateException("Parse method hasn't been called yet!");
        }
        return positionsMap;
    }
    
    /**
     * Sets the script for this template.
     * @param script the script for this template
     */
    void setScript(Script script) {
        this.script = script;
    }
    
    /**
     * Returns the script for this template.
     * @return the script for this template
     */
    Script getScript() {
        return script;
    }

}