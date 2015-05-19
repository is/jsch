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
 * <p>Cipher interface.</p>
 *
 * @author <a href="https://github.com/ymnk"">Atsuhiko Yamanaka</a>
 * @version $Id: $Id
 */
public interface Cipher{
  /** Constant <code>ENCRYPT_MODE=0</code> */
  static int ENCRYPT_MODE=0;
  /** Constant <code>DECRYPT_MODE=1</code> */
  static int DECRYPT_MODE=1;
  /**
   * <p>getIVSize.</p>
   *
   * @return a int.
   */
  int getIVSize(); 
  /**
   * <p>getBlockSize.</p>
   *
   * @return a int.
   */
  int getBlockSize(); 
  /**
   * <p>init.</p>
   *
   * @param mode a int.
   * @param key an array of byte.
   * @param iv an array of byte.
   * @throws java.lang.Exception if any.
   */
  void init(int mode, byte[] key, byte[] iv) throws Exception; 
  /**
   * <p>update.</p>
   *
   * @param foo an array of byte.
   * @param s1 a int.
   * @param len a int.
   * @param bar an array of byte.
   * @param s2 a int.
   * @throws java.lang.Exception if any.
   */
  void update(byte[] foo, int s1, int len, byte[] bar, int s2) throws Exception;
  /**
   * <p>isCBC.</p>
   *
   * @return a boolean.
   */
  boolean isCBC();
}
