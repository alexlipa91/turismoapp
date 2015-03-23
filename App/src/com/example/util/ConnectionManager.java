package com.example.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class ConnectionManager {

	public static InputStream getDataMainFile( Context c ) throws IOException {		
		//Get the text file
		return c.getAssets().open("elements") ;
	}	


}
