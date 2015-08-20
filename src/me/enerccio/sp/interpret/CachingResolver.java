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
package me.enerccio.sp.interpret;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import me.enerccio.sp.runtime.ModuleProvider;
import me.enerccio.sp.utils.StaticTools.IOUtils;

public abstract class CachingResolver implements PythonDataSourceResolver {

	public static File rootCachePath = getCachePath("__spy_cache__");
	
	private static File getCachePath(String cacheName){
		
		String OS = (System.getProperty("os.name")).toUpperCase();
		String workingDirectory;
		//to determine what the workingDirectory is.
		//if it is some version of Windows
		if (OS.contains("WIN"))
		{
		    //it is simply the location of the "AppData" folder
		    workingDirectory = System.getenv("AppData");
		}
		//Otherwise, we assume Linux or Mac
		else
		{
		    //in either case, we would start in the user's home directory
		    workingDirectory = System.getProperty("user.home");
		    //if we are on a Mac, we are not done, we look for "Application Support"
		    workingDirectory += "/Library/Application Support";
		}
		File f = new File(new File(workingDirectory), cacheName);
		f.mkdirs();
		return f;
	}
	
	protected ModuleProvider resolveWithCache(String moduleName, String fileName, String resolvePath, boolean isPackage, byte[] originalData, String resolverId){
		
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
			return new ModuleProvider(moduleName, null, fileName, resolvePath, isPackage,
					false, false, null, null);
		}
		md.update(moduleName.getBytes());
		md.update(fileName.getBytes());
		md.update(resolvePath.getBytes());
		md.update(resolverId.getBytes());
		
		StringBuffer hexString = new StringBuffer();
		byte[] hash = md.digest();

        for (int i = 0; i < hash.length; i++) {
            if ((0xff & hash[i]) < 0x10) {
                hexString.append("0"
                        + Integer.toHexString((0xFF & hash[i])));
            } else {
                hexString.append(Integer.toHexString(0xFF & hash[i]));
            }
        }
        
        String pycName = hexString.toString()+".pyc";
        File cacheFile = new File(rootCachePath, pycName);
        
        try {
	        if (!cacheFile.exists()){
	        	cacheFile.createNewFile();
	        	return new ModuleProvider(moduleName, originalData, fileName, resolvePath, isPackage,
						false, true, new FileOutputStream(cacheFile), null);
	        } else {
	        	byte[] cache = IOUtils.toByteArray(new FileInputStream(cacheFile));
	        	return new ModuleProvider(moduleName, originalData, fileName, resolvePath, isPackage,
						true, true, new FileOutputStream(cacheFile), cache);
	        }
        } catch (Exception e){
        	return new ModuleProvider(moduleName, originalData, fileName, resolvePath, isPackage,
    						false, false, null, null);
        }
	}
}
