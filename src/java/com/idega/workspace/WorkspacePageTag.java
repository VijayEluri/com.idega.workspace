/*
 * $Id: WorkspacePageTag.java,v 1.4 2005/10/10 11:30:54 tryggvil Exp $
 *
 * Copyright (C) 2004 Idega. All Rights Reserved.
 *
 * This software is the proprietary information of Idega.
 * Use is subject to license terms.
 *
 */
package com.idega.workspace;

import javax.faces.component.UIComponent;
import javax.servlet.jsp.JspException;
import com.idega.presentation.PageTag;

/**
 * JSP tag for Workspace
 * <p>
 * Last modified: $Date: 2005/10/10 11:30:54 $ by $Author: tryggvil $
 *
 * @author tryggvil
 * @version $Revision: 1.4 $
 */
public class WorkspacePageTag extends PageTag {
	
	String layout;
	
	/**
	 * @see javax.faces.webapp.UIComponentTag#getRendererType()
	 */
	public String getRendererType() {
		return null;
	}
		
	/**
	 * @see javax.faces.webapp.UIComponentTag#getComponentType()
	 */
	public String getComponentType() {
		return "WorkspacePage";
	}
	
	
	/* (non-Javadoc)
	 * @see com.idega.presentation.PageTag#setProperties(javax.faces.component.UIComponent)
	 */
	protected void setProperties(UIComponent component) {
		// TODO Auto-generated method stub
		super.setProperties(component);
		WorkspacePage page = (WorkspacePage)component;
		if(layout!=null){
			page.setLayout(layout);
		}
	}

	public void setLayout(String layout){
		this.layout=layout;
	}

	/* (non-Javadoc)
	 * @see javax.faces.webapp.UIComponentTag#doEndTag()
	 */
	public int doEndTag() throws JspException {
		// TODO Auto-generated method stub
		return super.doEndTag();
	}

	/* (non-Javadoc)
	 * @see javax.faces.webapp.UIComponentTag#doStartTag()
	 */
	public int doStartTag() throws JspException {
		// TODO Auto-generated method stub
		return super.doStartTag();
	}
}
