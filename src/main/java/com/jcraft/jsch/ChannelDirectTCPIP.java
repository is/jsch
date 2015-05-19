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

import java.io.*;

/**
 * <p>ChannelDirectTCPIP class.</p>
 *
 * @author <a href="https://github.com/ymnk"">Atsuhiko Yamanaka</a>
 * @version $Id: $Id
 */
public class ChannelDirectTCPIP extends Channel{

  static private final int LOCAL_WINDOW_SIZE_MAX=0x20000;
  static private final int LOCAL_MAXIMUM_PACKET_SIZE=0x4000;

  String host;
  int port;

  String originator_IP_address="127.0.0.1";
  int originator_port=0;

  ChannelDirectTCPIP(){
    super();
    setLocalWindowSizeMax(LOCAL_WINDOW_SIZE_MAX);
    setLocalWindowSize(LOCAL_WINDOW_SIZE_MAX);
    setLocalPacketSize(LOCAL_MAXIMUM_PACKET_SIZE);
  }

  void init (){
    try{ 
      io=new IO();
    }
    catch(Exception e){
      System.err.println(e);
    }
  }

  /**
   * <p>connect.</p>
   *
   * @throws com.jcraft.jsch.JSchException if any.
   */
  public void connect() throws JSchException{
    try{
      Session _session=getSession();
      if(!_session.isConnected()){
        throw new JSchException("session is down");
      }
      Buffer buf=new Buffer(150);
      Packet packet=new Packet(buf);
      // send
      // byte   SSH_MSG_CHANNEL_OPEN(90)
      // string channel type         //
      // uint32 sender channel       // 0
      // uint32 initial window size  // 0x100000(65536)
      // uint32 maxmum packet size   // 0x4000(16384)

      packet.reset();
      buf.putByte((byte)90);
      buf.putString(Util.str2byte("direct-tcpip"));
      buf.putInt(id);
      buf.putInt(lwsize);
      buf.putInt(lmpsize);
      buf.putString(Util.str2byte(host));
      buf.putInt(port);
      buf.putString(Util.str2byte(originator_IP_address));
      buf.putInt(originator_port);
      _session.write(packet);

      int retry=1000;
      try{
        while(this.getRecipient()==-1 && 
              _session.isConnected() &&
              retry>0 &&
              !eof_remote){
          //Thread.sleep(500);
          Thread.sleep(50);
          retry--;
        }
      }
      catch(Exception ee){
      }
      if(!_session.isConnected()){
	throw new JSchException("session is down");
      }
      if(retry==0 || this.eof_remote){
        throw new JSchException("channel is not opened.");
      }
      /*
      if(this.eof_remote){      // failed to open
        disconnect();
        return;
      }
      */

      connected=true;

      if(io.in!=null){
        thread=new Thread(this);
        thread.setName("DirectTCPIP thread "+_session.getHost());
        if(_session.daemon_thread){
          thread.setDaemon(_session.daemon_thread);
        }
        thread.start();
      }
    }
    catch(Exception e){
      io.close();
      io=null;
      Channel.del(this);
      if (e instanceof JSchException) {
        throw (JSchException) e;
      }
    }
  }

  /**
   * <p>run.</p>
   */
  public void run(){

    Buffer buf=new Buffer(rmpsize);
    Packet packet=new Packet(buf);
    int i=0;

    try{
      Session _session=getSession();
      while(isConnected() &&
            thread!=null && 
            io!=null && 
            io.in!=null){
        i=io.in.read(buf.buffer, 
                     14, 
                     buf.buffer.length-14
                     -32 -20 // padding and mac
                     );

        if(i<=0){
          eof();
          break;
        }
        if(close)break;
        packet.reset();
        buf.putByte((byte)Session.SSH_MSG_CHANNEL_DATA);
        buf.putInt(recipient);
        buf.putInt(i);
        buf.skip(i);
        _session.write(packet, this, i);
      }
    }
    catch(Exception e){
    }
    disconnect();
    //System.err.println("connect end");
  }

  @Override
  public void setInputStream(InputStream in){
    io.setInputStream(in);
  }
  @Override
  public void setOutputStream(OutputStream out){
    io.setOutputStream(out);
  }

  /**
   * <p>Setter for the field <code>host</code>.</p>
   *
   * @param host a {@link java.lang.String} object.
   */
  public void setHost(String host){this.host=host;}
  /**
   * <p>Setter for the field <code>port</code>.</p>
   *
   * @param port a int.
   */
  public void setPort(int port){this.port=port;}
  /**
   * <p>setOrgIPAddress.</p>
   *
   * @param foo a {@link java.lang.String} object.
   */
  public void setOrgIPAddress(String foo){this.originator_IP_address=foo;}
  /**
   * <p>setOrgPort.</p>
   *
   * @param foo a int.
   */
  public void setOrgPort(int foo){this.originator_port=foo;}
}
