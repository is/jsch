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
 * <p>HostKey class.</p>
 *
 * @author <a href="https://github.com/ymnk"">Atsuhiko Yamanaka</a>
 * @version $Id: $Id
 */
public class HostKey{
  private static final byte[] sshdss=Util.str2byte("ssh-dss");
  private static final byte[] sshrsa=Util.str2byte("ssh-rsa");

  /** Constant <code>GUESS=0</code> */
  protected static final int GUESS=0;
  /** Constant <code>SSHDSS=1</code> */
  public static final int SSHDSS=1;
  /** Constant <code>SSHRSA=2</code> */
  public static final int SSHRSA=2;
  static final int UNKNOWN=3;

  /**
   * The name of the host
   */
  protected String host;
  
  /**
   * The type of key
   */
  protected int type;
  
  /**
   * The bytes of the host key
   */
  protected byte[] key;

  /**
   * <p>Constructor for HostKey.</p>
   *
   * @param host a {@link java.lang.String} object.
   * @param key an array of byte.
   * @throws com.jcraft.jsch.JSchException if any.
   */
  public HostKey(String host, byte[] key) throws JSchException {
    this(host, GUESS, key);
  }

  /**
   * <p>Constructor for HostKey.</p>
   *
   * @param host a {@link java.lang.String} object.
   * @param type a int.
   * @param key an array of byte.
   * @throws com.jcraft.jsch.JSchException if any.
   */
  public HostKey(String host, int type, byte[] key) throws JSchException {
    this.host=host; 
    if(type==GUESS){
      if(key[8]=='d'){ this.type=SSHDSS; }
      else if(key[8]=='r'){ this.type=SSHRSA; }
      else { throw new JSchException("invalid key type");}
    }
    else{
      this.type=type; 
    }
    this.key=key;
  }

  /**
   * <p>Getter for the field <code>host</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getHost(){ return host; }
  /**
   * <p>Getter for the field <code>type</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getType(){
    if(type==SSHDSS){ return Util.byte2str(sshdss); }
    if(type==SSHRSA){ return Util.byte2str(sshrsa);}
    return "UNKNOWN";
  }
  /**
   * <p>Getter for the field <code>key</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getKey(){
    return Util.byte2str(Util.toBase64(key, 0, key.length));
  }
  /**
   * <p>getFingerPrint.</p>
   *
   * @param jsch a {@link com.jcraft.jsch.JSch} object.
   * @return a {@link java.lang.String} object.
   */
  public String getFingerPrint(JSch jsch){
    HASH hash=null;
    try{
      Class c=Class.forName(jsch.getConfig("md5"));
      hash=(HASH)(c.newInstance());
    }
    catch(Exception e){ System.err.println("getFingerPrint: "+e); }
    return Util.getFingerPrint(hash, key);
  }

  boolean isMatched(String _host){
    return isIncluded(_host);
  }

  private boolean isIncluded(String _host){
    int i=0;
    String hosts=this.host; 
    int hostslen=hosts.length();
    int hostlen=_host.length();
    int j;
    while(i<hostslen){
      j=hosts.indexOf(',', i);
      if(j==-1){
       if(hostlen!=hostslen-i) return false;
       return hosts.regionMatches(true, i, _host, 0, hostlen);
      }
      if(hostlen==(j-i)){
	if(hosts.regionMatches(true, i, _host, 0, hostlen)) return true;
      }
      i=j+1;
    }
    return false;
  }
}
