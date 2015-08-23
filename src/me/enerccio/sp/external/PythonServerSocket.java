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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.net.ServerSocketFactory;

import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.sandbox.SecureAction;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.pointer.WrapAnnotationFactory.WrapMethod;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Coerce;
import me.enerccio.sp.utils.Utils;

public class PythonServerSocket implements Closeable {

	private ServerSocket socket;
	private ClassObject errorType;
	private ClassObject timeoutType;

	public PythonServerSocket(ClassObject errorType, ClassObject timeoutType,
			String addr, int port, int backlog) {
		PythonRuntime.runtime.checkSandboxAction("socket", SecureAction.SOCKET,
				addr, port);
		PythonRuntime.runtime.checkSandboxAction("socket",
				SecureAction.SOCKET_SERVER, addr, port);

		this.errorType = errorType;
		this.timeoutType = timeoutType;
		try {
			socket = ServerSocketFactory.getDefault().createServerSocket(port,
					backlog, InetAddress.getByName(addr));
		} catch (Exception e) {
			throw Utils.throwException(errorType,
					"failed to open server socket", e);
		}
	}

	@WrapMethod
	@Override
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			throw Utils.throwException(errorType,
					"failed to close server socket", e);
		}
	}

	@WrapMethod
	public void timeout(int timeout) {
		try {
			socket.setSoTimeout(timeout);
		} catch (Exception e) {
			throw Utils.throwException(errorType,
					"failed to set server socket timeout", e);
		}
	}

	@WrapMethod
	public PythonObject accept() {
		PythonObject[] result = new PythonObject[2];
		Socket s;
		try {
			s = socket.accept();
		} catch (SocketTimeoutException e) {
			throw Utils.throwException(timeoutType, "timeout", e);
		} catch (IOException e) {
			throw Utils.throwException(errorType,
					"server socket accept() failure", e);
		}
		result[0] = Coerce.toPython(s);
		result[1] = new StringObject(s.getLocalAddress().getHostAddress());
		return new TupleObject(false, result);
	}

}
