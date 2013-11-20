package groovyx.gaelyk.util;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

/**
 * {@link LineAndColumnNumberReader} is reader which exposes not only the current line number
 * but also the current column number.
 * 
 * As this is rather trivial implementation only {@link #read()}, {@link #mark(int)} 
 * and {@link #reset()} methods are supported.
 * 
 * Line and column numbers starts at 1.
 * 
 * @author Vladimir Orany
 */
class LineAndColumnNumberReader extends LineNumberReader implements Positionable {

    private int columnNumber = 1;
    private int markedColumnNumber = 0;

    /**
     * Create a new line-and-column-numbering reader,
     * reading characters into a buffer of the given size.
     * 
     * @param in
     *            A Reader object to provide the underlying stream
     * @param sz
     *            An int specifying the size of the buffer
     */
    public LineAndColumnNumberReader(Reader in, int sz) {
        super(in, sz);
    }

    /**
     * Create a new line-and-column numbering reader, using the default input-buffer
     * size.
     * 
     * @param in
     *            A Reader object to provide the underlying stream
     */
    public LineAndColumnNumberReader(Reader in) {
        super(in);
    }

    /**
     * Returns the current column number.
     * 
     * @return the current column number
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     * Sets the current column number.
     * 
     * @param columnNumber
     *            new current column number
     */
    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    @Override public int read(char[] cbuf, int off, int len) throws IOException {
        throw new UnsupportedOperationException("This Reader only supports reading character by character!");
    }

    @Override public String readLine() throws IOException {
        throw new UnsupportedOperationException("This Reader only supports reading character by character!");
    }

    @Override public void mark(int readAheadLimit) throws IOException {
        synchronized (lock) {
            super.mark(readAheadLimit);
            markedColumnNumber = columnNumber;
        }
    }

    @Override public void reset() throws IOException {
        synchronized (lock) {
            super.reset();
            columnNumber = markedColumnNumber;
            markedColumnNumber = 0;
        }
    }

    @Override public int read() throws IOException {
        synchronized (lock) {
            int origLineNumber = getLineNumber();
            int c = super.read();
            int newLineNumber = getLineNumber();
            if (c >= 0) {
                if (origLineNumber == newLineNumber) {
                    columnNumber++;
                } else {
                    columnNumber = 1;
                }
            }
            return c;
        }
    }
    
    @Override public int getLineNumber() {
        return super.getLineNumber() + 1;
    }

}
