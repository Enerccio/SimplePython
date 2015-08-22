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

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;
import edu.stanford.ejalbert.exceptionhandler.BrowserLauncherErrorHandler;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.sandbox.SecureAction;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.pointer.WrapAnnotationFactory.WrapMethod;
import me.enerccio.sp.utils.Utils;

public class WebbrowserController {
	
	private enum OpenType {
		OPEN_DEFAULT, OPEN_NEW, OPEN_NEW_TAB
	}

	private ClassObject error;
	private BrowserLauncher launcher;
	private String name;
	
	public WebbrowserController(String name, ClassObject error){
		PythonRuntime.runtime.checkSandboxAction("webbrowser", SecureAction.WEBBROWSER);
		this.error = error;
		this.name = name;
		try {
			this.launcher = new BrowserLauncher(null, new BrowserLauncherErrorHandler() {
				
				@Override
				public void handleException(Exception arg0) {
					throw Utils.throwException(WebbrowserController.this.error, "failure to open page", arg0);
				}
			});
		} catch (BrowserLaunchingInitializingException e) {
			throw Utils.throwException(error, "failure to create browser handler", e);
		} catch (UnsupportedOperatingSystemException e) {
			throw Utils.throwException(error, "failure to create browser handler", e);
		}
	}
	
	@WrapMethod
	public void open(String url, OpenType open, boolean autoraise){
		synchronized (launcher){
			if (open == OpenType.OPEN_DEFAULT){
				launcher.setNewWindowPolicy(false);
			} else {
				launcher.setNewWindowPolicy(true);
			}
			if (name == null){
				launcher.openURLinBrowser(url);
			} else {
				launcher.openURLinBrowser(name, url);
			}
		}
	}
}
