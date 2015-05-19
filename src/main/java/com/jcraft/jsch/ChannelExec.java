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

import java.util.*;

/**
 * <p>ChannelExec class.</p>
 *
 * @author <a href="https://github.com/ymnk"">Atsuhiko Yamanaka</a>
 * @version $Id: $Id
 */
public class ChannelExec extends ChannelSession{

  byte[] command=new byte[0];

  /**
   * <p>start.</p>
   *
   * @throws com.jcraft.jsch.JSchException if any.
   */
  public void start() throws JSchException{
    Session _session=getSession();
    try{
      sendRequests();
      Request request=new RequestExec(command);
      request.request(_session, this);
    }
    catch(Exception e){
      if(e instanceof JSchException) throw (JSchException)e;
      if(e instanceof Throwable)
        throw new JSchException("ChannelExec", (Throwable)e);
      throw new JSchException("ChannelExec");
    }

    if(io.in!=null){
      thread=new Thread(this);
      thread.setName("Exec thread "+_session.getHost());
      if(_session.daemon_thread){
        thread.setDaemon(_session.daemon_thread);
      }
      thread.start();
    }
  }

  /**
   * <p>Setter for the field <code>command</code>.</p>
   *
   * @param command a {@link java.lang.String} object.
   */
  public void setCommand(String command){ 
    this.command=Util.str2byte(command);
  }
  /**
   * <p>Setter for the field <code>command</code>.</p>
   *
   * @param command an array of byte.
   */
  public void setCommand(byte[] command){ 
    this.command=command;
  }

  void init() throws JSchException {
    io.setInputStream(getSession().in);
    io.setOutputStream(getSession().out);
  }

  /**
   * <p>setErrStream.</p>
   *
   * @param out a {@link java.io.OutputStream} object.
   */
  public void setErrStream(java.io.OutputStream out){
    setExtOutputStream(out);
  }
  /**
   * <p>setErrStream.</p>
   *
   * @param out a {@link java.io.OutputStream} object.
   * @param dontclose a boolean.
   */
  public void setErrStream(java.io.OutputStream out, boolean dontclose){
    setExtOutputStream(out, dontclose);
  }
  /**
   * <p>getErrStream.</p>
   *
   * @return a {@link java.io.InputStream} object.
   * @throws java.io.IOException if any.
   */
  public java.io.InputStream getErrStream() throws java.io.IOException {
    return getExtInputStream();
  }
}
