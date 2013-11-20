package groovyx.gaelyk.util

import spock.lang.Specification

class DebuggableStringWriterSpec extends Specification {
    
    def "Positions map is created"() {
        Positionable positionable = Mock()
        DebuggableStringWriter writer = new DebuggableStringWriter(positionable)
        
        
        when: "char is written to the writer"
        writer.write((int) 'H')
        
        then: "position is writen to the map"
        writer.lineNumber == 1
        writer.columnNumber == 1
        writer.positionsMap.size() == 1
        writer.positionsMap[Position.at(1,1)] == Position.at(1, 2)
        1 * positionable.getLineNumber() >> 1
        1 * positionable.getColumnNumber() >> 2
        
        when: "new line occurs"
        writer.write((int) '\n')
        
        then: "then line number remains the same until next char"
        writer.lineNumber == 1
        writer.columnNumber == 2 
        writer.positionsMap.size() == 2
        writer.positionsMap[Position.at(1,2)] == Position.at(1, 3)
        1 * positionable.getLineNumber() >> 1
        1 * positionable.getColumnNumber() >> 3
        
        when: "carridge return is written"
        writer.write((int) '\r')
        
        then: "it is taken as regular char at the end of the line"
        writer.lineNumber == 1
        writer.columnNumber == 3
        writer.positionsMap.size() == 3
        1 * positionable.getLineNumber() >> 1
        1 * positionable.getColumnNumber() >> 4
        
        when: "char at new line is s written"
        writer.write((int) 'a')
        
        then: "column number is reset and line number increased"
        writer.lineNumber == 2
        writer.columnNumber == 1
        writer.positionsMap.size() == 4
        1 * positionable.getLineNumber() >> 2
        1 * positionable.getColumnNumber() >> 0
        
        expect: "everything is contained in the result string"
        writer.toString() == "H\n\ra"
        
    }

}
