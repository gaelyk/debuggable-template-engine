package groovyx.gaelyk.dte;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * String writer which keep its line and column position.
 * 
 * Line and column numbers starts at 1.
 * 
 * It also keeps map translating positions written to this writer
 * and the original ones.
 * 
 * @author Vladimir Orany
 */
class DebuggableStringWriter extends StringWriter implements Positionable {

    private int                     lineNumber   = 0;
    private int                     columnNumber = 0;
    private boolean                 wasNewLine   = true;

    private Map<Position, Position> positionsMap = new LinkedHashMap<>();
    private final Positionable      positionable;

    public DebuggableStringWriter(Positionable positionable) {
        super();
        this.positionable = positionable;
    }

    public DebuggableStringWriter(Positionable positionable, int initialSize) {
        super(initialSize);
        this.positionable = positionable;
    }

    @Override public void write(int c) {
        if (wasNewLine && c != '\r') {
            wasNewLine = false;
            lineNumber++;
            columnNumber = 1;
        } else {
            columnNumber++;            
        }
        
        if (c == '\n') {
            wasNewLine = true;
        }
        
        Position ourPos = Position.from(this);
        Position theirPos = Position.from(positionable);
        positionsMap.put(ourPos, theirPos);            
        super.write(c);

        
    }

    @Override public void write(char[] cbuf) throws IOException {
        for (char c : cbuf) {
            write(c);
        }
    }

    @Override public void write(String str) {
        for (char c : str.toCharArray()) {
            write(c);
        }
    }

    @Override public StringWriter append(CharSequence csq, int start, int end) {
        throw new UnsupportedOperationException("Only append(char|String) is supported");
    }

    @Override public StringWriter append(char c) {
        write(c);
        return this;
    }

    @Override public StringWriter append(CharSequence csq) {
        for (int i = 0; i < csq.length(); i++) {
            append(csq.charAt(i));
        }
        return this;
    }
    
    @Override public void write(char[] cbuf, int off, int len) {
        throw new UnsupportedOperationException("Only write(char|String|char[]) is supported");
    }
    
    @Override public void write(String str, int off, int len) {
        throw new UnsupportedOperationException("Only write(char|String|char[]) is supported");
    }

    @Override public int getColumnNumber() {
        return columnNumber;
    }

    @Override public int getLineNumber() {
        return lineNumber;
    }
    
    /**
     * Returns map mapping positions in written string back to the original one.
     * @return map mapping positions in written string back to the original one.
     */
    public Map<Position, Position> getPositionsMap() {
        return Collections.unmodifiableMap(positionsMap);
    }

}
