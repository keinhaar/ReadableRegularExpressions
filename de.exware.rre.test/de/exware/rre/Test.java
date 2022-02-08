package de.exware.rre;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;

import de.exware.rre.ReadablePattern.Builder;
import de.exware.rre.ReadablePattern.ReadablePatternExtension;

public class Test 
{
    @org.junit.Test
    public void testDate()
    {
        ReadablePattern pat = ReadablePattern.compile("date()", false);
        System.out.println(pat.toString());
        Matcher matcher = pat.matcher("Hallo 31.12.2020 \r\nABC");
        assertTrue(matcher.find());
        System.out.println(matcher.group());
        assertFalse(matcher.matches());
        matcher = pat.matcher("Hallo 2020-12-31 \r\nABC");
        assertTrue(matcher.find());
        System.out.println(matcher.group());
        assertFalse(matcher.matches());
        matcher = pat.matcher("Hallo 31/12/2020 \r\nABC");
        assertTrue(matcher.find());
        System.out.println(matcher.group());
        assertFalse(matcher.matches());
    }

    @org.junit.Test
    public void testOneOf()
    {
        ReadablePattern pat = ReadablePattern.compile("oneOf('aa','bb','cc')", false);
        System.out.println(pat.toString());
        Matcher matcher = pat.matcher("XXX bb YYY");
        assertTrue(matcher.find());
        System.out.println(matcher.group());
        assertFalse(matcher.matches());
    }

    @org.junit.Test
    public void testLanguage() throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty("datum", "date");
//        properties.load(ReadablePattern.class.getResourceAsStream("german.properties"));
        ReadablePattern.Builder.addLanguage(properties);
        ReadablePattern pat = ReadablePattern.compile("datum()");
        System.out.println(pat.toString());
        Matcher matcher = pat.matcher("Hallo 31.12.2020 \r\nABC");
        assertTrue(matcher.find());
        System.out.println(matcher.group());
        assertFalse(matcher.matches());
        matcher = pat.matcher("Hallo 2020-12-31 \r\nABC");
        assertTrue(matcher.find());
        System.out.println(matcher.group());
        assertFalse(matcher.matches());
        matcher = pat.matcher("Hallo 31/12/2020 \r\nABC");
        assertTrue(matcher.find());
        System.out.println(matcher.group());
        assertFalse(matcher.matches());
    }

    @org.junit.Test
    public void testExtension()
    {
        List<ReadablePatternExtension> extensions = new ArrayList<>();
        extensions.add(new ReadablePatternExtension()
        {
            @Override
            public String getFunctionName()
            {
                return "hello";
            }
            
            @Override
            public void createRegEx(Builder builder, String param)
            {
                builder.add("hello");
            }
        });
        ReadablePattern pat2 = ReadablePattern.compile(extensions, "hello()", false);
        Matcher matcher = pat2.matcher("HUHU");
        assertFalse(matcher.matches());
        matcher = pat2.matcher("hello");
        assertTrue(matcher.matches());
        matcher = pat2.matcher("ABC hello DEF");
        assertFalse(matcher.matches());
    }
}
