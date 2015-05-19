/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/*
Copyright (c) 2002-2010 ymnk, JCraft,Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright 
     notice, this list of conditions and the following disclaimer in 
     the documentation and/or other materials provided with the distribution.

  3. The names of the authors may not be used to endorse or promote products
     derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.jcraft.jsch;

/**
 * <p>Buffer class.</p>
 *
 * @author <a href="https://github.com/ymnk"">Atsuhiko Yamanaka</a>
 * @version $Id: $Id
 */
public class Buffer{
  final byte[] tmp=new byte[4];
  byte[] buffer;
  int index;
  int s;
  /**
   * <p>Constructor for Buffer.</p>
   *
   * @param size a int.
   */
  public Buffer(int size){
    buffer=new byte[size];
    index=0;
    s=0;
  }
  /**
   * <p>Constructor for Buffer.</p>
   *
   * @param buffer an array of byte.
   */
  public Buffer(byte[] buffer){
    this.buffer=buffer;
    index=0;
    s=0;
  }
  /**
   * <p>Constructor for Buffer.</p>
   */
  public Buffer(){ this(1024*10*2); }
  /**
   * <p>putByte.</p>
   *
   * @param foo a byte.
   */
  public void putByte(byte foo){
    buffer[index++]=foo;
  }
  /**
   * <p>putByte.</p>
   *
   * @param foo an array of byte.
   */
  public void putByte(byte[] foo) {
    putByte(foo, 0, foo.length);
  }
  /**
   * <p>putByte.</p>
   *
   * @param foo an array of byte.
   * @param begin a int.
   * @param length a int.
   */
  public void putByte(byte[] foo, int begin, int length) {
    System.arraycopy(foo, begin, buffer, index, length);
    index+=length;
  }
  /**
   * <p>putString.</p>
   *
   * @param foo an array of byte.
   */
  public void putString(byte[] foo){
    putString(foo, 0, foo.length);
  }
  /**
   * <p>putString.</p>
   *
   * @param foo an array of byte.
   * @param begin a int.
   * @param length a int.
   */
  public void putString(byte[] foo, int begin, int length) {
    putInt(length);
    putByte(foo, begin, length);
  }
  /**
   * <p>putInt.</p>
   *
   * @param val a int.
   */
  public void putInt(int val) {
    tmp[0]=(byte)(val >>> 24);
    tmp[1]=(byte)(val >>> 16);
    tmp[2]=(byte)(val >>> 8);
    tmp[3]=(byte)(val);
    System.arraycopy(tmp, 0, buffer, index, 4);
    index+=4;
  }
  /**
   * <p>putLong.</p>
   *
   * @param val a long.
   */
  public void putLong(long val) {
    tmp[0]=(byte)(val >>> 56);
    tmp[1]=(byte)(val >>> 48);
    tmp[2]=(byte)(val >>> 40);
    tmp[3]=(byte)(val >>> 32);
    System.arraycopy(tmp, 0, buffer, index, 4);
    tmp[0]=(byte)(val >>> 24);
    tmp[1]=(byte)(val >>> 16);
    tmp[2]=(byte)(val >>> 8);
    tmp[3]=(byte)(val);
    System.arraycopy(tmp, 0, buffer, index+4, 4);
    index+=8;
  }
  void skip(int n) {
    index+=n;
  }
  void putPad(int n) {
    while(n>0){
      buffer[index++]=(byte)0;
      n--;
    }
  }
  /**
   * <p>putMPInt.</p>
   *
   * @param foo an array of byte.
   */
  public void putMPInt(byte[] foo){
    int i=foo.length;
    if((foo[0]&0x80)!=0){
      i++;
      putInt(i);
      putByte((byte)0);
    }
    else{
      putInt(i);
    }
    putByte(foo);
  }
  /**
   * <p>getLength.</p>
   *
   * @return a int.
   */
  public int getLength(){
    return index-s;
  }
  /**
   * <p>getOffSet.</p>
   *
   * @return a int.
   */
  public int getOffSet(){
    return s;
  }
  /**
   * <p>setOffSet.</p>
   *
   * @param s a int.
   */
  public void setOffSet(int s){
    this.s=s;
  }
  /**
   * <p>getLong.</p>
   *
   * @return a long.
   */
  public long getLong(){
    long foo = getInt()&0xffffffffL;
    foo = ((foo<<32)) | (getInt()&0xffffffffL);
    return foo;
  }
  /**
   * <p>getInt.</p>
   *
   * @return a int.
   */
  public int getInt(){
    int foo = getShort();
    foo = ((foo<<16)&0xffff0000) | (getShort()&0xffff);
    return foo;
  }
  /**
   * <p>getUInt.</p>
   *
   * @return a long.
   */
  public long getUInt(){
    long foo = 0L;
    long bar = 0L;
    foo = getByte();
    foo = ((foo<<8)&0xff00)|(getByte()&0xff);
    bar = getByte();
    bar = ((bar<<8)&0xff00)|(getByte()&0xff);
    foo = ((foo<<16)&0xffff0000) | (bar&0xffff);
    return foo;
  }
  int getShort() {
    int foo = getByte();
    foo = ((foo<<8)&0xff00)|(getByte()&0xff);
    return foo;
  }
  /**
   * <p>getByte.</p>
   *
   * @return a int.
   */
  public int getByte() {
    return (buffer[s++]&0xff);
  }
  /**
   * <p>getByte.</p>
   *
   * @param foo an array of byte.
   */
  public void getByte(byte[] foo) {
    getByte(foo, 0, foo.length);
  }
  void getByte(byte[] foo, int start, int len) {
    System.arraycopy(buffer, s, foo, start, len); 
    s+=len;
  }
  /**
   * <p>getByte.</p>
   *
   * @param len a int.
   * @return a int.
   */
  public int getByte(int len) {
    int foo=s;
    s+=len;
    return foo;
  }
  /**
   * <p>getMPInt.</p>
   *
   * @return an array of byte.
   */
  public byte[] getMPInt() {
    int i=getInt();  // uint32
    if(i<0 ||  // bigger than 0x7fffffff
       i>8*1024){
      // TODO: an exception should be thrown.
      i = 8*1024; // the session will be broken, but working around OOME.
    }
    byte[] foo=new byte[i];
    getByte(foo, 0, i);
    return foo;
  }
  /**
   * <p>getMPIntBits.</p>
   *
   * @return an array of byte.
   */
  public byte[] getMPIntBits() {
    int bits=getInt();
    int bytes=(bits+7)/8;
    byte[] foo=new byte[bytes];
    getByte(foo, 0, bytes);
    if((foo[0]&0x80)!=0){
      byte[] bar=new byte[foo.length+1];
      bar[0]=0; // ??
      System.arraycopy(foo, 0, bar, 1, foo.length);
      foo=bar;
    }
    return foo;
  }
  /**
   * <p>getString.</p>
   *
   * @return an array of byte.
   */
  public byte[] getString() {
    int i = getInt();  // uint32
    if(i<0 ||  // bigger than 0x7fffffff
       i>256*1024){
      // TODO: an exception should be thrown.
      i = 256*1024; // the session will be broken, but working around OOME.
    }
    byte[] foo=new byte[i];
    getByte(foo, 0, i);
    return foo;
  }
  byte[] getString(int[]start, int[]len) {
    int i=getInt();
    start[0]=getByte(i);
    len[0]=i;
    return buffer;
  }
  /**
   * <p>reset.</p>
   */
  public void reset(){
    index=0;
    s=0;
  }
  /**
   * <p>shift.</p>
   */
  public void shift(){
    if(s==0)return;
    System.arraycopy(buffer, s, buffer, 0, index-s);
    index=index-s;
    s=0;
  }
  void rewind(){
    s=0;
  }

  byte getCommand(){
    return buffer[5];
  }

  void checkFreeSize(int n){
    if(buffer.length<index+n){
      byte[] tmp = new byte[buffer.length*2];
      System.arraycopy(buffer, 0, tmp, 0, index);
      buffer = tmp;
    }
  }

/*
  static String[] chars={
    "0","1","2","3","4","5","6","7","8","9", "a","b","c","d","e","f"
  };
  static void dump_buffer(){
    int foo;
    for(int i=0; i<tmp_buffer_index; i++){
        foo=tmp_buffer[i]&0xff;
	System.err.print(chars[(foo>>>4)&0xf]);
	System.err.print(chars[foo&0xf]);
        if(i%16==15){
          System.err.println("");
	  continue;
	}
        if(i>0 && i%2==1){
          System.err.print(" ");
	}
    }
    System.err.println("");
  }
  static void dump(byte[] b){
    dump(b, 0, b.length);
  }
  static void dump(byte[] b, int s, int l){
    for(int i=s; i<s+l; i++){
      System.err.print(Integer.toHexString(b[i]&0xff)+":");
    }
    System.err.println("");
  }
*/

}
