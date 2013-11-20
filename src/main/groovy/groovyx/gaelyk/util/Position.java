package groovyx.gaelyk.util;

/**
 * Line and column position
 * @author ladin
 *
 */
class Position implements Positionable{
    
    public static final Position NOT_FOUND = new Position(0, 0);

    /**
     * Line number
     */
    public final int line;
    
    /**
     * Column number
     */
    public final int column;

    /**
     * Returns {@link Position} object representing given line and column positions
     * @param line line number
     * @param column column number
     * @return {@link Position} object representing given line and column positions
     */
    public static Position at(int line, int column) {
        return new Position(line, column);
    }
    
    /**
     * Returns {@link Position} object representing given line and column positions
     * @param pos {@link Positionable} object from which are line and column positions taken
     * @return {@link Position} object representing given line and column positions
     */
    public static Position from(Positionable pos) {
        return new Position(pos.getLineNumber(), pos.getColumnNumber());
    }
    
    @Override public int getLineNumber() {
        return line;
    }
    
    @Override public int getColumnNumber() {
        return column;
    }
    
    private Position(int line, int column) {
        this.line = line;
        this.column = column;
    }
    
    @Override public String toString() {
        return "[l:" + line + ",c:" + column + "]";
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + column;
        result = prime * result + line;
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Position other = (Position) obj;
        if (column != other.column) return false;
        if (line != other.line) return false;
        return true;
    }
    
}
