/**
 * Copyright (C) 2013-2016 The Rythm Engine project
 * for LICENSE and other details see:
 * https://github.com/rythmengine/rythmengine
 */
package org.rythmengine.internal.parser.build_in;

import com.stevesoft.pat.Regex;
import org.rythmengine.internal.IContext;
import org.rythmengine.internal.IParser;
import org.rythmengine.internal.Keyword;
import org.rythmengine.internal.Token;
import org.rythmengine.internal.parser.CodeToken;
import org.rythmengine.internal.parser.ParserBase;
import org.rythmengine.utils.S;

public class IncludeParser extends KeywordParserFactory {

    private static final String R = "(^\\n?[ \\t\\x0B\\f]*%s(%s\\s*((?@()))\\s*))";

    public IncludeParser() {
    }

    protected String patternStr() {
        return R;
    }

    public IParser create(final IContext ctx) {
        return new ParserBase(ctx) {
            public Token go() {
                Regex r = reg(dialect());
                if (!r.search(remain())) {
                    raiseParseException("Error parsing @include statement. Correct usage: @include(\"foo.bar, a.b.c, ...\")");
                }
                final String matched = r.stringMatched();
                if (matched.startsWith("\n")) {
                    ctx.getCodeBuilder().addBuilder(new Token.StringToken("\n", ctx));
                    Regex r0 = new Regex("\\n([ \\t\\x0B\\f]*).*");
                    if (r0.search(matched)) {
                        String blank = r0.stringMatched(1);
                        if (blank.length() > 0) {
                            ctx.getCodeBuilder().addBuilder(new Token.StringToken(blank, ctx));
                        }
                    }
                } else {
                    Regex r0 = new Regex("([ \\t\\x0B\\f]*).*");
                    if (r0.search(matched)) {
                        String blank = r0.stringMatched(1);
                        if (blank.length() > 0) {
                            ctx.getCodeBuilder().addBuilder(new Token.StringToken(blank, ctx));
                        }
                    }
                }
                int lineNo = ctx().currentLine();
                step(matched.length());
                String s = r.stringMatched(3);
                if (S.isEmpty(s)) {
                    raiseParseException("Error parsing @include statement. Correct usage: @include(foo.bar, a.b.c, ...)");
                }
                s = S.stripBraceAndQuotation(s);
                try {
                    String code = ctx().getCodeBuilder().addIncludes(s, lineNo, ctx.peekCodeType());
                    if (matched.endsWith("\n")) {
                        code = code + ";p(\"\\n\");";
                    }
                    return new CodeToken(code, ctx());
                } catch (NoClassDefFoundError e) {
                    raiseParseException("error adding includes: " + e.getMessage() + "\n possible cause: lower/upper case issue on windows platform");
                    return null;
                }
            }
        };
    }

    @Override
    public Keyword keyword() {
        return Keyword.INCLUDE;
    }

}
