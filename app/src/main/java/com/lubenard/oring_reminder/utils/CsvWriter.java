package com.lubenard.oring_reminder.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CsvWriter {

    Writer fileWriter;

    /**
     * Constructor
     * @param outputFileStream
     */
    public CsvWriter(OutputStream outputFileStream) {
        fileWriter = new OutputStreamWriter(outputFileStream, StandardCharsets.UTF_8);
    }

    /**
     * Write colum names
     * @param names column's name
     * @throws IOException
     */
    public void writeColumnsName(String[] names) throws IOException {
        if (fileWriter != null) {
            for (int i = 0; i != names.length; i++) {
                fileWriter.append(names[i]);
                fileWriter.append(',');
            }
            fileWriter.append('\n');
        }
    }

    /**
     * Write column datas
     * @param datas datas to write
     * @throws IOException IOException thrown if error happens
     */
    public void writeColumnsDatas(ArrayList<String> datas) throws IOException {
        if (fileWriter != null) {
            for (int i = 0; i != datas.size(); i++) {
                fileWriter.append(datas.get(i));
                fileWriter.append(',');
            }
            fileWriter.append('\n');
        }
    }

    public void close() throws IOException{
        fileWriter.close();
    }
}
