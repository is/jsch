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

package com.jcraft.jsch.jce;

import java.security.*;
import java.security.interfaces.*;

/**
 * <p>KeyPairGenDSA class.</p>
 *
 * @author <a href="https://github.com/ymnk"">Atsuhiko Yamanaka</a>
 * @version $Id: $Id
 */
public class KeyPairGenDSA implements com.jcraft.jsch.KeyPairGenDSA{
  byte[] x;  // private
  byte[] y;  // public
  byte[] p;
  byte[] q;
  byte[] g;

  @Override
  public void init(int key_size) throws Exception{
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
    keyGen.initialize(key_size, new SecureRandom());
    KeyPair pair = keyGen.generateKeyPair();
    PublicKey pubKey=pair.getPublic();
    PrivateKey prvKey=pair.getPrivate();

    x=((DSAPrivateKey)prvKey).getX().toByteArray();
    y=((DSAPublicKey)pubKey).getY().toByteArray();

    DSAParams params=((DSAKey)prvKey).getParams();
    p=params.getP().toByteArray();
    q=params.getQ().toByteArray();
    g=params.getG().toByteArray();
  }
  /**
   * <p>Getter for the field <code>x</code>.</p>
   *
   * @return an array of byte.
   */
  public byte[] getX(){return x;}
  /**
   * <p>Getter for the field <code>y</code>.</p>
   *
   * @return an array of byte.
   */
  public byte[] getY(){return y;}
  /**
   * <p>Getter for the field <code>p</code>.</p>
   *
   * @return an array of byte.
   */
  public byte[] getP(){return p;}
  /**
   * <p>Getter for the field <code>q</code>.</p>
   *
   * @return an array of byte.
   */
  public byte[] getQ(){return q;}
  /**
   * <p>Getter for the field <code>g</code>.</p>
   *
   * @return an array of byte.
   */
  public byte[] getG(){return g;}
}
