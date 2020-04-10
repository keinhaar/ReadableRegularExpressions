package de.exware.rre;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A readable Pattern implementation. Makes it easier to define an later on check what an 
 * regular Expression does, because it can be defined in natural language with it's fluent API.
 * It's also possible to parse these fluent API from text form. So it may be used be people
 * who never did programming.
 * Example:
 * <pre>
 * ReadablePattern pattern = new Builder()
 *    .oneOf('a','b','c')
 *    .oneOrMore()
 *    .add("xyz")
 *    .zeroOrMore()
 *    .build();
 * System.out.println(pattern.matches("bxyz"));
 * </pre>
 * writes true, and results in the Pattern "[abc]+xyz*"
 */
public class ReadablePattern
{
    public static final String WORD_CHARACTER = "\\w";
    public static final String NON_WORD_CHARACTER = "\\W";
    public static final String DIGIT = "\\d";
    public static final String NON_DIGIT = "\\D";
    private Pattern pattern;
    private String readableRegex;
    
    /**
     * Create a ReadablePattern by calling ReadablePattern.compile(pattern) or by using a Builder.
     */
    private ReadablePattern()
    {
    }
    
    /**
     * Used to create an ReadablePattern with an fluent API.
     */
    public static class Builder
    {
        int groupCount;
        StringBuilder regex = new StringBuilder();
        StringBuilder remaining;
        StringBuilder readableRegex = new StringBuilder();
        int flags = Pattern.MULTILINE;
        
        /**
         * Creates a new empty Builder.
         */
        public Builder()
        {
        }
        
        /**
         * Create a new Builder which parses the Expression from Text.
         * @param readableRegExp the textual representation of the expression.
         */
        public Builder(String readableRegExp)
        {
            readableRegExp = readableRegExp.replaceAll("(?s)\\)[ \\r\\n\\t]+\\.", ")."); //replace whitespace between methods.
            remaining = new StringBuilder(readableRegExp);
            String token = nextToken();
            while(token != null)
            {
                handleToken(token);
                token = nextToken();
            }
        }
        
        /**
         * Handles a single token of the textual regex.
         * Translates the token to a method call.
         * Allowed tokens match one of the public methods of this class except build().
         * @param token an element of the text input. For example add("abc")
         */
        private void handleToken(String token)
        {
            int i = token.indexOf('(');
            int ei = token.lastIndexOf(')');
            String param = token.substring(i+1, ei);
            if(token.startsWith("add("))
            {
                param = removeParagraph(param);
                add(param);
            }
            else if(token.startsWith("addRegEx("))
            {
                param = removeParagraph(param);
                addRegEx(param);
            }
            else if(token.startsWith("oneOf("))
            {
                handleOneOf(param);
            }
            else if(token.startsWith("range("))
            {
                handleRange(param);
            }
            else if(token.startsWith("count("))
            {
                handleCount(param);
            }
            else if(token.startsWith("zeroOrMore("))
            {
                zeroOrMore();
            }
            else if(token.startsWith("zeroOrMoreShortest("))
            {
                zeroOrMoreShortest();
            }
            else if(token.startsWith("zeroOrOne("))
            {
                zeroOrOne();
            }
            else if(token.startsWith("oneOrMore("))
            {
                oneOrMore();
            }
            else if(token.startsWith("oneOrMoreShortest("))
            {
                oneOrMoreShortest();
            }
            else if(token.startsWith("lazy("))
            {
                lazy();
            }
            else if(token.startsWith("digit("))
            {
                digit();
            }
            else if(token.startsWith("notDigit("))
            {
                notDigit();
            }
            else if(token.startsWith("alpha("))
            {
                alpha();
            }
            else if(token.startsWith("notAlpha("))
            {
                notAlpha();
            }
            else if(token.startsWith("lineBreak("))
            {
                lineBreak();
            }
            else if(token.startsWith("tab("))
            {
                tab();
            }
            else if(token.startsWith("capture("))
            {
                capture();
            }
            else if(token.startsWith("captureEnd("))
            {
                captureEnd();
            }
            else if(token.startsWith("anyCharacter("))
            {
                anyCharacter();
            }
            else if(token.startsWith("startOfLine("))
            {
                startOfLine();
            }
            else if(token.startsWith("endOfLine("))
            {
                endOfLine();
            }
            else if(token.startsWith("ignoreCase("))
            {
                ignoreCase(isEmptyOrTrue(param));
            }
            else if(token.startsWith("singleLine("))
            {
                singleLine();
            }
            else
            {
                throw new IllegalArgumentException("Unknown token: " + token);
            }
        }

        /**
         * check if the parameter is null, empty or "true".
         * @param param
         * @return
         */
        private boolean isEmptyOrTrue(String param)
        {
            return param == null || param.length() == 0 || Boolean.parseBoolean(param);
        }
        
        /**
         * Removes quotation marks from param.
         * @param param
         * @return
         */
        private String removeParagraph(String param)
        {
            param = param.trim();
            if(param.startsWith("\"") && param.endsWith("\"")
                || param.startsWith("'") && param.endsWith("'"))
            {
                param = param.substring(1, param.length()-1);
            }
            return param;
        }
        
        /**
         * helper method to parse the possible count parameters.
         * @param param
         */
        private void handleCount(String param)
        {
            String[] parts = splitParameter(param);
            int[] iparts = new int[parts.length];
            for(int i=0;i<parts.length;i++)
            {
                iparts[i] = Integer.parseInt(removeParagraph(parts[i]));
            }
            if(parts.length > 2) 
            {
                throw new IllegalArgumentException("To many Parameters for multiple.");
            }
            if(iparts.length == 1)
            {
                count(iparts[0]);
            }
            else
            {
                count(iparts[0], iparts[1]);
            }
        }
        
        /**
         * helper method to parse the possible oneOf parameters.
         * @param param
         */
        private void handleOneOf(String param)
        {
            String[] parts = splitParameter(param);
            for(int i=0;i<parts.length;i++)
            {
                parts[i] = removeParagraph(parts[i]);
            }
            oneOf(parts);
        }
        
        private String[] splitParameter(String param)
        {
            List<String> parts = new ArrayList<>();
            int is = 0;
            boolean escaped = false;
            boolean quoted = false;
            for(int i=0;i<param.length();i++)
            {
                char c = param.charAt(i);
                if((c == ',' && quoted == false && escaped == false))
                {
                    parts.add(param.substring(is, i));
                    is = i+1;
                }
                if((c == '\'' || c == '"'))
                {
                    quoted = quoted == false;
                }
                escaped = c == '\\';
            }
            parts.add(param.substring(is));
            return parts.toArray(new String[parts.size()]);
        }
        
        /**
         * helper method to parse the possible range parameters.
         * @param param
         */
        private void handleRange(String param)
        {
            String[] parts = splitParameter(param);
            char[] cparts = new char[parts.length];
            for(int i=0;i<parts.length;i++)
            {
                parts[i] = removeParagraph(parts[i]);
                cparts[i] = parts[i].charAt(0);
            }
            range(cparts);
        }
        
        /**
         * adds a quantifier with upper and lower limit.
         */
        public Builder count(int from, int to)
        {
            _appendRRE(".count(");
            _appendRRE(from);
            _appendRRE(",");
            _appendRRE(to);
            _appendRRE(")");
            _add("{" + from);
            _add("," +to);
            _add("}");
            return this;
        }
        
        /**
         * adds a quantifier with exact limit. no less and no more matches.
         * @param count number of expected matches.
         * @return
         */
        public Builder count(int count)
        {
            _appendRRE(".count(");
            _appendRRE(count);
            _appendRRE(")");
            _add("{" + count);
            _add("}");
            return this;
        }
        
        /**
         * Ignore Case of Characters completely. This can be used
         * at any place, but is always valid for the entire matching.
         * @param ignoreCase
         * @return
         */
        public Builder ignoreCase(boolean ignoreCase)
        {
            _appendRRE(".ignoreCase(");
            _appendRRE(ignoreCase);
            _appendRRE(")");
            if(ignoreCase)
            {
                flags = flags | Pattern.CASE_INSENSITIVE;
            }
            else
            {
                flags = flags & ~Pattern.CASE_INSENSITIVE;
            }
            return this;
        }
        
        /**
         * Set the Pattern to do singleLine matching.
         * This can be used
         * at any place, but is always valid for the entire matching.
         * @return
         */
        public Builder singleLine()
        {
            _appendRRE(".singleLine(");
            _appendRRE(")");
            flags = flags | Pattern.DOTALL;
            flags = flags & ~Pattern.MULTILINE;
            return this;
        }
        
        /**
         * add the regex as it is. 
         * @param plainRegEx the Expression like it would be used in Pattern.compile()
         */
        public Builder addRegEx(String plainRegEx)
        {
            addRRE(".addRegEx(", plainRegEx);
            _add(plainRegEx);
            return this;
        }
        
        /**
         * Add some text to the Expression.
         * Non word characters will be escaped with double slashes.
         * @param text
         * @return
         */
        public Builder add(String text)
        {
            addRRE(".add(", text);
            text = text.replaceAll("\\W", "\\\\$0");
            _add(text);
            return this;
        }
        
        /**
         * Convinient Method for a character. Same as
         * add(String.valueOf(text))
         * @param text
         * @return
         */
        public Builder add(char text)
        {
            return add(String.valueOf(text));
        }
        
        /**
         * helper method.
         * @param command
         * @param text
         */
        private void addRRE(String command, String text)
        {
            boolean whiteSpace = text.startsWith(" ")
                || text.endsWith(" ");
            _appendRRE(command);
            if(whiteSpace)
            {
                _appendRRE('\'');
            }
            _appendRRE(text);
            if(whiteSpace)
            {
                _appendRRE('\'');
            }
            _appendRRE(")");
        }
        
        /**
         * helper method to make other methods simpler and more readable.
         * @param text
         */
        private void _appendRRE(String text)
        {
            readableRegex.append(text);
        }
        
        /**
         * helper method to make other methods simpler and more readable.
         * @param text
         */
        private void _appendRRE(char c)
        {
            readableRegex.append(c);
        }
        
        /**
         * helper method to make other methods simpler and more readable.
         * @param text
         */
        private void _appendRRE(int c)
        {
            readableRegex.append(c);
        }
        
        /**
         * helper method to make other methods simpler and more readable.
         * @param text
         */
        private void _appendRRE(boolean c)
        {
            readableRegex.append(c);
        }
        
        /**
         * helper method to make other methods simpler and more readable.
         * @param text
         */
        private void _add(String plainRegEx)
        {
            regex.append(plainRegEx);
        }
        
        /**
         * helper method to make other methods simpler and more readable.
         * @param text
         */
        private void _add(int index, String plainRegEx)
        {
            regex.insert(index, plainRegEx);
        }
        
        /**
         * helper method to make other methods simpler and more readable.
         * @param text
         */
        private void _add(char plainRegEx)
        {
            regex.append(plainRegEx);
        }
        
        /**
         * add a digit. Is like [0-9]
         */
        public Builder digit()
        {
            _appendRRE(".digit()");
            _add("\\d");
            return this;
        }
        
        /**
         * Match a Tab Character
         */
        public Builder tab()
        {
            _appendRRE(".tab()");
            _add("\\t");
            return this;
        }
        
        /**
         * Match a Linebreak Character. This is OS independent and matches \r, \r\n and \n
         */
        public Builder lineBreak()
        {
            _appendRRE(".lineBreak()");
            _add("(?:\\r|\\r\\n|\\n)");
            return this;
        }
        
        /**
         * add not a digit. Just like [^0-9]
         */
        public Builder notDigit()
        {
            _appendRRE(".notDigit()");
            _add("\\D");
            return this;
        }
        
        /**
         * Matches 0-n Elements of previous group.
         * This is like *
         */
        public Builder zeroOrMore()
        {
            _appendRRE(".zeroOrMore()");
            _add("*");
            return this;
        }

        /**
         * Matches as less as possible.
         * This is like *?
         */
        public Builder zeroOrMoreShortest()
        {
            _appendRRE(".zeroOrMoreShortest()");
            _add("*?");
            return this;
        }

        /**
         * Reduces the previous quantifier to match the shortest possible match.
         * Is invalid after methods ending with ...Shortest().
         */
        public Builder lazy()
        {
            _appendRRE(".lazy()");
            _add("?");
            return this;
        }

        /**
         * Matches 0 or 1 Elements of previous group.
         * This is like ?
         */
        public Builder zeroOrOne()
        {
            _appendRRE(".zeroOrOne()");
            _add("?");
            return this;
        }
        
        /**
         * Matches a range of characters.
         * <BR>for example range('a','f','0','9) will result in [a-f0-9] and matches hex values.
         * @param chars must be a multiple of 2 elements.
         * @return
         */
        public Builder range(char ... chars)
        {
            if(chars.length % 2 != 0)
            {
                throw new IllegalArgumentException("Parameter count must always be a multiple of 2");
            }
            _appendRRE(".range(");
            _add('[');
            for(int i=0;i<chars.length;i+=2)
            {
                if(i>0)
                {
                    _appendRRE(",");  
                }
                _appendRRE(chars[i]);
                _appendRRE(",");
                _appendRRE(chars[i+1]);
                _add(chars[i]);
                _add("-");
                _add(chars[i+1]);
            }
            _appendRRE(")");
            _add(']');
            return this;
        }
        
        /**
         * Matches 1-n Elements of previous group.
         * This is like +
         */
        public Builder oneOrMore()
        {
            _appendRRE(".oneOrMore()");
            _add("+");
            return this;
        }
        
        /**
         * Matches 1-n Elements of previous group, but as less as possible.
         * This is like *?
         */
        public Builder oneOrMoreShortest()
        {
            _appendRRE(".oneOrMoreShortest()");
            _add("+?");
            return this;
        }
        
        /**
         * Matches the beginning of the Input.
         * May be placed at any position, but will always mean the start of the expression.
         */
        public Builder startOfLine()
        {
            groupCount++;
            readableRegex.insert(0, ".startOfLine()");
            _add(0, "^");
            return this;
        }
        
        /**
         * Matches the end of the Input.
         * Can only be used at the end of the expression.
         */
        public Builder endOfLine()
        {
            groupCount++;
            _appendRRE(".endOfLine()");
            _add("$");
            return this;
        }
        
        /**
         * Start of a capturing group. Use group() for non capturing groups.
         */
        public Builder capture()
        {
            groupCount++;
            _appendRRE(".capture()");
            _add("(");
            return this;
        }
        
        /**
         * Start of a non capturing group. Use capture() for capturing groups.
         */
        public Builder group()
        {
            groupCount++;
            _appendRRE(".group()");
            _add("(");
            return this;
        }
        
        /**
         * End of an capturing group
         * @return
         */
        public Builder captureEnd()
        {
            if(groupCount <= 0)
            {
                throw new IllegalStateException("captureEnd without capture");
            }
            groupCount--;
            _appendRRE(".captureEnd()");
            _add(")");
            return this;
        }
        
        /**
         * End of an non capturing group
         * @return
         */
        public Builder groupEnd()
        {
            if(groupCount <= 0)
            {
                throw new IllegalStateException("groupEnd without group");
            }
            groupCount--;
            _appendRRE(".groupEnd()");
            _add(")");
            return this;
        }
        
        /**
         * add a word character. Is like [a-zA-Z0-9_]
         */
        public Builder alpha()
        {
            _appendRRE(".alpha()");
            _add("\\w");
            return this;
        }
        
        /**
         * add a non word character. 
         */
        public Builder notAlpha()
        {
            _appendRRE(".notAlpha()");
            _add("\\W");
            return this;
        }
        
        /**
         * any character. Is like .
         * @param plainRegEx
         */
        public Builder anyCharacter()
        {
            _appendRRE(".anyCharacter()");
            _add(".");
            return this;
        }
        
        /**
         * Match one of the characters given in param.
         * oneOf('a','f') is like [af]
         * @param param
         * @return
         */
        public Builder oneOf(char ... param)
        {
            _add("[");
            _appendRRE(".oneOf(");
            for(int i=0;i<param.length;i++)
            {
                if(i>0)
                {
                    _appendRRE(","); 
                }
                _appendRRE("\"");
                _add(param[i]);
                _appendRRE(param[i]);
                _appendRRE("\"");
            }
            _add("]");
            _appendRRE(")");
            return this;
        }

        /**
         * Match one of the characters given in param.
         * oneOf('a','f','\w') is like [af\w]
         * @param param
         * @return
         */
        private Builder _oneOf(String ... param)
        {
            _add("[");
            _appendRRE(".oneOf(");
            for(int i=0;i<param.length;i++)
            {
                if(i>0)
                {
                    _appendRRE(","); 
                }
                _appendRRE("\"");
                _add(param[i]);
                _appendRRE(param[i]);
                _appendRRE("\"");
            }
            _add("]");
            _appendRRE(")");
            return this;
        }

        /**
         * Match one of the Strings given in param.
         * oneOf('abc','fgh','xyz') is like (?:(?:abc)|(?:fgh)|(?:xyz))
         * @param param
         * @return
         */
        public Builder oneOf(String ... param)
        {
            boolean singleChars = true;
            for(int i=0;i<param.length;i++)
            {
                if(param[i].length() > 1
                    && (param[i].length() != 2 || param[i].charAt(0) != '\\'))
                {
                    singleChars = false;
                    break;
                }
            }
            if(singleChars)
            {
                _oneOf(param);
            }
            else
            {
                _add("(?:");
                _appendRRE(".oneOf(");
                for(int i=0;i<param.length;i++)
                {
                    if(i>0)
                    {
                        _appendRRE(",");    
                        _add("|");
                    }
                    if(param[i].length() > 1)
                    {
                        _add("(?:");
                        _add(param[i]);
                        _add(")");
                    }
                    else
                    {
                        _add(param[i]);
                    }
                    _appendRRE(param[i]);
                }
                _add(")");
                _appendRRE(")");
            }
            return this;
        }

        /**
         * get next token from text representation
         * @return
         */
        private String nextToken()
        {
            String token = null;
            int index = remaining.indexOf(").");  
            if(index > 0)
            {
                token = remaining.substring(0, index+1).trim();
                remaining.delete(0, index+2);
            }
            else if(remaining.length() > 0)
            {
                token = remaining.toString().trim();
                remaining.setLength(0);
            }
            return token;
        }

        /**
         * Create the ReadablePattern of this Builder.
         * @return
         */
        public ReadablePattern build()
        {
            ReadablePattern rpat = new ReadablePattern();
            rpat.pattern = Pattern.compile(regex.toString(), flags);
            rpat.readableRegex = readableRegex.toString();
            return rpat;
        }
        
        /**
         * Add the Contents of the given Builder to this Builder.
         * @param builder
         * @return
         */
        public Builder add(Builder builder)
        {
            addRegEx("(?:" + builder.regex.toString() + ")");
            return this;
        }
        
        /**
         * Create a new Builder in the fluent call.
         * @return
         */
        public Builder builder()
        {
            return new Builder();
        }
    }
    
    /**
     * Create a ReadablePattern from Text representation.
     * @param readableRegex Can contain all the public method calls that are valid on
     * the Builder Object, except the builder() method. To make Expressions even easier, you 
     * won't need to quote parameters in most cases. So add(ABC) would be the same as add('ABC') or 
     * add("ABC").
     * @return
     */
    public static ReadablePattern compile(String readableRegex)
    {
        Builder builder = new Builder(readableRegex);
        return builder.build();
    }

    /**
     * Return a Matcher for this ReadablePattern.
     * @param text
     * @return
     */
    public Matcher matcher(String text)
    {
        return pattern.matcher(text);
    }

    /**
     * Shortcut for matcher(text).matches()
     * @param text
     * @return
     */
    public boolean matches(String text)
    {
        return matcher(text).matches();
    }
    
    /**
     * Shortcut for matcher(text).find()
     * @param text
     * @return
     */
    public boolean find(String text)
    {
        return matcher(text).find();
    }
    
    @Override
    public String toString()
    {
        return "ReadablePattern: " + readableRegex + " ; compiled:" + pattern.pattern();
    }
    
    public static void main(String[] args)
    {
        String testString = "aaa XXXbd0\t\r\n";
        //Parse from Text
        ReadablePattern pat = ReadablePattern.compile("startOfLine()"
            + ".add(a)" //dont need quotation if there is no space at beginning or end.
            + "   \r\n   .add('a')"
            + ".add(\"a\")"
            + ".add(' ')"
            + ".add('X')"
            + ".oneOrMore()"
            + ".capture()"
            + ".oneOf(a,b,c,'d')"
            + ".count(2)"
            + ".captureEnd()"
            + ".range(a,f,A,F,0,9)" //Hex Chars
            + ".digit()"
            + ".zeroOrMore()"
            + ".addRegEx((?:ABC[abc]))"
            + ".zeroOrMore()"
            + ".tab()"
            + ".lineBreak()"
            + ".endOfLine()");
        Matcher matcher = pat.matcher(testString);
        System.out.println("matches: " + matcher.matches() + "; " + pat);
        if(matcher.matches())
        {
            System.out.println("Result of Group 1: " + matcher.group(1));
        }

        //Same as above, but done directly in Code
        Builder builder = new Builder();
        pat = builder.startOfLine()
            .add('a') //Can't add without quotation marks
            .add('a')
            .add("a")
            .add(' ')
            .add('X')
            .oneOrMore()
            .capture()
            .oneOf('a','b','c','d')
            .count(2)
            .captureEnd()
            .range('a', 'f', 'A', 'F', '0', '9') //Hex Chars
            .digit()
            .zeroOrMore()
            .add(new Builder().add("ABC").oneOf('a','b','c'))
            .zeroOrMore()
            .tab()
            .lineBreak()
            .endOfLine()
            .build();
        matcher = pat.matcher(testString);
        System.out.println("matches: " + matcher.matches() + "; " + pat);
        if(matcher.matches())
        {
            System.out.println("Result of Group 1: " + matcher.group(1));
        }

        ReadablePattern pattern = new Builder()
            .oneOf('a','b','c')
            .oneOrMore()
            .add("xyz")
            .zeroOrMore()
            .build();
        System.out.println(pattern.matches("bxyz"));
        System.out.println(pattern);
        
    }
}
