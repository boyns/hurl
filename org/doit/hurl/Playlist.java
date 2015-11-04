/* Playlist.java */

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
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.Reader;
import java.util.*;

class Playlist extends Frame implements Runnable, ActionListener, WindowListener
{
    Hurl parent = null;
    TextField input = null;
    TextArea text = null;
    Thread thread = null;
    Button playButton = null;
    Button stopButton = null;
    Button closeButton = null;
    Button pauseButton = null;
    Button continueButton = null;
    
    Playlist (Hurl parent)
    {
	super ("Hurl: Playlist");

	this.parent = parent;

	Panel panel = new Panel ();
	GridBagLayout layout = new GridBagLayout ();
	panel.setLayout (layout);
	GridBagConstraints c;

	Label l;
	l = new Label ("File:", Label.RIGHT);
	l.setFont (new Font ("Helvetica", Font.BOLD, 12));
	panel.add (l);
	
	input = new TextField (40);
	input.setText ("");
	panel.add (input);

	Button browse = new Button ("Browse...");
	browse.setFont (new Font ("Helvetica", Font.BOLD, 12));
	browse.setActionCommand ("doBrowse");
	browse.addActionListener (this);
	c = new GridBagConstraints ();
	c.anchor = GridBagConstraints.NORTHWEST;
	layout.setConstraints (browse, c);
	panel.add (browse);

	add ("North", panel);

	panel = new Panel ();
	layout = new GridBagLayout ();
	panel.setLayout (layout);

	l = new Label ("Playlist");
	l.setFont (new Font ("Helvetica", Font.BOLD, 12));
	c = new GridBagConstraints ();
	c.insets = new Insets (0, 10, 5, 10);
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.anchor = GridBagConstraints.NORTHWEST;
	layout.setConstraints (l, c);
	panel.add (l);
	
	text = new TextArea ();
	c = new GridBagConstraints ();
	c.gridheight = 2;
	c.insets = new Insets (0, 10, 5, 10);
	layout.setConstraints (text, c);
	panel.add (text);

	Button b;

	b = new Button ("Load");
	b.setFont (new Font ("Helvetica", Font.BOLD, 12));
	b.setActionCommand ("doLoad");
	b.addActionListener (this);
	c = new GridBagConstraints ();
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.anchor = GridBagConstraints.NORTHWEST;
	layout.setConstraints (b, c);
	panel.add (b);

	b = new Button ("Save");
	b.setFont (new Font ("Helvetica", Font.BOLD, 12));
	b.setActionCommand ("doSave");
	b.addActionListener (this);
	c = new GridBagConstraints ();
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.anchor = GridBagConstraints.NORTHWEST;
	layout.setConstraints (b, c);
	panel.add (b);

	add ("Center", panel);

	Panel buttonPanel = new Panel ();
	buttonPanel.setLayout (new GridLayout (1, 5));
        playButton = new Button ("Play");
	playButton.setFont (new Font ("Helvetica", Font.BOLD, 12));
	playButton.setActionCommand ("doPlay");
	playButton.addActionListener (this);
	buttonPanel.add (playButton);

        pauseButton = new Button ("Pause");
	pauseButton.setFont (new Font ("Helvetica", Font.BOLD, 12));
	pauseButton.setActionCommand ("doPause");
	pauseButton.addActionListener (this);
	pauseButton.setEnabled (false);
	buttonPanel.add (pauseButton);

        continueButton = new Button ("Continue");
	continueButton.setFont (new Font ("Helvetica", Font.BOLD, 12));
	continueButton.setActionCommand ("doContinue");
	continueButton.addActionListener (this);
	continueButton.setEnabled (false);
	buttonPanel.add (continueButton);

	stopButton = new Button ("Stop");
	stopButton.setFont (new Font ("Helvetica", Font.BOLD, 12));
	stopButton.setActionCommand ("doStop");
	stopButton.addActionListener (this);
	stopButton.setEnabled (false);
	buttonPanel.add (stopButton);

	closeButton = new Button ("Close");
	closeButton.setFont (new Font ("Helvetica", Font.BOLD, 12));
	closeButton.setActionCommand ("doClose");
	closeButton.addActionListener (this);
	buttonPanel.add (closeButton);
	add ("South", buttonPanel);

	addWindowListener (this);
	
	pack ();
	setSize (getPreferredSize ());

	show ();
    }

    void doplay ()
    {
	thread = new Thread (this);
	thread.start ();
	playButton.setEnabled (false);
	stopButton.setEnabled (true);
	closeButton.setEnabled (false);
	pauseButton.setEnabled (true);
    }

    void dostop ()
    {
	playButton.setEnabled (true);
	stopButton.setEnabled (false);
	closeButton.setEnabled (true);
	pauseButton.setEnabled (false);
	continueButton.setEnabled (false);
	thread.stop ();
    }

    void dopause ()
    {
	continueButton.setEnabled (true);
	pauseButton.setEnabled (false);
	thread.suspend ();
    }

    void docontinue ()
    {
	continueButton.setEnabled (false);
	pauseButton.setEnabled (true);
	thread.resume ();
    }

    public void run ()
    {
	try
	{
	    BufferedReader in = new BufferedReader (new StringReader (text.getText ()));
	    String line;
	    int offset = 0;
	    int pos = text.getCaretPosition ();
	    while ((line = in.readLine ()) != null)
	    {
		text.setSelectionStart (offset);
		text.setSelectionEnd (offset + line.length ());
		offset += line.length () + System.getProperty ("line.separator").length ();

		if (pos > text.getSelectionEnd ())
		{
		    continue;
		}

		//System.out.println (line);

		try
		{
		    StringTokenizer st = new StringTokenizer (line);
		    String command = st.nextToken ();
		    if (command.equals ("HURL"))
		    {
			parent.hurl (st.nextToken ().trim ());
		    }
		    else if (command.equals ("SLEEP"))
		    {
			try
			{
			    int seconds = Integer.parseInt (st.nextToken ().trim ());
			    Thread.sleep (seconds * 1000);
			}
			catch (Exception e)
			{
			}
		    }
		    else if (command.equals ("PAUSE"))
		    {
			dopause ();
		    }
		    else if (command.equals ("STOP"))
		    {
			break;
		    }
		}
		catch (NoSuchElementException nse)
		{
		}
	    }
	    in.close ();
	    text.select (0, 0);
	}
	catch (Exception e)
	{
	    System.out.println (e);
	}

	dostop ();
    }

    void loadFile (String filename)
    {
	text.setText ("");
	
	File file = new File (filename);
	if (!file.exists ())
	{
	    return;
	}
	
	try
	{
	    BufferedReader in = new BufferedReader (new FileReader (file));
	    String s;
	    while ((s = in.readLine ()) != null)
	    {
		text.append (s + "\n");
	    }
	    in.close ();
	    text.setCaretPosition (0);
	}
	catch (Exception e)
	{
	    System.out.println (e);
	}
    }

    void saveFile (String filename)
    {
	try
	{
	    File file = new File (filename);
	    if (file.exists ())
	    {
		file.delete ();
	    }
	    FileWriter writer = new FileWriter (file);
	    writer.write (text.getText ());
	    writer.close ();
	}
	catch (Exception e)
	{
	    System.out.println (e);
	}
    }

    public void actionPerformed (ActionEvent event)
    {
	String arg = event.getActionCommand ();
	
	if ("doSave".equals (arg))
	{
	    saveFile (input.getText ().trim ());
	}
	else if ("doLoad".equals (arg))
	{
	    loadFile (input.getText ().trim ());
	}
	else if ("doPlay".equals (arg))
	{
	    doplay ();
	}
	else if ("doStop".equals (arg))
	{
	    dostop ();
	}
	else if ("doPause".equals (arg))
	{
	    dopause ();
	}
	else if ("doContinue".equals (arg))
	{
	    docontinue ();
	}
	else if ("doClose".equals (arg))
	{
	    setVisible (false);
	    dispose ();
	}
	else if ("doBrowse".equals (arg))
	{
	    FileDialog dialog = new FileDialog (this, "Playlist Load");
	    dialog.show ();
	    if (dialog.getFile () != null)
	    {
		input.setText (dialog.getDirectory () + dialog.getFile ());
		loadFile (input.getText ());
	    }
	}
    }

    public void windowActivated (WindowEvent e)
    {
    }
  
    public void windowDeactivated (WindowEvent e)
    {
    }
  
    public void windowClosing (WindowEvent e)
    {
	setVisible (false);
	dispose ();
    }
  
    public void windowClosed (WindowEvent e)
    {
    }
  
    public void windowIconified (WindowEvent e)
    {
    }
  
    public void windowDeiconified (WindowEvent e)
    {
    }
  
    public void windowOpened (WindowEvent e)
    {
    }
}
