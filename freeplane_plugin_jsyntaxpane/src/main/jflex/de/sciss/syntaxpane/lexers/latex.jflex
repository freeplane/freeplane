/*
 * Copyright 2008 Ayman Al-Sairafi ayman.alsairafi@gmail.com,
 * 2012 Felix Natter
 * 
 * Some very basic ideas taken from here:
 * http://dev.geogebra.org/trac/browser/trunk/geogebra/desktop/geogebra/gui/editor/latex.jflex?rev=13586 and
 * the list of keywords is from gtksourceview's latex.lang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License
 *       at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.sciss.syntaxpane.lexers;


import de.sciss.syntaxpane.Token;
import de.sciss.syntaxpane.TokenType;

%%

%public
%class LaTeXLexer
%extends DefaultJFlexLexer
%final
%unicode
%char
%type Token


%{
    /**
     * Default constructor is needed as we will always call the yyreset
     */
    public LaTeXLexer() {
        super();
    }

    @Override
    public int yychar() {
        return yychar;
    }

    private static final byte PAREN     = 1;
    private static final byte BRACKET   = 2;
    private static final byte CURLY     = 3;

%}

/* main character classes */

eol = [\r\n]

sub = "_"
sup = "^"
amp = "&"
dollar = "$"


number = [0-9]+

default = [^\[{\]}_\^&\$\\\r\n \t0-9]+

comments = "%".*{eol}

%state MATHMODE1
%state MATHMODE2
%state MATHMODE3

mathMode1Begin = "$"
mathMode1End = "$"
mathMode2Begin = "\\["
mathMode2End = "\\]"
mathMode3Begin = "$$"
mathMode3End = "$$"
command = "\\"[^\[][a-zA-Z]*

keywords = "\\Alpha"         |
  "\\Beta"                   |  
  "\\Chi"                    |
  "\\Delta"                  |
  "\\Epsilon"                |
  "\\Eta"                    |
  "\\Gamma"                  |
  "\\Iota"                   |
  "\\Kappa"                  |
  "\\Lambda"                 |
  "\\Leftarrow"              |
  "\\Leftrightarrow"         |
  "\\Mu"                     |
  "\\Nu"                     |
  "\\Omega"                  |
  "\\Phi"                    |
  "\\Pi"                     |
  "\\Psi"                    |
  "\\Rho"                    |
  "\\Rightarrow"             |
  "\\Sigma"                  |
  "\\Tau"                    |
  "\\Zeta"                   |
  "\\alpha"                  |
  "\\appendix"               |
  "\\begin"                  |
  "\\beta"                   |
  "\\bigcap"                 |
  "\\bigcup"                 |
  "\\cap"                    |
  "\\cdot"                   |
  "\\chapter"                |
  "\\chi"                    |
  "\\cite"                   |
  "\\cup"                    |
  "\\delta"                  |
  "\\documentclass"          |
  "\\end"                    |
  "\\enumi"                  |
  "\\enumii"                 |
  "\\enumiii"                |
  "\\enumiv"                 |
  "\\epsilon"                |
  "\\equation"               |
  "\\eta"                    |
  "\\exists"                 |
  "\\figure"                 |
  "\\footnote"               |
  "\\footnotemark"           |
  "\\footnotetext"           |
  "\\forall"                 |
  "\\gamma"                  |
  "\\geq"                    |
  "\\in"                     |
  "\\int"                    |
  "\\iota"                   |
  "\\kappa"                  |
  "\\label"                  |
  "\\lambda"                 |
  "\\ldots"                  |
  "\\leftarrow"              |
  "\\leq"                    |
  "\\mpfootnote"             |
  "\\mu"                     |
  "\\neq"                    |
  "\\newcommand"             |
  "\\newenvironment"         |
  "\\newfont"                |
  "\\newtheorem"             |
  "\\not"                    |
  "\\notin"                  |
  "\\nu"                     |
  "\\omega"                  |
  "\\onecolumn"              |
  "\\page"                   |
  "\\pageref"                |
  "\\paragraph"              |
  "\\part"                   |
  "\\phi"                    |
  "\\pi"                     |
  "\\prod"                   |
  "\\psi"                    |
  "\\qquad"                  |
  "\\quad"                   |
  "\\ref"                    |
  "\\rho"                    |
  "\\rightarrow"             |
  "\\section"                |
  "\\setminus"               |
  "\\sigma"                  |
  "\\subparagraph"           |
  "\\subsection"             |
  "\\subset"                 |
  "\\subseteq"               |
  "\\subsetneq"              |
  "\\subsubsection"          |
  "\\subsubsubsection"       |
  "\\sum"                    |
  "\\supset"                 |
  "\\supseteq"               |
  "\\supsetneq"              |
  "\\table"                  |
  "\\tau"                    |
  "\\times"                  |
  "\\twocolumn"              |
  "\\varepsilon"             |
  "\\varphi"                 |
  "\\zeta"                   |
  "\\\\"

%%

<MATHMODE1> {
  {mathMode1End}                 { yybegin(YYINITIAL);
                                   return token(TokenType.OPERATOR);
                                 }

  {number}                       {
                                   return token(TokenType.STRING);
                                 }
  {default}                      {
                                   return token(TokenType.TYPE2);
                                 }

  {keywords}                     { return token(TokenType.KEYWORD2); }

  {command}                      {
                                   return token(TokenType.TYPE);
                                 }
  /* operators */
  "("                            { return token(TokenType.TYPE3,  PAREN); }
  ")"                            { return token(TokenType.TYPE3, -PAREN); }
  "{"                            { return token(TokenType.TYPE3,  CURLY); }
  "}"                            { return token(TokenType.TYPE3, -CURLY); }
  "["                            { return token(TokenType.TYPE3,  BRACKET); }
  "]"                            { return token(TokenType.TYPE3, -BRACKET); }
}

<MATHMODE2> {
  {mathMode2End}                 { yybegin(YYINITIAL);
                                   return token(TokenType.OPERATOR);
                                 }

  {number}                       {
                                   return token(TokenType.STRING);
                                 }
  {default}                      {
                                   return token(TokenType.TYPE2);
                                 }

  {keywords}                     { return token(TokenType.KEYWORD2); }

  {command}                      {
                                   return token(TokenType.TYPE);
                                 }
  /* operators */
  "("                            { return token(TokenType.TYPE3,  PAREN); }
  ")"                            { return token(TokenType.TYPE3, -PAREN); }
  "{"                            { return token(TokenType.TYPE3,  CURLY); }
  "}"                            { return token(TokenType.TYPE3, -CURLY); }
  "["                            { return token(TokenType.TYPE3,  BRACKET); }
  "]"                            { return token(TokenType.TYPE3, -BRACKET); }
}

<MATHMODE3> {
  {mathMode3End}                 { yybegin(YYINITIAL);
                                   return token(TokenType.OPERATOR);
                                 }

  {number}                       {
                                   return token(TokenType.STRING);
                                 }
  {default}                      {
                                   return token(TokenType.TYPE2);
                                 }

  {keywords}                     { return token(TokenType.KEYWORD2); }

  {command}                      {
                                   return token(TokenType.TYPE);
                                 }
  /* operators */
  "("                            { return token(TokenType.TYPE3,  PAREN); }
  ")"                            { return token(TokenType.TYPE3, -PAREN); }
  "{"                            { return token(TokenType.TYPE3,  CURLY); }
  "}"                            { return token(TokenType.TYPE3, -CURLY); }
  "["                            { return token(TokenType.TYPE3,  BRACKET); }
  "]"                            { return token(TokenType.TYPE3, -BRACKET); }
}


<YYINITIAL> {

  {keywords}                     { return token(TokenType.KEYWORD); }


  {default}                      {
                                   return token(TokenType.DEFAULT);
                                 }

  {amp}                          {
                                   return token(TokenType.OPERATOR);
                                 }
  /* operators */
  "("                            { return token(TokenType.OPERATOR,  PAREN); }
  ")"                            { return token(TokenType.OPERATOR, -PAREN); }
  "{"                            { return token(TokenType.OPERATOR,  CURLY); }
  "}"                            { return token(TokenType.OPERATOR, -CURLY); }
  "["                            { return token(TokenType.OPERATOR,  BRACKET); }
  "]"                            { return token(TokenType.OPERATOR, -BRACKET); }


  {number}                       {
                                   return token(TokenType.NUMBER);
                                 }

  {sub}                          |
  {sup}                          {
                                   return token(TokenType.OPERATOR);
                                 }


  {comments}                     {
                                   return token(TokenType.COMMENT);
                                 }

  " "                            {
                                   return token(TokenType.DEFAULT);
                                 }

  "\t"                           {
                                   return token(TokenType.DEFAULT);
                                 }

  {mathMode1Begin}               {
                                   yybegin(MATHMODE1);
                                   return token(TokenType.OPERATOR);
                                 }
  {mathMode2Begin}               {
                                   yybegin(MATHMODE2);
                                   return token(TokenType.OPERATOR);
                                 }
  {mathMode3Begin}               {
                                   yybegin(MATHMODE3);
                                   return token(TokenType.OPERATOR);
                                 }

  {command}                      {
                                   return token(TokenType.IDENTIFIER);
                                 }


  .                              |
  {eol}                          {
                                   return token(TokenType.DEFAULT);
                                 }
}


/* error fallback */
.|\n                             {  }
<<EOF>>                          { return null; }
