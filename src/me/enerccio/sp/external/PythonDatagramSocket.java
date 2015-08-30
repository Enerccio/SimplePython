/*
 * SimplePython - embeddable python interpret in java
 * Copyright (c) Peter Vanusanik, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package me.enerccio.sp.external;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.sandbox.SecureAction;
import me.enerccio.sp.serialization.PySerializer;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.pointer.WrapAnnotationFactory.WrapMethod;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Coerce;
import me.enerccio.sp.utils.Utils;

public class PythonDatagramSocket implements Closeable, Externalizable {

	private DatagramSocket socket;
	private ClassObject errorType;
	private ClassObject timeoutType;

	public PythonDatagramSocket(ClassObject errorType, ClassObject timeoutType) {
		PythonRuntime.runtime.checkSandboxAction("socket", SecureAction.SOCKET);
		PythonRuntime.runtime.checkSandboxAction("socket",
				SecureAction.SOCKET_SERVER);

		this.errorType = errorType;
		this.timeoutType = timeoutType;
		try {
			socket = new DatagramSocket();
		} catch (Exception e) {
			throw Utils.throwException(errorType, "failed to open socket", e);
		}
	}
	

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		PySerializer s = PythonRuntime.activeSerializer;
		s.serialize(errorType);
		s.serialize(timeoutType);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		// TODO Auto-generated method stub
		
	}

	@WrapMethod
	@Override
	public void close() {
		try {
			socket.close();
		} catch (Exception e) {
			throw Utils.throwException(errorType, "failed to close socket", e);
		}
	}

	@WrapMethod
	public void timeout(int timeout) {
		try {
			socket.setSoTimeout(timeout);
		} catch (Exception e) {
			throw Utils.throwException(errorType,
					"failed to set socket timeout", e);
		}
	}

	@WrapMethod
	public void bindTo(String addr, int port) {
		try {
			socket.close();
			socket = new DatagramSocket(port, InetAddress.getByName(addr));
		} catch (Exception e) {
			throw Utils.throwException(errorType,
					"failed to set server socket timeout", e);
		}
	}

	@WrapMethod
	public void sendto(byte[] message, TupleObject to) {
		String addr = Coerce.argument(to, 0, "sendto", String.class);
		int port = Coerce.argument(to, 1, "sendto", int.class);
		DatagramPacket sendPacket;
		try {
			sendPacket = new DatagramPacket(message, message.length,
					InetAddress.getByName(addr), port);
		} catch (UnknownHostException e) {
			throw Utils.throwException(errorType, "failed to get address", e);
		}
		try {
			socket.send(sendPacket);
		} catch (IOException e) {
			throw Utils.throwException(errorType, "failed to send message", e);
		}
	}

	@WrapMethod
	public PythonObject recv(int bufs) {
		DatagramPacket dp = new DatagramPacket(new byte[bufs], bufs);
		try {
			socket.receive(dp);
		} catch (SocketTimeoutException e) {
			throw Utils.throwException(timeoutType, "timeout", e);
		} catch (IOException e) {
			throw Utils.throwException(errorType, "failed to recv message", e);
		}
		return new TupleObject(false, Coerce.toPython(
				Arrays.copyOfRange(dp.getData(), 0, dp.getLength()),
				byte[].class), new StringObject(dp.getAddress()
				.getHostAddress()));
	}
}
