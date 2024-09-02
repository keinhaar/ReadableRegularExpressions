# ReadableRegularExpressions (RRE) #
This Tools provides readable regular Expressions (RRE) for Java

Regular Expressions are really cool stuff, if you understand it's syntax. It's cool while you are writing the expressions and finally see it's working. Still cool if you see simple regular expressions. But if you read a regex that's not trivial, can you immediatly tell, what's going on in it? Even if you wrote it yourself some time ago, that will be difficult an error prone.

Simple Example:
<pre>
    ReadablePattern pattern = new Builder()
        .oneOf('a','b','c')
        .oneOrMore()
        .add("xyz")
        .zeroOrMore()
        .build();
    System.out.println(pattern.matches("bxyz"));
</pre> 
writes true, and results in the Pattern "[abc]+xyz*".

ReadablePatterns can also be parsed from text. This is mainly to allow End Users to write simple rules by themself, like it is done in <a href="http://www.exware.de/scandor/en/index.html">scandor</a> (a simple scan and forget tool, which automatically saves files by using rules defined with RRE.

Here's an example for that:
<pre>
    ReadablePattern patttern = ReadablePattern.compile("add('xyz')");
</pre>
The Pattern will be parsed to "xyz" and would match the text "xyz"

The parsed text can contain any public method call that is in Builder, except the builder() and build() methods. If no special characters are included, the quoting is not nessessary. The example above could be written as "add(xyz)".

You could even mix normal text with method calls. That makes it easy to use for non programmers.
(Plain Text needs to prefixed with a single dot, except at the beginning of the Pattern)

Here's an example for that:
<pre>
    ReadablePattern patttern = ReadablePattern.compile("myexample.add(xyz).extra content");
</pre>

The Pattern will be parsed to "(?:myexample)xyz(?:extra\ content)" and would match the text "myexamplexyzextra content"

There are lots of more complex scenarios.

## Supported Functions ##
Functionname | Description | RegEx
---- | ---- | ----
add('ABC')   | Adds some Plain Text to the RegEx | ABC
addGroup('ABC') | Adds a non capturing group | (?:ABC)
addRegEx('\d+') | Adds a normal RegEx to the Readable RegEx | \d+
alpha() | match an alpha character like a-z A-Z 0-9 and _ | \w
anyCharacter() | matches any character | .
capture() | start of a capturing group. must end with captureEnd(). | 
captureEnd() | end of a capturing group. must be started with capture(). | 
count(4) | how often must the previous element occur. Here 4 times. If two numbers are given, then they are min and max number of occurences count(2,4) | {4}
date() | matches a date in one of the formats dd.MM.yyyy, dd/MM/yyyy or yyyy-MM-dd 'dd. MMM. yyyy' if it is surrounded by whitespace | (?:(?:(?<=\s)\d{4}\-[01]{0,1}[0-9]-[0-3]{0,1}[0-9](?=\s))\|...
digit() | match a single digit | \d
dot() | match a single dot | \.
endOfLine() | matches the end of line | $
ignoreCase() | ignore the case of Characters | (?i)
lazy() | match as short as possible | ?
lineBreak() | match a linebreak | (?:\r|\r\n|\n)
notAlpha() | match something that is NOT a alpha | \W
notDigit() | match something that is NOT a digit | \D
oneOf('aa','bb','cc') | One of the Elements should b matched | (?:(?:aa)|(?:bb)|(?:cc))
oneOrMore() | The previous element must occur at least 1 time or unlimited times | +
oneOrMoreShortest() | The previous element must occur at least 1 time or unlimited times, but it should be matched as short as possible | +?
range(a,f) | matches a range of characters. in this example a,b,c,d,e,f | [a-f]
startOfLine() | matches the beginning of a line | ^
tab() | match a single Tabulator character | \t
whitespace() | match whitespace characters like ' ', tab and some others | \s
zeroOrMore() | The previous element can be absend or occurs unlimited times | *
zeroOrMoreShortest() | The previous element can be absend or occurs unlimited times, but it should be matched as short as possible | *?
zeroOrOne() | The previous element can be absend or occurs exactly 1 time | ?

## Translation ##
The Function Names can be Translated to other Languages. To do that, just add a Properties Object to the Builder.
In the example below, you can use 'datum()' as an alternative name for 'date()'
<pre>
    Properties properties = new Properties();
    properties.setProperty("datum", "date");
    properties.setProperty("zeilenanfang", "startOfLine");
    ReadablePattern.Builder.addLanguage(properties);
</pre>

`var specificLanguage_code = 
    {
        "data": {
            "lookedUpPlatform": 1,
            "query": "Kasabian+Test+Transmission",
            "lookedUpItem": {
                "name": "Test Transmission",
                "artist": "Kasabian",
                "album": "Kasabian",
                "picture": null,
                "link": "http://open.spotify.com/track/5jhJur5n4fasblLSCOcrTp"
            }
        }
    }`
## Extensions ##
For special applications you may want some more abbreviations for your users. For this case an simple extension mechanism has been added. Each Extension consists of a name returned by getFunctionName(), and a method that builds the regular expression.
<pre>
    List<ReadablePatternExtension> extensions = new ArrayList<>();
    extensions.add(new ReadablePatternExtension()
    {
        @Override
        public String getFunctionName()
        {
            return "hello";
        }
        @Override
        public void createRegEx(Builder builder)
        {
            builder.add("hello");
        }
    });
    ReadablePattern pat2 = ReadablePattern.compile(extensions, "hello()", false);
</pre>
