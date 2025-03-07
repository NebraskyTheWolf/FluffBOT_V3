/*
 * $Id: DOMNodeCellRenderer.java,v 1.1 2008/04/15 20:59:50 edankert Exp $
 *
 * Copyright (c) 2002 - 2008, Edwin Dankert
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright 
 *   notice, this list of conditions and the following disclaimer in the 
 *   documentation and/or other materials provided with the distribution. 
 * * Neither the name of 'Edwin Dankert' nor the names of its contributors 
 *   may  be used to endorse or promote products derived from this software 
 *   without specific prior written permission. 
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

package org.bounce.viewer.xml;

/*
---------------------------------------------------------------------------------
File Name : DOMNodeCellRenderer.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/



import java.awt.Component;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;

/**
 * The cell renderer for a XmlElementNode.
 *
 * @author Edwin Dankert (edankert@gmail.com)
 */
public class DOMNodeCellRenderer extends JLabel implements TreeCellRenderer {
	private static final long serialVersionUID = -3943270917800439561L;

    private boolean selected = false;
	private NodeTreeNode node = null;
	
	/**
	 * Sets the look and feel to the Jump Label UI look and feel.
	 * Override this method if you want to install a different UI.
	 */
	public void updateUI() {
	    setUI(DOMNodeCellRendererUI.createUI( this));
	}

	/**
	  * Configures the renderer based on the passed in commands.
	  * The value is set from messaging the tree with
	  * <code>convertValueToText</code>, which ultimately invokes
	  * <code>toString</code> on <code>value</code>.
	  * The foreground color is set based on the selection and the icon
	  * is set based on on leaf and expanded.
	  */
	public Component getTreeCellRendererComponent( JTree tree, Object value,
						  boolean selected, boolean expanded, boolean leaf, 
						  int row,  boolean hasFocus) {
						  
		this.selected = selected;
		
		if (value instanceof NodeTreeNode) {
			this.node = (NodeTreeNode)value;
			
			if (selected) {
			    setForeground(UIManager.getColor("Tree.selectionForeground"));
			} else  {
			    setForeground(UIManager.getColor("Tree.textForeground"));
			}
			
		    setComponentOrientation(tree.getComponentOrientation());
		} 

		return this;
	}

	boolean isSelected() {
		return selected;
	}
	
	List<Line> getLines() {
		return node.getLines();
	}
} 
