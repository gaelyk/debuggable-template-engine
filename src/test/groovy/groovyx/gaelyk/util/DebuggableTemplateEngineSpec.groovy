package groovyx.gaelyk.util

import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import spock.lang.Specification

class DebuggableTemplateEngineSpec extends Specification {

    
    def "Failing to parse template give much detailed information"() {
        GroovyShell shell = new GroovyShell()
        DebuggableTemplateEngine dte = new DebuggableTemplateEngine(shell)
        
        String source = """
        Hello world!
        <% if (true) %>
        This will fail due missing opening bracket!
        <% } %>"""
        
        String script = '''out.print("""
        Hello world!
        """); if (true) ;
out.print("""
        This will fail due missing opening bracket!
        """); } ;
out.print("""""");

/* Generated by SimpleTemplateEngine */'''
        
        when:
        dte.createTemplate(source).make()
        
        then:
        TemplateMultipleCompilationErrorsException e = thrown(TemplateMultipleCompilationErrorsException)
        e.templateSource == script
        
        when:
        SyntaxException semsg = e.errorCollector.errors[0].cause
        
        then:
        e.positionsMap[Position.at(semsg.startLine, semsg.startColumn)] == Position.at(3, 24)
        e.positionsMap[Position.at(semsg.endLine, semsg.endColumn)]     == Position.at(3, 24)
        
    }
    
}
