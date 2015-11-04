#!/usr/bin/perl
#
# Copyright (C) 1998 Mark Boyns <boyns@sdsu.edu>
#
# This file is part of Hurl.
#
# Hurl is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# Muffin is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with Muffin; see the file COPYING.  If not, write to the
# Free Software Foundation, Inc.,
# 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.


use Socket;

$hurl_server = "doctor.sdsu.edu";
$hurl_port = 9000;

hurl_connect ($hurl_server, $hurl_port);
print HURL "HURL " . $ARGV[0] . "\n";
hurl_close ();
exit 0;

sub getaddress
{
    local ($host) = @_;
    local (@h);

    @h = gethostbyname ($host);
    return unpack ("C4", $h[4]);
}

sub hurl_connect
{
    local ($host, $port) = @_;
    ($sockaddr, $there, $response, $tries) = ("Snc4x8");
    $there = pack ($sockaddr, 2, $port, &getaddress ($host));
    $proto = (getprotobyname ('tcp'))[2];
    socket (HURL, &AF_INET, &SOCK_STREAM, $proto) || die "$0: $!";
    connect (HURL, $there) || die "$0: $!";
    select (HURL);
    $| = 1;
    select (STDOUT);
}

sub hurl_close
{
    close HURL;
}
