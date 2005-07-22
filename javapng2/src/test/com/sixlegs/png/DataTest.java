/*
com.sixlegs.image.png - Java package to read and display PNG images
Copyright (C) 1998-2005 Chris Nokleberg

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*/

package com.sixlegs.png;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.*;
import java.io.*;
import java.security.MessageDigest;
import junit.framework.*;

public class DataTest
extends PngTestCase
{
    public void testImages()
    throws Exception
    {
        BufferedImage argb = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        Raster raster = argb.getRaster();
        int[] data = new int[800 * 600];
        byte[] pixbuf = new byte[4];

        ExtendedPngConfig config = new ExtendedPngConfig();
        config.setWarningsFatal(true);
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BufferedReader r = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/images.txt")));
        boolean fail = false;
        String line;
        while ((line = r.readLine()) != null) {
            line = line.trim();
            if (line.equals("") || line.startsWith("#"))
                continue;
            int space = line.indexOf(' ');
            String name = line.substring(0, space).trim();
            String result = line.substring(space + 1).trim();
            try {
                InputStream in = getClass().getResourceAsStream(name);
                if (in == null)
                    fail("Cannot find image \"" + name + "\"");
                BufferedImage image = new PngImage(config).read(in, true);
                Graphics2D g = argb.createGraphics();
                g.setPaint(new Color(255, 255, 255, 0));
                g.fillRect(0, 0, argb.getWidth(), argb.getHeight());
                g.drawImage(image, null, 0, 0);
                raster.getDataElements(0, 0, image.getWidth(), image.getHeight(), data);
                for (int i = 0, y = 0, h = image.getHeight(); y < h; y++) {
                    for (int x = 0, w = image.getWidth(); x < w; x++, i++) {
                        int pixel = data[i];
                        pixbuf[3] = (byte)(0xFF & pixel);
                        pixbuf[2] = (byte)(0xFF & (pixel >>> 8));
                        pixbuf[1] = (byte)(0xFF & (pixel >>> 16));
                        pixbuf[0] = (byte)(0xFF & (pixel >>> 24));
//                         if (i < 10)
//                             System.err.println("pixel " + i + " = [" + (0xFF&pixbuf[1]) + "," + (0xFF&pixbuf[2]) + "," + (0xFF&pixbuf[3]) + "," + (0xFF&pixbuf[0]) + "]");
                        md5.update(pixbuf);
                    }
                }
                String hash = toHexString(md5.digest());
                if (!result.equals(hash)) {
                    System.err.println("Expected digest 0x" + result + " for image " + name + ", got 0x" + hash);
                    fail = true;
                }
             } catch (Exception e) {
                 if (!result.equals(e.getMessage())) {
                     System.err.println("Caught exception while processing image " + name + ":");
                     e.printStackTrace(System.err);
                     fail = true;
                 }
             }
        }
        if (fail)
            fail("Failures detected.");
    }

    private static String toHexString(byte[] b)
    {
       StringBuffer hex = new StringBuffer(2 * b.length);
       for (int i = 0; i < b.length; i++) {
           byte n = b[i];
           if (n >= 0 && n <= 15)
               hex.append("0");
           hex.append(Integer.toHexString(0xFF & n));
       }
       return hex.toString().toUpperCase();
    }

    public DataTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return getSuite(DataTest.class);
    }
}
