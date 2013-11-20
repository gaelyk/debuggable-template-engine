package groovyx.gaelyk.dte;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.codehaus.groovy.syntax.SyntaxException;

public class TemplateParsingException extends RuntimeException {

    private static final long             serialVersionUID = -5963311079822514978L;

    private final String                  templateSource;
    private final String                  parsedScript;
    private final Map<Position, Position> positionsMap;

    public TemplateParsingException(String templateSource, String parsedScript, Map<Position, Position> positionsMap, MultipleCompilationErrorsException e) {
        super(e);
        this.templateSource = templateSource;
        this.parsedScript = parsedScript;
        this.positionsMap = positionsMap;
    }

    String getTemplateSource() {
        return templateSource;
    }
    
    String getParsedScript() {
        return parsedScript;
    }

    Map<Position, Position> getPositionsMap() {
        return Collections.unmodifiableMap(positionsMap);
    }
    
    @Override public String getMessage() {
        return DefaultGroovyMethods.join(collectCompilationErrorDetails(), "\n");
    }

    @SuppressWarnings("unchecked") private List<String> collectCompilationErrorDetails() {
        String[] sourceLines = getTemplateSource().split("\n");
        String[] scriptLines = getParsedScript().split("\n");
        
        List<String> details = new ArrayList<String>();
        for (Message message : ((List<Message>) ((MultipleCompilationErrorsException) getCause()).getErrorCollector().getErrors())) {
            if (!(message instanceof SyntaxErrorMessage)) {
                continue;
            }
            SyntaxException compilationError = ((SyntaxErrorMessage) message).getCause();
            
            Position errorPosition = Position.at(compilationError.getStartLine(), compilationError.getStartColumn());
            details.addAll(collectCompilationErrorDetail(sourceLines, compilationError.getCause().getMessage(), 
                    getPositionsMap().get(errorPosition)));
            details.addAll(collectCompilationErrorDetail(scriptLines, "The template was parsed into following script:", errorPosition));
            details.add("");
        }
        return details;
    }
    
    private List<String> collectCompilationErrorDetail(String[] sourceLines, String errorMessage, Position startPosition) {
        List<String> details = new ArrayList<String>();
        details.add("");
        details.add(errorMessage);
        details.add("");
        int detailStartLine = Math.max(0, startPosition.line - 6);
        int detailEndLine = Math.min(startPosition.line + 5, sourceLines.length - 1);
        int counter = detailStartLine + 1;
        int padding = 4;
        for (int i = 0; i < sourceLines.length; i++) {
            if (i < detailStartLine) {
                continue;
            }
            if (i > detailEndLine) {
                continue;
            }
            details.add(StringGroovyMethods.padRight("" + counter, padding) + ":" + sourceLines[i]);
            if (startPosition.line == i + 1) {
                details.add(StringGroovyMethods.padRight("=", padding) + ":" + StringGroovyMethods.multiply(" ", startPosition.column - 1) + "^");
            }
            counter++;
        }
        return details;
    }

}
