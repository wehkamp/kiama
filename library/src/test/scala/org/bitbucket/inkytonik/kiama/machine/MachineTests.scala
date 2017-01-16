/*
 * This file is part of Kiama.
 *
 * Copyright (C) 2011-2017 Anthony M Sloane, Macquarie University.
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
package machine

import org.bitbucket.inkytonik.kiama.util.Tests

/**
 * Basic tests of machine module.  More complex setups are tested
 * within particular examples.
 */
class MachineTests extends Tests {

    import org.bitbucket.inkytonik.kiama.util.{Emitter, StringEmitter}

    /**
     * Make a new machine for testing. The machine contains an empty `main`
     * rule and debugging is turned on. `emitter` is used as the emitter
     * within the machine (default: a new string emitter).
     */
    def makeMachine(emitter : Emitter = new StringEmitter) : Machine =
        new Machine("m", emitter) {
            override def debug = true
            def main {}
        }

    // Scalar state

    test("new state is undefined") {
        val m = makeMachine()
        val s = new m.State[Int]("s")
        s.isUndefined shouldBe true
    }

    test("asking for the value of undefined state gives an error") {
        val m = makeMachine()
        val s = new m.State[Int]("s")
        val i = intercept[RuntimeException] {
            s.value
        }
        i.getMessage shouldBe "State.value: m.s is undefined"
    }

    test("state can be made undefined") {
        val m = makeMachine()
        val s = new m.State[Int]("s")
        m.reset
        s := 42
        m.performUpdates
        !s.isUndefined shouldBe true
        s.undefine
        s.isUndefined shouldBe true
    }

    test("undefined state is not equal to anything") {
        val m = makeMachine()
        val s = new m.State[Int]("s")
        !(s =:= 0) shouldBe true
        !(s =:= 42) shouldBe true
        !(s =:= 99) shouldBe true
    }

    test("defined state is only equal to its value") {
        val m = makeMachine()
        val s = new m.State[Int]("s")
        s := 0
        m.performUpdates
        s =:= 0 shouldBe true
        !(s =:= 42) shouldBe true
        !(s =:= 99) shouldBe true
    }

    test("undefined state toStrings to a special message") {
        val m = makeMachine()
        val s = new m.State[Int]("s")
        s.toString shouldBe "** undefined **"
    }

    test("defined state toStrings to its value") {
        val m = makeMachine()
        val s = new m.State[Int]("s")
        m.reset
        s := 42
        m.performUpdates
        s.toString shouldBe "42"
    }

    test("state updates trigger suitable debug messages") {
        val memitter = new StringEmitter
        val m = makeMachine(memitter)
        val s = new m.State[Int]("s")
        val t = new m.State[Int]("t")
        memitter.clear
        m.reset
        s := 88
        t := 99
        m.performUpdates
        m.reset
        s := 44
        m.performUpdates
        memitter.result shouldBe "m.t := 99\nm.s := 88\nm.s := 44\n"
    }

    test("multiple consistent state updates are allowed") {
        val m = makeMachine()
        val s = new m.State[Int]("s")
        m.reset
        s := 0
        s := 0
        m.performUpdates
        s =:= 0 shouldBe true
        !(s =:= 1) shouldBe true
    }

    test("inconsistent state updates in differents steps are allowed") {
        val m = makeMachine()
        val s = new m.State[Int]("s")
        m.reset
        s := 0
        m.performUpdates
        s =:= 0 shouldBe true
        !(s =:= 1) shouldBe true
        m.reset
        s := 1
        m.performUpdates
        !(s =:= 0) shouldBe true
        s =:= 1 shouldBe true
    }

    test("inconsistent state updates in one step trigger an exception") {
        val m = makeMachine()
        val s = new m.State[Int]("s")
        m.reset
        s := 0
        s := 1
        val i = intercept[InconsistentUpdateException] {
            m.performUpdates
        }
        i.getMessage shouldBe "Machine = m, updates = List(m.s := 1, m.s := 0)"
    }

    // Parameterised state

    test("new parameterised state is undefined") {
        val m = makeMachine()
        val p = new m.ParamState[Int, Int]("p")
        p.isUndefined(0) shouldBe true
        p.isUndefined(42) shouldBe true
        p.isUndefined(99) shouldBe true
    }

    test("asking for the value of undefined parameterised state gives an error") {
        val m = makeMachine()
        val p = new m.ParamState[Int, Int]("p")
        val i = intercept[RuntimeException] {
            p.value(0)
        }
        i.getMessage shouldBe "ParamState.value: m.p is undefined"
    }

    test("asking for the value of parameterised state at an undefined value gives an error") {
        val m = makeMachine()
        val p = new m.ParamState[Int, Int]("p")
        m.reset
        p(0) := 42
        m.performUpdates
        val i = intercept[RuntimeException] {
            p.value(12)
        }
        i.getMessage shouldBe "ParamState.value: m.p(12) is undefined"
    }

    test("parameterised state can be made undefined") {
        val m = makeMachine()
        val p = new m.ParamState[Int, Int]("p")
        m.reset
        p(0) := 42
        m.performUpdates
        !(p.isUndefined(0)) shouldBe true
        p.undefine(0)
        p.isUndefined(0) shouldBe true
    }

    test("undefined parameterised state is not equal to anything") {
        val m = makeMachine()
        val p = new m.ParamState[String, Int]("p")
        !(p("one") =:= 0) shouldBe true
        !(p("one") =:= 42) shouldBe true
        !(p("one") =:= 99) shouldBe true
    }

    test("defined parameterised state is only equal to its value") {
        val m = makeMachine()
        val p = new m.ParamState[String, Int]("p")
        m.reset
        p("one") := 42
        m.performUpdates
        !(p("one") =:= 0) shouldBe true
        p("one") =:= 42 shouldBe true
        !(p("one") =:= 99) shouldBe true
        m.reset
        p("two") := 99
        m.performUpdates
        !(p("one") =:= 0) shouldBe true
        p("one") =:= 42 shouldBe true
        !(p("one") =:= 99) shouldBe true
        !(p("two") =:= 0) shouldBe true
        !(p("two") =:= 42) shouldBe true
        p("two") =:= 99 shouldBe true
    }

    test("parameterised state updates trigger suitable debug messages") {
        val memitter = new StringEmitter
        val m = makeMachine(memitter)
        val p = new m.ParamState[String, Int]("p")
        val q = new m.ParamState[Int, Int]("q")
        memitter.clear
        m.reset
        p("one") := 1
        p("two") := 2
        q(0) := 0
        m.performUpdates
        m.reset
        p("one") := 3
        q(0) := 1
        q(1) := 2
        m.performUpdates
        memitter.result shouldBe
            """m.q(0) := 0
              |m.p(two) := 2
              |m.p(one) := 1
              |m.q(1) := 2
              |m.q(0) := 1
              |m.p(one) := 3
              |""".stripMargin
    }

    test("multiple consistent parameterised state updates are allowed") {
        val m = makeMachine()
        val p = new m.ParamState[String, Int]("p")
        m.reset
        p("one") := 0
        p("one") := 0
        m.performUpdates
        p("one") =:= 0 shouldBe true
        !(p("one") =:= 1) shouldBe true
    }

    test("inconsistent parameterised state updates in differents steps are allowed") {
        val m = makeMachine()
        val p = new m.ParamState[String, Int]("p")
        m.reset
        p("one") := 0
        m.performUpdates
        p("one") =:= 0 shouldBe true
        !(p("one") =:= 1) shouldBe true
        m.reset
        p("one") := 1
        m.performUpdates
        !(p("one") =:= 0) shouldBe true
        p("one") =:= 1 shouldBe true
    }

    test("inconsistent parameterised state updates in one step trigger an exception") {
        val m = makeMachine()
        val p = new m.ParamState[String, Int]("p")
        m.reset
        p("one") := 0
        p("one") := 1
        val i = intercept[InconsistentUpdateException] {
            m.performUpdates
        }
        i.getMessage shouldBe "Machine = m, updates = List(m.p(one) := 1, m.p(one) := 0)"
    }

    // Tests of step debugging trace

    {
        val mmemitter = new StringEmitter

        object MM extends Machine("MM", mmemitter) {
            override val debug = true

            val s = new State[Int]("s")
            val t = new State[String]("t")
            val p = new ParamState[String, Int]("p")

            def main {
                if (s.isUndefined) {
                    s := 0
                    p("one") := 42
                    p("two") := 99
                } else if (s =:= 0) {
                    s := 1
                    t := "hello"
                    p("two") := 88
                    p("three") := 66
                }
            }
        }

        test("running multiple steps produces a suitable trace") {
            MM.run
            mmemitter.result shouldBe
                """MM step 0
                  |MM.p(two) := 99
                  |MM.p(one) := 42
                  |MM.s := 0
                  |MM step 1
                  |MM.p(three) := 66
                  |MM.p(two) := 88
                  |MM.t := hello
                  |MM.s := 1
                  |MM step 2
                  |""".stripMargin
        }
    }

}