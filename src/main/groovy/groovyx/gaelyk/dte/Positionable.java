package groovyx.gaelyk.dte;

/**
 * Iterface for objects which are able to tell its current line and column number.
 * @author Vladimir Orany
 *
 */
interface Positionable {

    /**
     * Returns current line number.
     * @return current line number
     */
    int getLineNumber();
    
    /**
     * Returns current column number.
     * @return current column number
     */
    int getColumnNumber();
    
}
