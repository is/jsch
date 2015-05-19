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

package com.jcraft.jsch.jcraft;
import com.jcraft.jzlib.*;
import com.jcraft.jsch.*;

/**
 * <p>Compression class.</p>
 *
 * @author <a href="https://github.com/ymnk"">Atsuhiko Yamanaka</a>
 * @version $Id: $Id
 */
public class Compression implements com.jcraft.jsch.Compression {
  static private final int BUF_SIZE=4096;

  private int type;
  private ZStream stream;
  private byte[] tmpbuf=new byte[BUF_SIZE];

  /**
   * <p>Constructor for Compression.</p>
   */
  public Compression(){
    stream=new ZStream();
  }

  @Override
  public void init(int type, int level){
    if(type==DEFLATER){
      stream.deflateInit(level);
      this.type=DEFLATER;
    }
    else if(type==INFLATER){
      stream.inflateInit();
      inflated_buf=new byte[BUF_SIZE];
      this.type=INFLATER;
    }
  }
  /*
  static Compression getDeflater(int level){
    Compression foo=new Compression();
    foo.stream.deflateInit(level);
    foo.type=DEFLATER;
    return foo;
  }
  */
  private byte[] inflated_buf;
  /*
  static Compression getInflater(){
    Compression foo=new Compression();
    foo.stream.inflateInit();
    foo.inflated_buf=new byte[BUF_SIZE];
    foo.type=INFLATER;
    return foo;
  }
  */

  @Override
  public int compress(byte[] buf, int start, int len){
    stream.next_in=buf;
    stream.next_in_index=start;
    stream.avail_in=len-start;
    int status;
    int outputlen=start;

    do{
      stream.next_out=tmpbuf;
      stream.next_out_index=0;
      stream.avail_out=BUF_SIZE;
      status=stream.deflate(JZlib.Z_PARTIAL_FLUSH);
      switch(status){
        case JZlib.Z_OK:
	    System.arraycopy(tmpbuf, 0,
			     buf, outputlen,
			     BUF_SIZE-stream.avail_out);
	    outputlen+=(BUF_SIZE-stream.avail_out);
	    break;
        default:
	    System.err.println("compress: deflate returnd "+status);
      }
    }
    while(stream.avail_out==0);
    return outputlen;
  }

  /**
   * <p>uncompress.</p>
   *
   * @param buffer an array of byte.
   * @param start a int.
   * @param length an array of int.
   * @return an array of byte.
   */
  public byte[] uncompress(byte[] buffer, int start, int[] length){
    int inflated_end=0;

    stream.next_in=buffer;
    stream.next_in_index=start;
    stream.avail_in=length[0];

    while(true){
      stream.next_out=tmpbuf;
      stream.next_out_index=0;
      stream.avail_out=BUF_SIZE;
      int status=stream.inflate(JZlib.Z_PARTIAL_FLUSH);
      switch(status){
        case JZlib.Z_OK:
	  if(inflated_buf.length<inflated_end+BUF_SIZE-stream.avail_out){
            byte[] foo=new byte[inflated_end+BUF_SIZE-stream.avail_out];
	    System.arraycopy(inflated_buf, 0, foo, 0, inflated_end);
	    inflated_buf=foo;
	  }
	  System.arraycopy(tmpbuf, 0,
			   inflated_buf, inflated_end,
			   BUF_SIZE-stream.avail_out);
	  inflated_end+=(BUF_SIZE-stream.avail_out);
          length[0]=inflated_end;
	  break;
        case JZlib.Z_BUF_ERROR:
          if(inflated_end>buffer.length-start){
            byte[] foo=new byte[inflated_end+start];
            System.arraycopy(buffer, 0, foo, 0, start);
            System.arraycopy(inflated_buf, 0, foo, start, inflated_end);
	    buffer=foo;
	  }
	  else{
            System.arraycopy(inflated_buf, 0, buffer, start, inflated_end);
	  }
          length[0]=inflated_end;
	  return buffer;
	default:
	  System.err.println("uncompress: inflate returnd "+status);
          return null;
      }
    }
  }
}
