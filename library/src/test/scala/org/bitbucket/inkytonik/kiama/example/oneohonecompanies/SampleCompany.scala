/*
 * This file is part of Kiama.
 *
 * Copyright (C) 2010-2017 Anthony M Sloane, Macquarie University.
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
package example.oneohonecompanies

object SampleCompany {

    import CompanyTree._

    val craig = Employee("Craig", "Redmond", 123456)
    val erik = Employee("Erik", "Utrecht", 12345)
    val ralf = Employee("Ralf", "Koblenz", 1234)
    val research = Dept("Research", craig, Vector(PU(erik), PU(ralf)))

    val klaus = Employee("Klaus", "Boston", 23456)
    val ray = Employee("Ray", "Redmond", 234567)
    val karl = Employee("Karl", "Riga", 2345)
    val joe = Employee("Joe", "Wifi City", 2344)
    val dev11 = Dept("Dev1.1", karl, Vector(PU(joe)))
    val dev1 = Dept("Dev1", klaus, Vector(DU(dev11)))
    val dev = Dept("Development", ray, Vector(DU(dev1)))

    val company = Company(Vector(research, dev))

}