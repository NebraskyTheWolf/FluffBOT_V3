/*
 * $Id: GrayFilter.java,v 1.4 2008/01/28 21:28:37 edankert Exp $
 *
 * Copyright (c) 2002 - 2008, Edwin Dankert
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, 
 *	 this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright 
 * 	 notice, this list of conditions and the following disclaimer in the 
 *	 documentation and/or other materials provided with the distribution. 
 * * Neither the name of 'Edwin Dankert' nor the names of its contributors 
 *	 may  be used to endorse or promote products derived from this software 
 *	 without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR 
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.bounce.image;

/*
---------------------------------------------------------------------------------
File Name : GrayFilter.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/



/**
 * A filter that creates a gray version of the image.
 * This is almost the same as the javax.swing.GrayFilter version.
 * This filter does only not allow for the brighter variable. If
 * the image needs to be made brighter, use the BrighterFilter.
 *
 * @version	$Revision: 1.4 $, $Date: 2008/01/28 21:28:37 $
 * @author Edwin Dankert (edankert@gmail.com)
 */
public class GrayFilter extends BaseFilter {
	private int percent;

	/**
	 * Constructs a Filter that can be used to construct a 
	 * grayscale image.
	 *
	 * @param p the percentage of color in the range 0..100, where 100 is 
	 *        the darkest, and 0 is the lightest.
	 */
	public GrayFilter( int p) {
	    percent = p;

	    canFilterIndexColorModel = true;
	}
	
    /**
     * Overrides <code>RGBImageFilter.filterRGB</code>.
     *
	 * @param x the horizontal position of the pixel.
	 * @param y the vertical position of the pixel.
	 * @param rgb the color of the pixel.
     *
     * @return the new rgb value
     */
    public int filterRGB( int x, int y, int rgb) {
		int component = getNTSCValue( rgb);
        
		component = (component * (100 - percent) / 100);
	
        if ( component < 0) {
			component = 0;
        }

        if ( component > 0xFF) {
		 	component = 255;
        }

        return getRGB( rgb, component, component, component);
    }
} 
