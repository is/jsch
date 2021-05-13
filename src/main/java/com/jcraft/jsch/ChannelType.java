/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/*
Copyright (c) 2002-2016 ymnk, JCraft,Inc. All rights reserved.
Define support channel type.
 */

package com.jcraft.jsch;

public enum ChannelType {
    SESSION("session"),
    SHELL("shell"),
    EXEC("exec"),
    X11("x11"),
    AUTH_AGENT_OPENSSH("auth-agent@openssh.com"),
    DIRECT_TCPIP("direct-tcpip"),
    FORWARDED_TCPIP("forwarded-tcpip"),
    SFTP("sftp"),
    SUBSYSTEM("subsystem");
    private String type;

    ChannelType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

}
