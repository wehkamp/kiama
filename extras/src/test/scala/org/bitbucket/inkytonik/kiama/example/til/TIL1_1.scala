/*
 * This file is part of Kiama.
 *
 * Copyright (C) 2008-2021 Anthony M Sloane, Macquarie University.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.bitbucket.inkytonik.kiama
package example.til

import org.bitbucket.inkytonik.kiama.parsing.ListParsers
import org.bitbucket.inkytonik.kiama.util.Positions

/**
 * AST the basic Tiny Imperative Language.
 */
object TILTree {

    abstract class TilNode

    case class Program(ss : List[Stat]) extends TilNode

    sealed abstract class Stat extends TilNode

    case class Decl(i : Id) extends Stat

    case class Assign(i : Id, e : Exp) extends Stat

    case class IfThen(e : Exp, t : List[Stat]) extends Stat
    case class IfElse(e : Exp, t : List[Stat], f : List[Stat]) extends Stat

    case class While(e : Exp, b : List[Stat]) extends Stat
    case class For(i : Id, f : Exp, t : Exp, b : List[Stat]) extends Stat

    case class Read(i : Id) extends Stat
    case class Write(e : Exp) extends Stat

    sealed abstract class Exp extends TilNode

    case class Id(s : String) extends TilNode

    case class Var(i : Id) extends Exp
    case class Num(n : Int) extends Exp
    case class Str(s : String) extends Exp

    case class Mul(l : Exp, r : Exp) extends Exp
    case class Div(l : Exp, r : Exp) extends Exp
    case class Add(l : Exp, r : Exp) extends Exp
    case class Sub(l : Exp, r : Exp) extends Exp

    case class Eq(l : Exp, r : Exp) extends Exp
    case class Ne(l : Exp, r : Exp) extends Exp

}

/**
 * Parser for the basic Tiny Imperative Language.
 */
class TIL1_1Parsers(positions : Positions) extends ListParsers(positions) {

    import TILTree._
    import scala.language.postfixOps

    lazy val program = (statement*) ^^ Program

    lazy val statement : Parser[Stat] =
        declaration | assignment_statement | if_statement | while_statement |
            for_statement | read_statement | write_statement

    lazy val declaration = "var" ~> identifier <~ ";" ^^ Decl

    lazy val assignment_statement =
        identifier ~ (":=" ~> expression <~ ";") ^^ Assign

    lazy val if_statement =
        ("if" ~> expression) ~ ("then" ~> (statement*)) ~ ("else" ~> (statement*) <~ "end") ^^ IfElse |
            "if" ~> expression ~ ("then" ~> (statement*) <~ "end") ^^ IfThen

    lazy val while_statement =
        ("while" ~> expression <~ "do") ~ (statement*) <~ "end" ^^ While

    lazy val for_statement =
        ("for" ~> identifier) ~ (":=" ~> expression) ~ ("to" ~> expression) ~ ("do" ~> (statement*) <~ "end") ^^ For

    lazy val read_statement = "read" ~> identifier <~ ";" ^^ Read

    lazy val write_statement = "write" ~> expression <~ ";" ^^ Write

    lazy val expression : PackratParser[Exp] =
        expression ~ ("=" ~> term) ^^ Eq |
            expression ~ ("!=" ~> term) ^^ Ne |
            term

    lazy val term : PackratParser[Exp] =
        term ~ ("+" ~> factor) ^^ Add |
            term ~ ("-" ~> factor) ^^ Sub |
            factor

    lazy val factor : PackratParser[Exp] =
        factor ~ ("*" ~> primary) ^^ Mul |
            factor ~ ("/" ~> primary) ^^ Div |
            primary

    lazy val primary =
        identifier ^^ Var |
            integer |
            string |
            "(" ~> expression <~ ")"

    lazy val keyword =
        keywords(
            "[^a-zA-Z0-9]".r,
            List("var", "if", "then", "else", "while", "do",
                "for", "read", "write")
        )

    lazy val identifier =
        not(keyword) ~> "[a-zA-Z][a-zA-Z0-9]*".r ^^ Id

    lazy val integer =
        "[0-9]+".r ^^ (s => Num(s.toInt))

    lazy val string =
        """\"[^\"]+\"""".r ^^ Str

    override val whitespace : Parser[String] =
        """(\s|(//.*\n))*""".r

}

class TIL1_1 extends ParsingMain {

    import org.bitbucket.inkytonik.kiama.parsing.ParseResult
    import org.bitbucket.inkytonik.kiama.util.Source

    def parse(source : Source) : ParseResult[TILTree.Program] = {
        val parsers = new TIL1_1Parsers(positions)
        parsers.parseAll(parsers.program, source)
    }

}

object TIL1_1Main extends TIL1_1
