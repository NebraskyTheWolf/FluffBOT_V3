/*
 * $Id$
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

package org.bounce.preferences;

/*
---------------------------------------------------------------------------------
File Name : PreferencesPage.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/



import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

public abstract class PreferencesPage extends JPanel {

    private static final long serialVersionUID = -3184851551114677222L;
    
    private JComponent centerPanel = null;
    private String label = null;
    private JLabel titleLabel = null;

    public PreferencesPage() {
        this(null);
    }

    public PreferencesPage(String title) {
        super(new BorderLayout( 10, 10));

        titleLabel = new JLabel(title);
        titleLabel.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        
        titleLabel.setBackground(Color.white);
        titleLabel.setOpaque(true);
        
        titleLabel.setFont(titleLabel.getFont().deriveFont( (float)16));

        add(titleLabel, BorderLayout.NORTH); 
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(new JSeparator(), BorderLayout.SOUTH);
        
        add(southPanel, BorderLayout.SOUTH);
    }
    
    protected void setCenterPane(JComponent panel) {
        centerPanel = panel;
        add(panel, BorderLayout.CENTER); 
    }

    protected JComponent getCenterPane() {
        return centerPanel; 
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }
   
    public String getTitle() {
        return titleLabel.getText();
    }

    public void setLabel(String label) {
        this.label = label;
    }
   
    public String getLabel() {
        if (label == null) {
            return titleLabel.getText();
        }
        
        return label;
    }
}

