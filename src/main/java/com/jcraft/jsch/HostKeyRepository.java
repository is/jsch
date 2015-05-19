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
 * <p>HostKeyRepository interface.</p>
 *
 * @author <a href="https://github.com/ymnk"">Atsuhiko Yamanaka</a>
 * @version $Id: $Id
 */
public interface HostKeyRepository{
  /** Constant <code>OK=0</code> */
  final int OK=0;
  /** Constant <code>NOT_INCLUDED=1</code> */
  final int NOT_INCLUDED=1;
  /** Constant <code>CHANGED=2</code> */
  final int CHANGED=2;

  /**
   * <p>check.</p>
   *
   * @param host a {@link java.lang.String} object.
   * @param key an array of byte.
   * @return a int.
   */
  int check(String host, byte[] key);
  /**
   * <p>add.</p>
   *
   * @param hostkey a {@link com.jcraft.jsch.HostKey} object.
   * @param ui a {@link com.jcraft.jsch.UserInfo} object.
   */
  void add(HostKey hostkey, UserInfo ui);
  /**
   * <p>remove.</p>
   *
   * @param host a {@link java.lang.String} object.
   * @param type a {@link java.lang.String} object.
   */
  void remove(String host, String type);
  /**
   * <p>remove.</p>
   *
   * @param host a {@link java.lang.String} object.
   * @param type a {@link java.lang.String} object.
   * @param key an array of byte.
   */
  void remove(String host, String type, byte[] key);
  /**
   * <p>getKnownHostsRepositoryID.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  String getKnownHostsRepositoryID();
  /**
   * <p>getHostKey.</p>
   *
   * @return an array of {@link com.jcraft.jsch.HostKey} objects.
   */
  HostKey[] getHostKey();
  /**
   * <p>getHostKey.</p>
   *
   * @param host a {@link java.lang.String} object.
   * @param type a {@link java.lang.String} object.
   * @return an array of {@link com.jcraft.jsch.HostKey} objects.
   */
  HostKey[] getHostKey(String host, String type);
}
