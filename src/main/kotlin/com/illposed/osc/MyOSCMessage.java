package com.illposed.osc;

import java.util.List;

public class MyOSCMessage extends OSCMessage {

    public MyOSCMessage(String address, List<?> arguments, OSCMessageInfo info) {
        super(address, arguments, info, false);
    }
}
