/*
 * Copyright (C) 2014 Tobias Brunner
 * HSR Hochschule fuer Technik Rapperswil
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.  See <http://www.fsf.org/copyleft/gpl.txt>.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 */

package com.gardion.android.family.client.logic;

import java.security.Security;

import com.gardion.android.family.client.security.LocalCertificateKeyStoreProvider;

import android.app.Application;
import android.content.Context;

public class GardionApplication extends Application
{
	private static Context mContext;

	static {
		Security.addProvider(new LocalCertificateKeyStoreProvider());
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		GardionApplication.mContext = getApplicationContext();
	}

	/**
	 * Returns the current application context
	 * @return context
	 */
	public static Context getContext()
	{
		return GardionApplication.mContext;
	}

	/*
	 * The libraries are extracted to /data/data/org.strongswan.android/...
	 * during installation.  On newer releases most are loaded in JNI_OnLoad.
	 */
	static
	{
		System.loadLibrary("androidbridge");
	}
}
