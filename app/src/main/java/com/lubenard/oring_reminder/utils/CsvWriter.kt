package com.lubenard.oring_reminder.utils

import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer
import java.nio.charset.StandardCharsets

class CsvWriter(outputFileStream: OutputStream) {

    private val fileWriter: Writer?

    /**
     * Constructor
     * @param outputFileStream stream to output to
     */
    init {
        fileWriter = OutputStreamWriter(outputFileStream, StandardCharsets.UTF_8)
    }

    /**
     * Write colum names
     * @param names column's name
     * @throws IOException if error happens during write
     */
    fun writeColumnsName(names: List<String>) {
        if (fileWriter != null) {
            names.forEach{ string ->
                fileWriter.append(string)
                fileWriter.append(',')
            }
            fileWriter.append('\n')
        }
    }

    /**
     * Write column datas
     * @param datas datas to write
     * @throws IOException IOException thrown if error happens
     */
    fun writeColumnsDatas(datas: ArrayList<String>) {
        if (fileWriter != null) {
            datas.forEach { string ->
                fileWriter.append(string)
                fileWriter.append(',')
            }
            fileWriter.append('\n')
        }
    }

    /**
     * Close the file Writer properly
     */
    fun close() {
        fileWriter?.close()
    }
}
