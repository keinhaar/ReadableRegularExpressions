# ReadableRegularExpressions
This Tools provides readable regular Expressions for Java

Regular Expressions are really cool stuff, if you understand it's syntax. It's cool while you are writing the expressions and finally see it's working. Still cool if you see simple regular expressions. But if you read a regex that's not trivial, can you immediatly tell, what going on in it? Even if you wrote it yourself some time ago, that will be difficult an error prone.

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

There are lots of more complex scenarios.
