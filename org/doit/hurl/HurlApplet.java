/* HurlApplet.java */

/*
 * Copyright (C) 1998 Mark Boyns <boyns@sdsu.edu>
 *
 * This file is part of Hurl.
 *
 * Hurl is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Muffin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Muffin; see the file COPYING.  If not, write to the
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
package org.doit.hurl;

import java.net.*;
import java.awt.*;
import java.util.*;
import java.io.*;

public class HurlApplet extends java.applet.Applet implements Runnable
{
    final String version = "0.0a";

    Thread thread = null;
    HurlAppletFrame frame = null;
    Socket socket = null;

    public void init ()
    {
	thread = new Thread (this);
	thread.start ();
    }

    public void run ()
    {
	BufferedReader in = null;
	
	try
	{
	    String server = getParameter ("HurlServer");
	    int port = Integer.parseInt (getParameter ("HurlPort"));
	    socket = new Socket (server, port);
	    in = new BufferedReader (new InputStreamReader (socket.getInputStream ()));
	}
	catch (Exception e)
	{
	    reallyStop ();
	}

	frame = new HurlAppletFrame (this);

	for (;;)
	{
	    String line = null;
	    
	    try
	    {
		line = in.readLine ();
	    }
	    catch (Exception e)
	    {
		System.out.println (e);
		break;
	    }
	    
	    StringTokenizer st = new StringTokenizer (line);
	    String command = st.nextToken ();

	    if (command.equals ("HURL"))
	    {
		String arg = st.nextToken ();
		try
		{
		    URL url = new URL (arg);
		    getAppletContext ().showDocument (url);
		}
		catch (Exception e)
		{
		    System.out.println (e);
		}
	    }
	    else if (command.equals ("QUIT"))
	    {
		break;
	    }
	}

	reallyStop ();
    }

    public boolean action (Event e, Object arg)
    {
	System.out.println (e);
	
        if (e.target instanceof Button)
        {
            if ("Quit".equals (arg))
            {
		reallyStop ();
	    }
	}
	return false;
    }

    void reallyStop ()
    {
	if (frame != null)
	{
	    frame.dispose ();
	}
	if (thread != null)
	{
	    thread.stop ();
	}
	if (socket != null)
	{
	    try
	    {
		socket.close ();
	    }
	    catch (Exception e)
	    {
	    }
	}
	destroy ();
    }
}
