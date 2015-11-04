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

import java.awt.*;

public class HurlAppletFrame extends Frame
{
    HurlApplet applet = null;
    
    HurlAppletFrame (HurlApplet applet)
    {
	super ("Hurl");
	this.applet = applet;

	MediaTracker tracker = new MediaTracker (this);
	Image image = applet.getImage (applet.getDocumentBase (), "hurl-small.jpg");
	tracker.addImage (image, 1);
	tracker.checkAll (true);
	try
        {
            tracker.waitForAll ();
        }
        catch (Exception e)
        {
        }

	setLayout (new BorderLayout ());
	add ("Center", new ImageCanvas (image));
	add ("South", new Button ("Quit"));
	pack ();
	show ();
    }
    
    public boolean action (Event e, Object arg)
    {
        if (e.target instanceof Button)
        {
            if ("Quit".equals (arg))
            {
		applet.reallyStop ();
	    }
	}
	return false;
    }
}
