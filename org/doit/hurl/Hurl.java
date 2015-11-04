/* Hurl.java */

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
import java.awt.event.*;
import java.util.*;
import java.io.*;
import gnu.getopt.*;

public class Hurl extends Frame implements ActionListener
{
    final String defaultUrl = "http://www.";
    static int httpPort = 9000;
    static int hurlPort = 9001;
    static String hurlServer = null;
    static String startUrl = null;
    static int startUrlDelay = 15;

    Vector clients = null;
    TextField input = null;
    List list = null;

    Hurl ()
    {
	super ("Hurl");

	GridBagLayout layout = new GridBagLayout ();
	setLayout (layout);

	MenuBar bar = new MenuBar ();
	Menu menu = new Menu ("File");
	menu.setFont (new Font ("Helvetica", Font.BOLD, 12));
	MenuItem item;
	item = new MenuItem ("Playlist...");
	item.setActionCommand ("doPlaylist");
	item.addActionListener (this);
	menu.add (item);
	item = new MenuItem ("Quit");
	item.setActionCommand ("doQuit");
	item.addActionListener (this);
	menu.add (item);
	bar.add (menu);

	menu = new Menu ("Help");
	menu.setFont (new Font ("Helvetica", Font.BOLD, 12));
	item = new MenuItem ("About Hurl...");
	item.setActionCommand ("doAbout");
	item.addActionListener (this);
	menu.add (item);
	item = new MenuItem ("License...");
	item.setActionCommand ("doLicense");
	item.addActionListener (this);
	menu.add (item);
	bar.setHelpMenu (menu);
	
	setMenuBar (bar);

	GridBagConstraints c;
	Panel panel;
	Button b;
	Label label;

	panel = new Panel ();
	c = new GridBagConstraints ();
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.anchor = GridBagConstraints.NORTHWEST;
	c.weightx = 1.0;
	layout.setConstraints (panel, c);

	label = new Label ("URL:", Label.RIGHT);
	label.setFont (new Font ("TimesRoman", Font.BOLD, 12));
	panel.add (label);
	    
	input = new TextField (50);
	input.setText (defaultUrl);
	input.addActionListener (this);
	panel.add (input);

	b = new Button ("Clear");
	b.addActionListener (this);
	b.setFont (new Font ("TimesRoman", Font.BOLD, 12));
	panel.add (b);

	add (panel);

	label = new Label ("Clients");
	label.setFont (new Font ("TimesRoman", Font.BOLD, 12));
	c = new GridBagConstraints ();
	c.anchor = GridBagConstraints.WEST;
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.insets = new Insets (0, 5, 5, 5);
	layout.setConstraints (label, c);
	add (label);

	list = new List (5, true);
	c = new GridBagConstraints ();
	c.fill = GridBagConstraints.HORIZONTAL;
	c.anchor = GridBagConstraints.NORTHWEST;
 	c.gridwidth = GridBagConstraints.REMAINDER;
	c.weightx = 1.0;
	c.insets = new Insets (0, 5, 5, 5);
	layout.setConstraints (list, c);
	add (list);

// 	ImageCanvas logo = new ImageCanvas ("hurl-small.jpg", null, true);
// 	c = new GridBagConstraints ();
// 	c.fill = GridBagConstraints.HORIZONTAL;
// 	c.gridwidth = GridBagConstraints.REMAINDER;
// 	c.insets = new Insets (0, 5, 5, 5);
// 	layout.setConstraints (logo, c);
// 	add (logo);

	pack ();
	show ();
	
	input.requestFocus ();
	
	clients = new Vector ();
	new Thread (new Server (httpPort)).start ();
	new Thread (new Server (hurlPort)).start ();
    }

    public void actionPerformed (ActionEvent event)
    {
        String arg = event.getActionCommand ();
        if ("Clear".equals (arg))
        {
	    input.setText (defaultUrl);
        }
        else if ("doPlaylist".equals (arg))
        {
	    new Playlist (this);
        }
        else if ("doQuit".equals (arg))
        {
	    System.exit (0);
        }
        else if ("doAbout".equals (arg))
        {
        }
        else if ("doLicense".equals (arg))
        {
        }
	else
	{
            hurl (input.getText ().trim ());
	}
    }

    void updateList ()
    {
	if (list.getItemCount () > 0)
	{
	    list.removeAll ();
	}
	Enumeration enum = clients.elements ();
	while (enum.hasMoreElements ())
	{
	    Client c = (Client) enum.nextElement ();
	    list.addItem (c.toString ());
	}
    }

    void hurl (String url)
    {
	StringBuffer buf = new StringBuffer ();
	buf.append ("HURL " + url + "\r\n");

	Enumeration enum = clients.elements ();
	while (enum.hasMoreElements ())
	{
	    Client c = (Client) enum.nextElement ();
	    try
	    {
		OutputStream out = c.socket.getOutputStream ();
		out.write (buf.toString ().getBytes ());
		out.flush ();
	    }
	    catch (Exception e)
	    {
		clients.removeElement (c);
		updateList ();
	    }
	}
    }

    class Client
    {
	Socket socket = null;
	boolean selected = false;

	Client (Socket s)
	{
	    socket = s;
	}

	public String toString ()
	{
	    return socket.getInetAddress ().toString ();
	}
    }

    class Server implements Runnable
    {
	int port;
	
	Server (int port)
	{
	    this.port = port;
	}

	public void run ()
	{
	    ServerSocket serv ;
	    try
	    {
		serv = new ServerSocket (port, 512);
	    }
	    catch (Exception ex)
	    {
		System.out.println (ex);
		return;
	    }
	    for (;;)
	    {
		Socket socket;
		try
		{
		    socket = serv.accept ();
		    if (port == httpPort)
		    {
			new Thread (new Handler (socket)).start ();
		    }
		    else if (port == hurlPort)
		    {
			clients.addElement (new Client (socket));
			updateList ();
		    }
		}
		catch (Exception e)
		{
		    System.out.println (e);
		}
	    }
	}
    }
    
    class Handler implements Runnable
    {
	Socket socket = null;

	Handler (Socket socket)
	{
	    this.socket = socket;
	}

	void copy (InputStream in, OutputStream out) throws IOException
	{
	    byte buffer[] = new byte[8192];
	    for (;;)
	    {
		int n = in.read (buffer, 0, buffer.length);
		if (n <= 0)
		{
		    break;
		}
		out.write (buffer, 0, n);
	    }
	    out.flush ();
	}

	public void run ()
	{
	    try
	    {
		BufferedReader in = new BufferedReader (new InputStreamReader (socket.getInputStream ()));
		String line = in.readLine ();
		StringTokenizer st = new StringTokenizer (line);
		String command = st.nextToken ();
		String url = st.nextToken ();
		//String proto = st.nextToken ();

		if (command.equals ("GET"))
		{
		    OutputStream out = socket.getOutputStream ();
		    StringBuffer buf;

		    if (url.equals ("/") || url.equals ("/index.html"))
		    {
			buf = new StringBuffer ();
		        buf.append ("HTTP/1.0 200 Ok\r\n");
		        buf.append ("Server: HURL/0.0\r\n");
		        buf.append ("Content-type: text/html\r\n");
		        buf.append ("\r\n");
			buf.append ("<head>\n");
			if (startUrl != null)
			{
			    buf.append ("<META HTTP-EQUIV=REFRESH CONTENT=\"" + startUrlDelay + "; URL=" + startUrl + "\">\n");
			}
			buf.append ("<title>HURL</title>\n");
			buf.append ("</head>\n");
			buf.append ("<body bgcolor=#ffffff text=#000000>\n");
			buf.append ("<img src=hurl.jpg>\n");
			buf.append ("<applet archive=applet.zip code=org.doit.hurl.HurlApplet.class width=1 height=1>\n");
			buf.append ("<param name=HurlServer value=" + hurlServer + ">\n");
			buf.append ("<param name=HurlPort value=" + hurlPort + ">\n");
			buf.append ("</applet>");
			buf.append ("<br>by Mark R. Boyns<p>\n");
			buf.append ("A HURL applet window will appear on your desktop.  While this applet is running.");
			buf.append ("your web browser will receive and display URLs from a remote server.");
			buf.append ("<ul><li>Press the Quit button to exit the applet.\n");
			buf.append ("<li>If the applet disappears, reload this page..\n</ul>\n");
			buf.append ("<p>More HURL information can be found at <a href=http://hurl.doit.org>hurl.doit.org</a>.");
			if (startUrl != null)
			{
			    buf.append ("<p>Document " + startUrl + " will be loaded in " + startUrlDelay + " seconds....");
			}
			buf.append ("</body>\n");
			out.write (buf.toString ().getBytes ());
		    }
		    else if (url.equals ("/hurl.jpg") || url.equals ("/hurl-small.jpg"))
		    {
			buf = new StringBuffer ();
		        buf.append ("HTTP/1.0 200 Ok\r\n");
		        buf.append ("Server: HURL/0.0\r\n");
		        buf.append ("Content-type: image/jpeg\r\n");
		        buf.append ("\r\n");
			out.write (buf.toString ().getBytes ());
			copy (getClass ().getResourceAsStream (url.substring (1)), out);
		    }
		    else if (url.equals ("/applet.zip"))
		    {
			buf = new StringBuffer ();
		        buf.append ("HTTP/1.0 200 Ok\r\n");
		        buf.append ("Server: HURL/0.0\r\n");
		        buf.append ("Content-type: application/zip\r\n");
		        buf.append ("\r\n");
			out.write (buf.toString ().getBytes ());
			copy (getClass ().getResourceAsStream (url.substring (1)), out);
		    }
		    else
		    {
			buf = new StringBuffer ();
		        buf.append ("HTTP/1.0 404 Not found\r\n");
		        buf.append ("Server: HURL/0.0\r\n");
		        buf.append ("Content-type: text/html\r\n");
		        buf.append ("\r\n");
			buf.append ("File not found.\n");
			out.write (buf.toString ().getBytes ());
		    }
		    out.flush ();
		    out.close ();
		}
		else if (command.equals ("HURL"))
		{
		    hurl (url);
		}
	    }
	    catch (Exception e)
	    {
		System.out.println (e);
		e.printStackTrace ();
	    }

	    try
	    {
		socket.close ();
	    }
	    catch (Exception e)
	    {
	    }
	}
    }

    public static void main (String args[])
    {
	try
	{
	    hurlServer = InetAddress.getLocalHost ().getHostName ();
	}
	catch (UnknownHostException e)
	{
	    System.out.println (e);
	    System.exit (1);
	}
	
	LongOpt longopts[] = new LongOpt[6];
	longopts[0] = new LongOpt ("httpPort", LongOpt.REQUIRED_ARGUMENT, null, 2);
	longopts[1] = new LongOpt ("hurlPort", LongOpt.REQUIRED_ARGUMENT, null, 3);
	longopts[2] = new LongOpt ("hurlServer", LongOpt.REQUIRED_ARGUMENT, null, 4);
	longopts[3] = new LongOpt ("help", LongOpt.NO_ARGUMENT, null, 'h');
	longopts[4] = new LongOpt ("version", LongOpt.NO_ARGUMENT, null, 'v');
	longopts[5] = new LongOpt ("url", LongOpt.REQUIRED_ARGUMENT, null, 5);

	Getopt g = new Getopt ("Hurl", args, "v", longopts, true);
	int c;
	while ((c = g.getopt ()) != -1)
	{
	    switch (c)
	    {
	    case 0:
		/* do nothing */
		break;

	    case 2:
		try
		{
		    httpPort = Integer.parseInt (g.getOptarg ());
		}
		catch (Exception e)
		{
		    System.out.println ("invalid httpPort: " + g.getOptarg ());
		    System.exit (1);
		}
		break;
		
	    case 3:
		try
		{
		    hurlPort = Integer.parseInt (g.getOptarg ());
		}
		catch (Exception e)
		{
		    System.out.println ("invalid hurlPort: " + g.getOptarg ());
		    System.exit (1);
		}
		break;

	    case 4:
		hurlServer = g.getOptarg ();
		break;

	    case 5:
		startUrl = g.getOptarg ();
		break;

	    case 'v': /* --version */
		System.exit (0);
		break;
		
	    case 'h': /* --help */
		System.out.println ("usage: java org.doit.hurl.Hurl [options]\n\n"
				    + "-help                 This useful message.\n"
				    + "-httpPort PORT        Use PORT as the HTTP port.\n"
				    + "-hurlPort PORT        Use PORT as the HURL port.\n"
				    + "-hurlServer HOST      Use HOST as the HURL server hostname.\n"
				    + "-url URL              Use URL default client start page.\n"
				    + "-v                    Display Hurl version.\n");
		System.exit (0);
		break;

	    case '?':
		System.exit (1);
		
	    default:
		break;
	    }
	}
	
	if (hurlServer.equals ("localhost"))
	{
	    System.out.println ("WARNING: hurlServer is set to \"" + hurlServer + "\"; use --hurlServer=HOST to fix");
	}
	
	new Hurl ();
    }
}
