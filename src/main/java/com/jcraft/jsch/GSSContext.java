/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/*
Copyright (c) 2004-2010 ymnk, JCraft,Inc. All rights reserved.

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
 * <p>GSSContext interface.</p>
 *
 * @author <a href="https://github.com/ymnk"">Atsuhiko Yamanaka</a>
 * @version $Id: $Id
 */
public interface GSSContext{
  /**
   * <p>create.</p>
   *
   * @param user a {@link java.lang.String} object.
   * @param host a {@link java.lang.String} object.
   * @throws com.jcraft.jsch.JSchException if any.
   */
  public void create(String user, String host) throws JSchException;
  /**
   * <p>isEstablished.</p>
   *
   * @return a boolean.
   */
  public boolean isEstablished();
  /**
   * <p>init.</p>
   *
   * @param token an array of byte.
   * @param s a int.
   * @param l a int.
   * @return an array of byte.
   * @throws com.jcraft.jsch.JSchException if any.
   */
  public byte[] init(byte[] token, int s, int l) throws JSchException;
  /**
   * <p>getMIC.</p>
   *
   * @param message an array of byte.
   * @param s a int.
   * @param l a int.
   * @return an array of byte.
   */
  public byte[] getMIC(byte[] message, int s, int l);
  /**
   * <p>dispose.</p>
   */
  public void dispose();
}
