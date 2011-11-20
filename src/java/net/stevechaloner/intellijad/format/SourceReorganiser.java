/*
 * Copyright 2007 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package net.stevechaloner.intellijad.format;

import com.intellij.openapi.diagnostic.Logger;

import net.stevechaloner.intellijad.decompilers.DecompilationContext;
import net.stevechaloner.intellijad.vfs.MemoryVirtualFile;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reorganises decompiled code to allow debugging.
 * <p>
 * This class is based on com.tagtraum.ideajad.LineSorter.java
 * </p>
 *
 * @author Steve Chaloner
 */
public class SourceReorganiser
{
    /**
     * Defines the placement of line numbers inserted as comments by Jad.
     */
    private static final Pattern LINE_NUMBER_PATTERN = Pattern.compile("^/\\*\\s*(\\d+)\\*/");

    /**
     * The system line separator.
     */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final int LINE_NUMBER_MARKER_LENGTH = 8;

    public static void reorganise(DecompilationContext context,
                                  MemoryVirtualFile file)
    {
        LineNumberReader in = null;
        StringWriter out = new StringWriter();
        boolean retainLineNumbers = context.getConfig().isLineNumbersAsComments();
        try
        {
            in = new LineNumberReader(new StringReader(file.getContent()));
            List<String> lines = reformat(in);
            Block currentBlock = new Block();
            for (String line : lines)
            {
                Matcher lineNumberMatcher = LINE_NUMBER_PATTERN.matcher(line);
                if (lineNumberMatcher.find())
                {
                    // we have a line  number
                    String lineNumberString = lineNumberMatcher.group(1);
                    int lineNumber = Integer.parseInt(lineNumberString);
                    String lineStringWithoutNumber = line.substring(lineNumberMatcher.end());
                    if (currentBlock.getLastLine() != null && currentBlock.getLastLine().getNumber() == lineNumber)
                    {
                        // if the last line exists and has the same number, add this line to it without number
                        currentBlock.getLastLine().add(lineStringWithoutNumber);
                    }
                    else
                    {
                        // if this line is a new line, i.e. the last line has a different number, add a new line to the block
                        currentBlock.add(new Line(lineStringWithoutNumber,
                                                  lineNumber,
                                                  retainLineNumbers));
                    }
                    String lastLine = currentBlock.getLastLine().getContent();
                    if (lastLine.endsWith("{"))
                    {
                        currentBlock = new Block(currentBlock,
                                                 currentBlock.removeLastLine());
                    }
                }
                else
                {
                    // no line number
                    String trimmedLineString = line.trim();
                    if ("{".equals(trimmedLineString))
                    {
                        currentBlock.getLastLine().add("{");
                        // this means we are starting a new block
                        currentBlock = new Block(currentBlock, currentBlock.removeLastLine());
                    }
                    else if (trimmedLineString.endsWith("{"))
                    {
                        if (trimmedLineString.startsWith("}"))
                        {
                            int closingBrace = line.indexOf('}');
                            // get rid of whatever is coming after '}'
                            currentBlock.add(new Line(line.substring(0,
                                                                     closingBrace + 1)));
                            currentBlock = currentBlock.getParent();
                            // get rid of '}'
                            line = line.substring(0, closingBrace) + line.substring(closingBrace + 1).trim();
                        }
                        // this means we are starting a new block
                        currentBlock = new Block(currentBlock,
                                                 new Line(line));
                    }
                    else if ("}".equals(trimmedLineString))
                    {
                        currentBlock.add(new Line(line));
                        currentBlock = currentBlock.getParent();
                    }
                    else if (trimmedLineString.startsWith("}"))
                    {
                        int closingBrace = line.indexOf('}');
                        // get rid of whatever is coming after '}'
                        currentBlock.add(new Line(line.substring(0,
                                                                 closingBrace + 1)));
                        currentBlock = currentBlock.getParent();
                        // get rid of '}'
                        line = line.substring(0, closingBrace) + line.substring(closingBrace + 1).trim();
                        currentBlock.add(new Line(line));
                    }
                    else
                    {
                        currentBlock.add(new Line(line));
                    }
                }
            }
            // try to sort things
            currentBlock.sort();
            // let's print it out
            currentBlock.write(out);
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    Logger.getInstance(SourceReorganiser.class.getName()).error(e);
                }
            }
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (IOException e)
                {
                    Logger.getInstance(SourceReorganiser.class.getName()).error(e);
                }
            }
        }
        file.setContent(out.getBuffer().toString());
    }

    private static List<String> reformat(LineNumberReader in)
    {
        String lineString;
        int lastIndent = 0;
        int indent = -1;
        boolean lastLineHadNoNumber = false;
        List<String> lines = new ArrayList<String>();
        try
        {
            while ((lineString = in.readLine()) != null)
            {
                if (lineString.length() > LINE_NUMBER_MARKER_LENGTH && !lineString.startsWith("/*"))
                {
                    int thisIndent = 0;
                    // count spaces or tabs
                    for (; thisIndent < lineString.length() && (lineString.charAt(thisIndent) == ' ' || lineString.charAt(thisIndent) == '\t'); thisIndent++)
                    {
                    }
                    thisIndent = thisIndent - LINE_NUMBER_MARKER_LENGTH;
                    if (indent == -1 && thisIndent > 0)
                    {
                        indent = thisIndent;
                    }
                    if ((thisIndent - lastIndent) / indent == 2 && lastLineHadNoNumber)
                    {
                        // add this line to the last line
                        String l = lines.get(lines.size() - 1) + ' ' + lineString.trim();
                        lines.set(lines.size() - 1, l);
                    }
                    else
                    {
                        lastIndent = thisIndent;
                        lines.add(lineString);
                    }
                    lastLineHadNoNumber = true;
                }
                else
                {
                    // strip case statement comments, which have the form "case 2: // '\002'"
                    int casePos = lineString.indexOf("case");
                    int colon;
                    if (casePos != -1 && (colon = lineString.indexOf(": //", casePos)) != -1)
                    {
                        lineString = lineString.substring(0, colon + 2);
                    }
                    lines.add(lineString);
                    lastLineHadNoNumber = false;
                }
            }
        }
        catch (IOException e)
        {
            Logger.getInstance(SourceReorganiser.class.getName()).error(e);
        }
        return lines;
    }

    private abstract static class Element
    {
        public abstract boolean hasNumber();

        public abstract int getNumber();
    }

    private static class Line extends Element
    {
        private int number = -1;
        private String content;
        private boolean lineNumberAsSuffix;

        Line(String content)
        {
            this(content,
                 -1,
                 false);
        }

        Line(String content,
                    int number,
                    boolean lineNumberAsSuffix)
        {
            this.number = number;
            this.content = content;
            this.lineNumberAsSuffix = lineNumberAsSuffix;
        }

        void add(String content)
        {
            this.content = this.content + " " + content.trim();
        }

        public int getNumber()
        {
            return number;
        }

        public boolean hasNumber()
        {
            return number != -1;
        }

        String getContent()
        {
            return content;
        }

        boolean getLineNumberAsSuffix()
        {
            return lineNumberAsSuffix;
        }
    }

    private static class Block extends Element
    {

        private Block parent;
        private List<Element> elements;

        Block()
        {
            elements = new ArrayList<Element>();
        }

        Block(Block parent, Line firstLine)
        {
            this();
            this.parent = parent;
            if (parent != null)
            {
                parent.add(this);
            }
            add(firstLine);
        }

        void sort()
        {
            if (isSorted())
            {
                // naive sorting algo
                List<Element> sortedElements = new ArrayList<Element>();
                int insertionPos = 0;
                Element lastElement = elements.get(elements.size() - 1);
                int size = elements.size();
                if (lastElement instanceof Line)
                {
                    size--;
                }
                for (int i = 0; i < size; i++)
                {
                    Element element = elements.get(i);
                    if (element instanceof Block)
                    {
                        ((Block) element).sort();
                    }
                    if (!element.hasNumber())
                    {
                        sortedElements.add(insertionPos, element);
                    }
                    else
                    {
                        for (insertionPos = 0; insertionPos < sortedElements.size(); insertionPos++)
                        {
                            Element sortedElement = sortedElements.get(insertionPos);
                            if (sortedElement.hasNumber() && sortedElement.getNumber() > element.getNumber())
                            {
                                break;
                            }
                        }
                        sortedElements.add(insertionPos, element);
                    }
                    insertionPos++;
                }
                if (lastElement instanceof Line)
                {
                    sortedElements.add(lastElement);
                }
                elements = sortedElements;
            }
        }

        Block getParent()
        {
            return parent;
        }

        Line getLastLine()
        {
            Object lastElement = elements.get(elements.size() - 1);
            if (lastElement instanceof Line)
            {
                return (Line) lastElement;
            }
            return null;
        }

        Line removeLastLine()
        {
            Object lastElement = elements.get(elements.size() - 1);
            if (lastElement instanceof Line)
            {
                elements.remove(elements.size() - 1);
                return (Line) lastElement;
            }
            return null;
        }

        void add(Element element)
        {
            elements.add(element);
        }

        public boolean hasNumber()
        {
            return getNumber() != -1;
        }

        public int getNumber()
        {
            int lowestNumber = -1;
            for (Element element : elements)
            {
                if (element.hasNumber())
                {
                    if (lowestNumber == -1 || element.getNumber() < lowestNumber)
                    {
                        lowestNumber = element.getNumber();
                    }
                }
            }
            return lowestNumber;
        }

        /**
         * Checks whether the lines in this block are sorted.
         *
         * @return true if the lines are sorted
         */
        boolean isSorted()
        {
            int currentLineNumber = 0;
            for (Element element : elements)
            {
                if (element instanceof Line)
                {
                    Line line = (Line) element;
                    if (line.hasNumber() && line.getNumber() < currentLineNumber)
                    {
                        return false;
                    }
                    if (line.hasNumber())
                    {
                        currentLineNumber = line.getNumber();
                    }
                }
            }
            return true;
        }

        void write(StringWriter out)
        {
            List<Line> lines = getLines();
            Map<Integer, Line> offLines = findOffLines(lines);
            int currentLine = 1;
            boolean lastLineHadNumber = false;
            for (int i = 0; i < lines.size(); i++)
            {
                Line line = lines.get(i);
                if (line.hasNumber())
                {
                    while (currentLine < line.getNumber())
                    {
                        out.write(LINE_SEPARATOR);
                        currentLine++;
                        checkOffLines(offLines,
                                      line,
                                      currentLine,
                                      out);
                    }
                    lastLineHadNumber = true;
                    if (currentLine != line.getNumber())
                    {
                        out.write("/*  */");
                    }
                }
                else
                {
                    Line nextLineWithNumber = null;
                    int linesInbetween = 0;
                    for (int j = i + 1; j < lines.size(); j++)
                    {
                        Line l = lines.get(j);
                        if (l.hasNumber())
                        {
                            nextLineWithNumber = l;
                            linesInbetween = j - i;
                            break;
                        }
                    }
                    if (nextLineWithNumber != null)
                    {
                        int linesToSkip = nextLineWithNumber.getNumber() - currentLine - linesInbetween;
                        if (lastLineHadNumber)
                        {
                            linesToSkip = Math.min(1,
                                                   linesToSkip);
                        }
                        for (int k = 0, max = linesToSkip; k < max; k++)
                        {
                            out.write(LINE_SEPARATOR);
                            currentLine++;
                            checkOffLines(offLines,
                                          line,
                                          currentLine,
                                          out);
                        }
                    }
                    else
                    {
                        out.write(LINE_SEPARATOR);
                        currentLine++;
                        checkOffLines(offLines,
                                      line,
                                      currentLine,
                                      out);
                    }
                    lastLineHadNumber = false;
                }
                out.write(line.getContent());// + (line.hasNumber() && line.getLineNumberAsSuffix() ? " // " + line.getNumber() : ""));
            }
        }

        private void checkOffLines(Map offLines,
                                   Line line,
                                   int currentLine,
                                   StringWriter out)
        {
            if (offLines.containsKey(currentLine))
            {
                Line offLine = (Line) offLines.get(currentLine);
                if (!offLine.getContent().equals(line.getContent()))
                {
                    out.write("// off: " + offLine.getContent().substring(8));
                }
            }
        }

        private Map<Integer, Line> findOffLines(List<Line> lines)
        {
            Map<Integer, Line> offLines = new HashMap<Integer, Line>();
            int currentLine = 1;
            for (Line line : lines)
            {
                if (line.hasNumber())
                {
                    if (line.getNumber() < currentLine)
                    {
                        offLines.put(line.getNumber(),
                                     line);
                    }
                    else
                    {
                        currentLine = line.getNumber();
                    }
                }
            }
            return offLines;
        }

        private List<Line> getLines()
        {
            List<Line> lines = new ArrayList<Line>();
            for (Element element : elements)
            {
                if (element instanceof Line)
                {
                    lines.add((Line) element);
                }
                else
                {
                    Block block = (Block) element;
                    lines.addAll(block.getLines());
                }
            }
            return lines;
        }

        public String toString()
        {
            return toString(0);
        }

        private String toString(int indent)
        {
            StringBuffer sb = new StringBuffer();
            sb.append("Block ").append(getNumber()).append("\r\n");
            for (Element element : elements)
            {
                if (element instanceof Line)
                {
                    for (int j = 0; j < indent; j++)
                    {
                        sb.append("+");
                    }
                    sb.append("|").append(element.getNumber()).append(' ').append(((Line) element).getContent()).append("\r\n");
                }
                else
                {
                    sb.append(((Block)element).toString(indent + 1));
                }
            }
            return sb.toString();
        }
    }
}
