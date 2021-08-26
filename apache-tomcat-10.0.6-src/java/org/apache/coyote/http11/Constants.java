/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.coyote.http11;

import org.apache.tomcat.util.buf.ByteChunk;

/**
 * Constants.
 *
 * @author Remy Maucherat
 */
public final class Constants {

    public static final int DEFAULT_CONNECTION_TIMEOUT = 60000;

    /**
     * 在计算机还没有出现之前，有一种叫做电传打字机（Teletype Model 33，Linux/Unix下的tty概念也来自于此）的玩意，每秒钟可以打10个字符。
     * 但是它有一个问题，就是打完一行换行的时候，要用去0.2秒，正好可以打两个字符。
     * 要是在这0.2秒里面，又有新的字符传过来，那么这个字符将丢失。
     * 于是，研制人员想了个办法解决这个问题，就是在每行后面加两个表示结束的字符。
     * 一个叫做“回车”，告诉打字机把打印头定位在左边界；另一个叫做“换行”，告诉打字机把纸向下移一行。
     * 这就是“换行”和“回车”的来历，从它们的英语名字上也可以看出一二。
     *
     * 后来，计算机发明了，这两个概念也就被般到了计算机上。
     * 那时，存储器很贵，一些科学家认为在每行结尾加两个字符太浪费了，加一个就可以。于是，就出现了分歧。
     * Unix系统里，每行结尾只有“<换行>”，即"\n"；
     * Windows系统里面，每行结尾是“<回车><换行>”，即“\r\n”；
     * Mac系统里，每行结尾是“<回车>”，即"\r"。
     * 一个直接后果是，Unix/Mac系统下的文件在 Windows里打开的话，所有文字会变成一行；
     * 而Windows里的文件在Unix/Mac下打开的话，在每行的结尾可能会多出一个^M符号（ASCII中回车的文字表示）。
     */
    /**
     * CRLF.
     * Dos和windows采用回车+换行CR/LF表示下一行
     */
    public static final String CRLF = "\r\n";


    /**
     * CR.(Carriage Return) 运输车返回，就是回车
     * 将光标移动到当前行的开头
     * ASCII代码是13
     * 十六进制代码为0x0D
     * MAC OS则采用回车符CR表示下一行
     */
    public static final byte CR = (byte) '\r';


    /**
     * LF.(Line Feed) 当前行饱和，就要进行换行
     * 将光标“垂直”移动到下一行。（而并不移动到下一行的开头，即不改变光标水平位置）
     * ASCII代码是10
     * 十六制为0x0A
     * UNIX/Linux采用换行符LF表示下一行
     */
    public static final byte LF = (byte) '\n';


    /**
     * SP.
     */
    public static final byte SP = (byte) ' ';


    /**
     * HT.
     */
    public static final byte HT = (byte) '\t';


    /**
     * COLON.
     */
    public static final byte COLON = (byte) ':';


    /**
     * SEMI_COLON.
     */
    public static final byte SEMI_COLON = (byte) ';';


    /**
     * 'A'.
     */
    public static final byte A = (byte) 'A';


    /**
     * 'a'.
     */
    public static final byte a = (byte) 'a';


    /**
     * 'Z'.
     */
    public static final byte Z = (byte) 'Z';


    /**
     * '?'.
     */
    public static final byte QUESTION = (byte) '?';


    /**
     * Lower case offset.
     */
    public static final byte LC_OFFSET = A - a;


    /* Various constant "strings" */
    public static final String CONNECTION = "Connection";
    public static final String CLOSE = "close";
    public static final String KEEP_ALIVE_HEADER_VALUE_TOKEN = "keep-alive";
    public static final String CHUNKED = "chunked";
    public static final byte[] ACK_BYTES = ByteChunk.convertToBytes("HTTP/1.1 100 " + CRLF + CRLF);
    public static final String TRANSFERENCODING = "Transfer-Encoding";
    public static final String KEEP_ALIVE_HEADER_NAME = "Keep-Alive";
    public static final byte[] _200_BYTES = ByteChunk.convertToBytes("200");
    public static final byte[] _400_BYTES = ByteChunk.convertToBytes("400");
    public static final byte[] _404_BYTES = ByteChunk.convertToBytes("404");


    /**
     * Identity filters (input and output).
     */
    public static final int IDENTITY_FILTER = 0;


    /**
     * Chunked filters (input and output).
     */
    public static final int CHUNKED_FILTER = 1;


    /**
     * Void filters (input and output).
     */
    public static final int VOID_FILTER = 2;


    /**
     * GZIP filter (output).
     */
    public static final int GZIP_FILTER = 3;


    /**
     * Buffered filter (input)
     */
    public static final int BUFFERED_FILTER = 3;


    /**
     * HTTP/1.0.
     */
    public static final String HTTP_10 = "HTTP/1.0";


    /**
     * HTTP/1.1.
     */
    public static final String HTTP_11 = "HTTP/1.1";
    public static final byte[] HTTP_11_BYTES = ByteChunk.convertToBytes(HTTP_11);
}
