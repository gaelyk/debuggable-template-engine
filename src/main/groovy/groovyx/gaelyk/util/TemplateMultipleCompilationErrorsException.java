package groovyx.gaelyk.util;

import java.util.Collections;
import java.util.Map;

import org.codehaus.groovy.control.MultipleCompilationErrorsException;

public class TemplateMultipleCompilationErrorsException extends MultipleCompilationErrorsException {

    private static final long serialVersionUID = -5963311079822514978L;
    
    private final String templateSource;
    private final Map<Position, Position> positionsMap;
    
    public TemplateMultipleCompilationErrorsException(DebuggableTemplate tpl, MultipleCompilationErrorsException e) {
        super(e.getErrorCollector());
        this.templateSource = tpl.getSource();
        this.positionsMap = tpl.getPositionsMap();
    }
    
    public String getTemplateSource() {
        return templateSource;
    }
    
    public Map<Position, Position> getPositionsMap() {
        return Collections.unmodifiableMap(positionsMap);
    }
    
}
