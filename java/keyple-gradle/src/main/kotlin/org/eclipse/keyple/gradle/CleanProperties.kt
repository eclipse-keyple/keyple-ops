package org.eclipse.keyple.gradle

import java.io.*
import java.util.*


fun Properties.store(writer: Writer) {
    this.store(StripFirstLineStream(BufferedWriter(writer)), null)
}

private class StripFirstLineStream(original: BufferedWriter): BufferedWriter(original) {
    private var firstLineSeen = false

    override fun write(str: String) {
        if (firstLineSeen) {
            super.write(str)
        }
    }

    override fun newLine() {
        if (firstLineSeen) {
            super.newLine()
        } else {
            firstLineSeen = true
        }
    }
}