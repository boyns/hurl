#!/bin/sh

javac org/doit/hurl/*.java
rm -f org/doit/hurl/applet.zip
zip org/doit/hurl/applet.zip org/doit/hurl/{HurlApplet*,ImageCanvas}.class
zip -r hurl.zip org gnu
