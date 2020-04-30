# ReadableRegularExpressions (RRE)
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

Here's an example for that:
<pre>
    ReadablePattern patttern = ReadablePattern.compile("myexample.add(xyz)extra content");
</pre>

The Pattern will be parsed to "(?:myexample)xyz(?:extra\ content)" and would match the text "myexamplexyzextra content"

There are lots of more complex scenarios.
