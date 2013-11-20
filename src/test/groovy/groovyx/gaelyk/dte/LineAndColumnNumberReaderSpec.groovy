package groovyx.gaelyk.dte

import groovyx.gaelyk.dte.LineAndColumnNumberReader;
import spock.lang.Specification

class LineAndColumnNumberReaderSpec extends Specification {
    
    StringReader source = new StringReader("Thiš iš á tést string.\nAnd this is a new line!")
    LineAndColumnNumberReader reader = new LineAndColumnNumberReader(source)
    
    def "On read the column number increses and get reset on the new line"() {
        expect: "the line number to be one at the beginning "
        reader.columnNumber == 1
        
        when: "one char is read"
        reader.read()
        
        then: "the column number increased by one"
        reader.columnNumber == 2
        
        when: "we get to the next line"
        22.times { reader.read() }
        
        then: "the column number resets back to one"
        reader.columnNumber == 1
        
        when: "we read next char on the next line"
        reader.read()
        
        then: "the column number starts increasing again"
        reader.columnNumber == 2
    }
    
    def "Mark and reset method should be supported to allow look ahead"() {
        when: "some chars are read and than marked"
        5.times { reader.read() }
        reader.mark(1)
        int markedColumnNumber = reader.columnNumber
        
        then: "the marked column number is the same as the current one"
        reader.markedColumnNumber == reader.columnNumber
        
        when: "next char is read"
        reader.read()
        
        then: "the marked column number remains the same but the current line number increases"
        reader.columnNumber == markedColumnNumber + 1
        reader.markedColumnNumber == markedColumnNumber
        
        when: "the reader is reset"
        reader.reset()
        
        then: "the column number is again the one marked and the marked one is back to zero"
        reader.columnNumber == markedColumnNumber
        reader.markedColumnNumber == 0
    }
    
}
