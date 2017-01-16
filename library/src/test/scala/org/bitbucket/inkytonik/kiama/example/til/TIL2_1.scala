/*
 * This file is part of Kiama.
 *
 * Copyright (C) 2008-2017 Anthony M Sloane, Macquarie University.
 *
 * Kiama is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Kiama is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Kiama.  (See files COPYING and COPYING.LESSER.)  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package org.bitbucket.inkytonik.kiama
package example.til

/**
 * Rewrite TILs for loops that automatically declare the control variable
 * adding an explicit declaration of the variable.
 */
class TIL2_1 extends TransformingMain {

    import TILTree._
    import org.bitbucket.inkytonik.kiama.rewriting.Rewriter._

    val parsers = new TIL1_1Parsers(positions)
    val parser = parsers.program

    def transform(ast : Program) : Program =
        rewrite(declareforvars)(ast)

    val declareforvars =
        everywherebu(rule[List[Stat]] {
            case (s @ For(Id(i), f, t, b)) :: ss =>
                Decl(Id(i)) :: s :: ss
        })

}

object TIL2_1Main extends TIL2_1