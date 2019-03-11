/*
 * This file is part of Kiama.
 *
 * Copyright (C) 2015-2019 Anthony M Sloane, Macquarie University.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.bitbucket.inkytonik.kiama
package util

/**
 * Support code for handling filenames.
 */
object Filenames {

    import java.io.File.separatorChar
    import java.lang.System.getProperty

    /**
     * Return a simplified filename where a string has been dropped if it
     * occurs as a prefix of the given filename. The system separator
     * character is also dropped if it occurs immediately after a
     * non-empty prefix.
     */
    def dropPrefix(filename : String, prefix : String) : String = {

        def dropIgnoreSep(i : Int) : String =
            if ((i == 0) || ((i == 1) && (filename(0) == separatorChar)))
                filename
            else if (i < filename.length)
                filename.drop(if (filename(i) == separatorChar) i + 1 else i)
            else
                ""

        for (i <- 0 until prefix.length) {
            if ((i == filename.length) || (filename(i) != prefix(i)))
                return filename
        }
        dropIgnoreSep(prefix.length)

    }

    /**
     * Return a simplified filename where the current path has been dropped
     * if it occurs as a prefix of the given filename.
     */
    def dropCurrentPath(filename : String) : String =
        dropPrefix(filename, getProperty("user.dir"))

    /**
     * Return a temporaray file name based on the current time. Append the
     * suffix (default: nothing).
     */
    def makeTempFilename(suffix : String = "") : String = {
        import scala.compat.Platform.currentTime

        val tmpDir = System.getProperty("java.io.tmpdir")
        s"${tmpDir}${separatorChar}kiama${currentTime}${suffix}"
    }

}
