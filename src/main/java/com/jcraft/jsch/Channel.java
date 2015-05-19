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

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;


/**
 * <p>Abstract Channel class.</p>
 *
 * @author <a href="https://github.com/ymnk"">Atsuhiko Yamanaka</a>
 * @version $Id: $Id
 */
public abstract class Channel implements Runnable{

  static final int SSH_MSG_CHANNEL_OPEN_CONFIRMATION=      91;
  static final int SSH_MSG_CHANNEL_OPEN_FAILURE=           92;
  static final int SSH_MSG_CHANNEL_WINDOW_ADJUST=          93;

  static final int SSH_OPEN_ADMINISTRATIVELY_PROHIBITED=    1;
  static final int SSH_OPEN_CONNECT_FAILED=                 2;
  static final int SSH_OPEN_UNKNOWN_CHANNEL_TYPE=           3;
  static final int SSH_OPEN_RESOURCE_SHORTAGE=              4;

  static int index=0; 
  private static java.util.Vector pool=new java.util.Vector();
  static Channel getChannel(String type){
    if(type.equals("session")){
      return new ChannelSession();
    }
    if(type.equals("shell")){
      return new ChannelShell();
    }
    if(type.equals("exec")){
      return new ChannelExec();
    }
    if(type.equals("x11")){
      return new ChannelX11();
    }
    if(type.equals("auth-agent@openssh.com")){
      return new ChannelAgentForwarding();
    }
    if(type.equals("direct-tcpip")){
      return new ChannelDirectTCPIP();
    }
    if(type.equals("forwarded-tcpip")){
      return new ChannelForwardedTCPIP();
    }
    if(type.equals("sftp")){
      return new ChannelSftp();
    }
    if(type.equals("subsystem")){
      return new ChannelSubsystem();
    }
    return null;
  }
  static Channel getChannel(int id, Session session){
    synchronized(pool){
      for(int i=0; i<pool.size(); i++){
        Channel c=(Channel)(pool.elementAt(i));
        if(c.id==id && c.session==session) return c;
      }
    }
    return null;
  }
  static void del(Channel c){
    synchronized(pool){
      pool.removeElement(c);
    }
  }

  int id;
  int recipient=-1;
  byte[] type=Util.str2byte("foo");
  int lwsize_max=0x100000;
//int lwsize_max=0x20000;  // 32*1024*4
  int lwsize=lwsize_max;  // local initial window size
  int lmpsize=0x4000;     // local maximum packet size
//int lmpsize=0x8000;     // local maximum packet size

  long rwsize=0;         // remote initial window size
  int rmpsize=0;        // remote maximum packet size

  IO io=null;    
  Thread thread=null;

  boolean eof_local=false;
  boolean eof_remote=false;

  boolean close=false;
  boolean connected=false;

  int exitstatus=-1;

  int reply=0; 
  int connectTimeout=0;

  private Session session;

  int notifyme=0; 

  Channel(){
    synchronized(pool){
      id=index++;
      pool.addElement(this);
    }
  }
  void setRecipient(int foo){
    this.recipient=foo;
  }
  int getRecipient(){
    return recipient;
  }

  void init() throws JSchException {
  }

  /**
   * <p>connect.</p>
   *
   * @throws com.jcraft.jsch.JSchException if any.
   */
  public void connect() throws JSchException{
    connect(0);
  }

  /**
   * <p>connect.</p>
   *
   * @param connectTimeout a int.
   * @throws com.jcraft.jsch.JSchException if any.
   */
  public void connect(int connectTimeout) throws JSchException{
    Session _session=getSession();
    if(!_session.isConnected()){
      throw new JSchException("session is down");
    }
    this.connectTimeout=connectTimeout;
    try{
      Buffer buf=new Buffer(100);
      Packet packet=new Packet(buf);
      // send
      // byte   SSH_MSG_CHANNEL_OPEN(90)
      // string channel type         //
      // uint32 sender channel       // 0
      // uint32 initial window size  // 0x100000(65536)
      // uint32 maxmum packet size   // 0x4000(16384)
      packet.reset();
      buf.putByte((byte)90);
      buf.putString(this.type);
      buf.putInt(this.id);
      buf.putInt(this.lwsize);
      buf.putInt(this.lmpsize);
      _session.write(packet);
      int retry=1000;
      long start=System.currentTimeMillis();
      long timeout=connectTimeout;
      while(this.getRecipient()==-1 &&
	    _session.isConnected() &&
	    retry>0){
        if(timeout>0L){
          if((System.currentTimeMillis()-start)>timeout){
            retry=0;
            continue;
          }
        }
	try{Thread.sleep(50);}catch(Exception ee){}
	retry--;
      }
      if(!_session.isConnected()){
	throw new JSchException("session is down");
      }
      if(retry==0){
        throw new JSchException("channel is not opened.");
      }

      /*
       * At the failure in opening the channel on the sshd, 
       * 'SSH_MSG_CHANNEL_OPEN_FAILURE' will be sent from sshd and it will
       * be processed in Session#run().
       */
      if(this.isClosed()){
        throw new JSchException("channel is not opened.");
      }
      connected=true;
      start();
    }
    catch(Exception e){
      connected=false;
      disconnect();
      if(e instanceof JSchException) 
        throw (JSchException)e;
      throw new JSchException(e.toString(), e);
    }
  }

  /**
   * <p>setXForwarding.</p>
   *
   * @param foo a boolean.
   */
  public void setXForwarding(boolean foo){
  }

  /**
   * <p>start.</p>
   *
   * @throws com.jcraft.jsch.JSchException if any.
   */
  public void start() throws JSchException{}

  /**
   * <p>isEOF.</p>
   *
   * @return a boolean.
   */
  public boolean isEOF() {return eof_remote;}

  void getData(Buffer buf){
    setRecipient(buf.getInt());
    setRemoteWindowSize(buf.getUInt());
    setRemotePacketSize(buf.getInt());
  }

  /**
   * <p>setInputStream.</p>
   *
   * @param in a {@link java.io.InputStream} object.
   */
  public void setInputStream(InputStream in){
    io.setInputStream(in, false);
  }
  /**
   * <p>setInputStream.</p>
   *
   * @param in a {@link java.io.InputStream} object.
   * @param dontclose a boolean.
   */
  public void setInputStream(InputStream in, boolean dontclose){
    io.setInputStream(in, dontclose);
  }
  /**
   * <p>setOutputStream.</p>
   *
   * @param out a {@link java.io.OutputStream} object.
   */
  public void setOutputStream(OutputStream out){
    io.setOutputStream(out, false);
  }
  /**
   * <p>setOutputStream.</p>
   *
   * @param out a {@link java.io.OutputStream} object.
   * @param dontclose a boolean.
   */
  public void setOutputStream(OutputStream out, boolean dontclose){
    io.setOutputStream(out, dontclose);
  }
  /**
   * <p>setExtOutputStream.</p>
   *
   * @param out a {@link java.io.OutputStream} object.
   */
  public void setExtOutputStream(OutputStream out){
    io.setExtOutputStream(out, false);
  }
  /**
   * <p>setExtOutputStream.</p>
   *
   * @param out a {@link java.io.OutputStream} object.
   * @param dontclose a boolean.
   */
  public void setExtOutputStream(OutputStream out, boolean dontclose){
    io.setExtOutputStream(out, dontclose);
  }
  /**
   * <p>getInputStream.</p>
   *
   * @return a {@link java.io.InputStream} object.
   * @throws java.io.IOException if any.
   */
  public InputStream getInputStream() throws IOException {
    PipedInputStream in=
      new MyPipedInputStream(
                             32*1024  // this value should be customizable.
                             );
    io.setOutputStream(new PassiveOutputStream(in), false);
    return in;
  }
  /**
   * <p>getExtInputStream.</p>
   *
   * @return a {@link java.io.InputStream} object.
   * @throws java.io.IOException if any.
   */
  public InputStream getExtInputStream() throws IOException {
    PipedInputStream in=
      new MyPipedInputStream(
                             32*1024  // this value should be customizable.
                             );
    io.setExtOutputStream(new PassiveOutputStream(in), false);
    return in;
  }
  /**
   * <p>getOutputStream.</p>
   *
   * @return a {@link java.io.OutputStream} object.
   * @throws java.io.IOException if any.
   */
  public OutputStream getOutputStream() throws IOException {
    /*
    PipedOutputStream out=new PipedOutputStream();
    io.setInputStream(new PassiveInputStream(out
                                             , 32*1024
                                             ), false);
    return out;
    */

    final Channel channel=this;
    OutputStream out=new OutputStream(){
        private int dataLen=0;
        private Buffer buffer=null;
        private Packet packet=null;
        private boolean closed=false;
        private synchronized void init() throws java.io.IOException{
          buffer=new Buffer(rmpsize);
          packet=new Packet(buffer);

          byte[] _buf=buffer.buffer;
          if(_buf.length-(14+0)-32-20<=0){
            buffer=null;
            packet=null;
            throw new IOException("failed to initialize the channel.");
          }

        }
        byte[] b=new byte[1];
        public void write(int w) throws java.io.IOException{
          b[0]=(byte)w;
          write(b, 0, 1);
        }
        public void write(byte[] buf, int s, int l) throws java.io.IOException{
          if(packet==null){
            init();
          }

          if(closed){
            throw new java.io.IOException("Already closed");
          }

          byte[] _buf=buffer.buffer;
          int _bufl=_buf.length;
          while(l>0){
            int _l=l;
            if(l>_bufl-(14+dataLen)-32-20){
              _l=_bufl-(14+dataLen)-32-20;
            }

            if(_l<=0){
              flush();
              continue;
            }

            System.arraycopy(buf, s, _buf, 14+dataLen, _l);
            dataLen+=_l;
            s+=_l;
            l-=_l;
          }
        }

        public void flush() throws java.io.IOException{
          if(closed){
            throw new java.io.IOException("Already closed");
          }
          if(dataLen==0)
            return;
          packet.reset();
          buffer.putByte((byte)Session.SSH_MSG_CHANNEL_DATA);
          buffer.putInt(recipient);
          buffer.putInt(dataLen);
          buffer.skip(dataLen);
          try{
            int foo=dataLen;
            dataLen=0;
            getSession().write(packet, channel, foo);
          }
          catch(Exception e){
            close();
            throw new java.io.IOException(e.toString());
          }

        }
        public void close() throws java.io.IOException{
          if(packet==null){
            try{
              init();
            }
            catch(java.io.IOException e){
              // close should be finished silently.
              return;
            }
          }
          if(closed){
            return;
          }
          if(dataLen>0){
            flush();
          }
          channel.eof();
          closed=true;
        }
      };
    return out;
  }

  class MyPipedInputStream extends PipedInputStream{
    MyPipedInputStream() throws IOException{ super(); }
    MyPipedInputStream(int size) throws IOException{
      super();
      buffer=new byte[size];
    }
    MyPipedInputStream(PipedOutputStream out) throws IOException{ super(out); }
    MyPipedInputStream(PipedOutputStream out, int size) throws IOException{
      super(out);
      buffer=new byte[size];
    }
  }
  void setLocalWindowSizeMax(int foo){ this.lwsize_max=foo; }
  void setLocalWindowSize(int foo){ this.lwsize=foo; }
  void setLocalPacketSize(int foo){ this.lmpsize=foo; }
  synchronized void setRemoteWindowSize(long foo){ this.rwsize=foo; }
  synchronized void addRemoteWindowSize(int foo){ 
    this.rwsize+=foo; 
    if(notifyme>0)
      notifyAll();
  }
  void setRemotePacketSize(int foo){ this.rmpsize=foo; }

  /**
   * <p>run.</p>
   */
  public void run(){
  }

  void write(byte[] foo) throws IOException {
    write(foo, 0, foo.length);
  }
  void write(byte[] foo, int s, int l) throws IOException {
    try{
      io.put(foo, s, l);
    }catch(NullPointerException e){}
  }
  void write_ext(byte[] foo, int s, int l) throws IOException {
    try{
      io.put_ext(foo, s, l);
    }catch(NullPointerException e){}
  }

  void eof_remote(){
    eof_remote=true;
    try{
      io.out_close();
    }
    catch(NullPointerException e){}
  }

  void eof(){
    if(eof_local)return;
    eof_local=true;

    try{
      Buffer buf=new Buffer(100);
      Packet packet=new Packet(buf);
      packet.reset();
      buf.putByte((byte)Session.SSH_MSG_CHANNEL_EOF);
      buf.putInt(getRecipient());
      synchronized(this){
        if(!close)
          getSession().write(packet);
      }
    }
    catch(Exception e){
      //System.err.println("Channel.eof");
      //e.printStackTrace();
    }
    /*
    if(!isConnected()){ disconnect(); }
    */
  }

  /*
  http://www1.ietf.org/internet-drafts/draft-ietf-secsh-connect-24.txt

5.3  Closing a Channel
  When a party will no longer send more data to a channel, it SHOULD
   send SSH_MSG_CHANNEL_EOF.

            byte      SSH_MSG_CHANNEL_EOF
            uint32    recipient_channel

  No explicit response is sent to this message.  However, the
   application may send EOF to whatever is at the other end of the
  channel.  Note that the channel remains open after this message, and
   more data may still be sent in the other direction.  This message
   does not consume window space and can be sent even if no window space
   is available.

     When either party wishes to terminate the channel, it sends
     SSH_MSG_CHANNEL_CLOSE.  Upon receiving this message, a party MUST
   send back a SSH_MSG_CHANNEL_CLOSE unless it has already sent this
   message for the channel.  The channel is considered closed for a
     party when it has both sent and received SSH_MSG_CHANNEL_CLOSE, and
   the party may then reuse the channel number.  A party MAY send
   SSH_MSG_CHANNEL_CLOSE without having sent or received
   SSH_MSG_CHANNEL_EOF.

            byte      SSH_MSG_CHANNEL_CLOSE
            uint32    recipient_channel

   This message does not consume window space and can be sent even if no
   window space is available.

   It is recommended that any data sent before this message is delivered
     to the actual destination, if possible.
  */

  void close(){
    if(close)return;
    close=true;
    eof_local=eof_remote=true;

    try{
      Buffer buf=new Buffer(100);
      Packet packet=new Packet(buf);
      packet.reset();
      buf.putByte((byte)Session.SSH_MSG_CHANNEL_CLOSE);
      buf.putInt(getRecipient());
      synchronized(this){
        getSession().write(packet);
      }
    }
    catch(Exception e){
      //e.printStackTrace();
    }
  }
  /**
   * <p>isClosed.</p>
   *
   * @return a boolean.
   */
  public boolean isClosed(){
    return close;
  }
  static void disconnect(Session session){
    Channel[] channels=null;
    int count=0;
    synchronized(pool){
      channels=new Channel[pool.size()];
      for(int i=0; i<pool.size(); i++){
	try{
	  Channel c=((Channel)(pool.elementAt(i)));
	  if(c.session==session){
	    channels[count++]=c;
	  }
	}
	catch(Exception e){
	}
      } 
    }
    for(int i=0; i<count; i++){
      channels[i].disconnect();
    }
  }

  /**
   * <p>disconnect.</p>
   */
  public void disconnect(){
    //System.err.println(this+":disconnect "+io+" "+connected);
    //Thread.dumpStack();

    try{

      synchronized(this){
        if(!connected){
          return;
        }
        connected=false;
      }

      close();

      eof_remote=eof_local=true;

      thread=null;

      try{
        if(io!=null){
          io.close();
        }
      }
      catch(Exception e){
        //e.printStackTrace();
      }
      // io=null;
    }
    finally{
      Channel.del(this);
    }
  }

  /**
   * <p>isConnected.</p>
   *
   * @return a boolean.
   */
  public boolean isConnected(){
    Session _session=this.session;
    if(_session!=null){
      return _session.isConnected() && connected;
    }
    return false;
  }

  /**
   * <p>sendSignal.</p>
   *
   * @param signal a {@link java.lang.String} object.
   * @throws java.lang.Exception if any.
   */
  public void sendSignal(String signal) throws Exception {
    RequestSignal request=new RequestSignal();
    request.setSignal(signal);
    request.request(getSession(), this);
  }

//  public String toString(){
//      return "Channel: type="+new String(type)+",id="+id+",recipient="+recipient+",window_size="+window_size+",packet_size="+packet_size;
//  }

/*
  class OutputThread extends Thread{
    Channel c;
    OutputThread(Channel c){ this.c=c;}
    public void run(){c.output_thread();}
  }
*/

  class PassiveInputStream extends MyPipedInputStream{
    PipedOutputStream out;
    PassiveInputStream(PipedOutputStream out, int size) throws IOException{
      super(out, size);
      this.out=out;
    }
    PassiveInputStream(PipedOutputStream out) throws IOException{
      super(out);
      this.out=out;
    }
    public void close() throws IOException{
      if(out!=null){
        this.out.close();
      }
      out=null;
    }
  }
  class PassiveOutputStream extends PipedOutputStream{
    PassiveOutputStream(PipedInputStream in) throws IOException{
      super(in);
    }
  }

  void setExitStatus(int status){ exitstatus=status; }
  /**
   * <p>getExitStatus.</p>
   *
   * @return a int.
   */
  public int getExitStatus(){ return exitstatus; }

  void setSession(Session session){
    this.session=session;
  }

  /**
   * <p>Getter for the field <code>session</code>.</p>
   *
   * @return a {@link com.jcraft.jsch.Session} object.
   * @throws com.jcraft.jsch.JSchException if any.
   */
  public Session getSession() throws JSchException{ 
    Session _session=session;
    if(_session==null){
      throw new JSchException("session is not available");
    }
    return _session;
  }
  /**
   * <p>Getter for the field <code>id</code>.</p>
   *
   * @return a int.
   */
  public int getId(){ return id; }

  /**
   * <p>sendOpenConfirmation.</p>
   *
   * @throws java.lang.Exception if any.
   */
  protected void sendOpenConfirmation() throws Exception{
    Buffer buf=new Buffer(100);
    Packet packet=new Packet(buf);
    packet.reset();
    buf.putByte((byte)SSH_MSG_CHANNEL_OPEN_CONFIRMATION);
    buf.putInt(getRecipient());
    buf.putInt(id);
    buf.putInt(lwsize);
    buf.putInt(lmpsize);
    getSession().write(packet);
  }

  /**
   * <p>sendOpenFailure.</p>
   *
   * @param reasoncode a int.
   */
  protected void sendOpenFailure(int reasoncode){
    try{
      Buffer buf=new Buffer(100);
      Packet packet=new Packet(buf);
      packet.reset();
      buf.putByte((byte)SSH_MSG_CHANNEL_OPEN_FAILURE);
      buf.putInt(getRecipient());
      buf.putInt(reasoncode);
      buf.putString(Util.str2byte("open failed"));
      buf.putString(Util.empty);
      getSession().write(packet);
    }
    catch(Exception e){
    }
  }
}
