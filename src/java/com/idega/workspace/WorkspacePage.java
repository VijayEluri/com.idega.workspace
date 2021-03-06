/*
 *  $Id: WorkspacePage.java,v 1.30 2009/01/07 11:35:02 valdas Exp $
 *
 *  Created on 13.7.2004 by Tryggvi Larusson
 *
 *  Copyright (C) 2004 Idega Software hf. All Rights Reserved.
 *
 *  This software is the proprietary information of Idega hf.
 *  Use is subject to license terms.
 *
 */
package com.idega.workspace;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;

import com.idega.core.view.FramedApplicationViewNode;
import com.idega.core.view.ViewManager;
import com.idega.core.view.ViewNode;
import com.idega.presentation.IWContext;
import com.idega.presentation.Page;
import com.idega.util.CoreConstants;
import com.idega.util.PresentationUtil;
import com.idega.webface.IWBundleStarter;
import com.idega.webface.WFContainer;
import com.idega.webface.WFFrame;

/**
 * A base page for using in the Workspace environment.<br>
 * This page should be around all UI components in the environment.<br>
 * 
 * <br>
 * Last modified: $Date: 2009/01/07 11:35:02 $ by $Author: valdas $
 * 
 * @author <a href="mailto:tryggvil@idega.com">Tryggvi Larusson</a>
 * @version $Revision: 1.30 $
 */
public class WorkspacePage extends Page {


	private final static String IW_BUNDLE_IDENTIFIER = "com.idega.webface";

	//private List specialList;
	private boolean embedForm=false;
	private boolean isInitalized=false;
	private transient UIForm form;
	private String WF_PAGE_CLASS="ws_body";
	
	public static String LAYOUT_ONECOLUMN="ws_layout_onecolumn";
	public static String LAYOUT_TWOCOLUMN="ws_layout_twocolumn";
	public static String LAYOUT_COMPACT="ws_layout_compact";
	
	private String layout=LAYOUT_ONECOLUMN;

	private Boolean showFunctionMenu;
	
	private Boolean useHtmlTag = Boolean.TRUE;
	
	public static String FACET_HEAD="ws_head";
	public static String FACET_FUNCTIONMENU="ws_functionmenu";
	public static String FACET_MAIN="ws_main";
	public static String FACET_LAYOUT="ws_layout";
	
	public WorkspacePage() {
		setTransient(false);
		setPrintScriptSourcesDirectly(false);
	}
	
	@Override
	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}
	public void initializeContent(FacesContext context) {
		IWContext iwc = IWContext.getIWContext(context);
		
		//TODO: Change this, this is a hack for the function menu:
		ViewManager viewManager = getViewManager(iwc);
		ViewNode node = viewManager.getViewNodeForContext(iwc);
		
		ViewNode parentNode = node.getParent();

		this.setStyleClass(this.WF_PAGE_CLASS + " " + node.getViewId() + (parentNode != null && !parentNode.getViewId().equals(CoreConstants.WORKSPACE_VIEW_MANAGER_ID) ? " " + parentNode.getViewId() : ""));

		//Initialize the areas:
		this.getMainArea();
		this.getHead();
		this.getFunctionMenu(node);

		Page thePage = this;

		thePage.setTitle("idegaWeb Applications");
		
		if(displayFunctionMenu(node)){
			try{
				setLayout(LAYOUT_TWOCOLUMN);
				String nodeId = getNodeNameForFunctionMenu(node);
				WorkspaceFunctionMenu menu = new WorkspaceFunctionMenu();
				menu.setId(getId()+"WorkspaceFunctionMenu");
				menu.setApplication(nodeId);
				add(FACET_FUNCTIONMENU,menu);					
			}
			catch(Throwable t){
				t.printStackTrace();
			}
		}
		if(node instanceof FramedApplicationViewNode){
			FramedApplicationViewNode frameNode = (FramedApplicationViewNode)node;
			WFFrame frame = new WFFrame(node.getName(),frameNode.getFrameUrl());
			frame.setFrameHeight(0, 90);
			add(FACET_MAIN,frame);
		}
		
		//Initialize the WorkspaceBar:
		/*WorkspaceBar bar =*/ getWorkspaceBar();
	}
	
	public WorkspaceBar getWorkspaceBar(){
		String componentId = this.getId()+"WorkspaceBar";
		UIComponent region = getPageFacet(FACET_HEAD);
		WorkspaceBar bar = (WorkspaceBar) region.findComponent(componentId);
		if(bar==null){
			bar = new WorkspaceBar();
			bar.setStyleClass("ws_mainnavigation");
			bar.setId(getId()+"WorkspaceBar");
			add(FACET_HEAD,bar);
		}
		return bar;
	}
	
	private ViewManager getViewManager(){
		FacesContext context = FacesContext.getCurrentInstance();
		return getViewManager(context);
	}
	
	private ViewManager getViewManager(FacesContext context){
		IWContext iwc = IWContext.getIWContext(context);
		return ViewManager.getInstance(iwc.getIWMainApplication());
	}
	
	@Override
	public List<UIComponent> getChildren(){
		if(this.embedForm){
			return getForm().getChildren();
		}
		else{
			return super.getChildren();
		}
		
	}
	
	
	protected boolean displayFunctionMenu(ViewNode node){
		if(this.showFunctionMenu!=null){
			return this.showFunctionMenu.booleanValue();
		}
		else{
			if(getLayout().equals(LAYOUT_COMPACT)){
				return false;
			}
			else{
				ViewNode appNode = node;
				ViewNode parentNode = appNode.getParent();
				ViewManager viewManager = getViewManager();
				ViewNode workspaceNode = viewManager.getWorkspaceRoot();
				while(!(parentNode == null || parentNode.equals(workspaceNode)||appNode.equals(workspaceNode))){
					appNode=parentNode;
					parentNode=appNode.getParent();
				}
				return (appNode.getChildren().size()>0);
			}
		}
	}
	
	public String getNodeNameForFunctionMenu(ViewNode node){
		ViewNode appNode = node;
		ViewNode parentNode = appNode.getParent();
		ViewManager viewManager = getViewManager();
		ViewNode workspaceNode = viewManager.getWorkspaceRoot();
		while(!(parentNode.equals(workspaceNode)||appNode.equals(workspaceNode))){
			appNode=parentNode;
			parentNode=appNode.getParent();
		}
		return appNode.getViewId();
	}
	
	@Override
	public void add(UIComponent comp){
		this.getForm().getChildren().add(comp);
	}
	
	public void add(String key,UIComponent child){
		getForm();
		UIComponent setComp = getPageFacet(key);
		if(setComp==null){
			WFContainer container = new WFContainer();
			setPageFacet(key,container);
			container.setStyleClass(key);
			container.add(child);
		}
		else{
			setComp.getChildren().add(child);
		}
	}
	
	public void setPageFacet(String facetKey,UIComponent component){
		getForm().getFacets().put(facetKey,component);
	}
	
	public UIComponent getPageFacet(String facetKey){
		return getForm().getFacets().get(facetKey);
	}
	
	public UIComponent getLayoutContainer(){
		UIComponent area = getPageFacet(FACET_LAYOUT);
		if(area==null){
			WFContainer container = new WFContainer();
			container.setId(FACET_LAYOUT);
			container.setStyleClass(getLayout());
			setPageFacet(FACET_LAYOUT,container);
			area=container;
		}
		return area;
	}
	
	public UIComponent getMainArea(){
		UIComponent area = getPageFacet(FACET_MAIN);
		if(area==null){
			WFContainer container = new WFContainer();
			container.setId(FACET_MAIN);
			container.setStyleClass(FACET_MAIN);
			setPageFacet(FACET_MAIN,container);
			area=container;
		}
		return area;
	}
	
	public UIComponent getHead(){
		UIComponent head = getPageFacet(FACET_HEAD);
		if(head==null){
			WFContainer container = new WFContainer();
			container.setId(FACET_HEAD);
			container.setStyleClass(FACET_HEAD);
			setPageFacet(FACET_HEAD,container);
			head=container;
		}
		return head;
	}
	
	public UIComponent getFunctionMenu(ViewNode node){
		UIComponent menu = getPageFacet(FACET_FUNCTIONMENU);
		if(menu==null){
			WFContainer container = new WFContainer();
			container.setId(FACET_FUNCTIONMENU);
			container.setStyleClass(FACET_FUNCTIONMENU + " " + node.getViewId());
			setPageFacet(FACET_FUNCTIONMENU,container);
			menu=container;
		}
		return menu;
	}
	
	/**
	 * This sets the page to embed a UIForm. This does not currently handle restoring state.
	 * @param doEmbed
	 */
	public void setToEmbedForm(boolean doEmbed){
		//TODO: implement handling of this fully
		this.embedForm=doEmbed;
	}
	
	private UIForm getForm(){
		//String formId = this.getId()+"-form";
		//return (UIForm)getFacets().get(formId);
		if(this.form==null){
			UIForm myForm  = findSubForm();
			if(myForm==null){
				throw new RuntimeException("WorkspacePage: No form found in page, it must be explicitly added inside page");
			}
			return myForm;
		}
		return this.form;
	}
	
	private UIForm findSubForm(){
		Collection<UIComponent> children = getChildren();
		for (UIComponent child: children) {
			if(child instanceof UIForm){
				return (UIForm)child;
			}
		}
		return null;
	}
	
	@Override
	public void encodeBegin(FacesContext context) throws IOException{
		if(!this.isInitalized){
			this.initializeContent(context);
			this.isInitalized=true;
		}
		super.encodeBegin(context);
	}
	
	@Override
	public void encodeChildren(FacesContext context) throws IOException{
		IWContext iwc = IWContext.getIWContext(context);
		PresentationUtil.addStyleSheetToHeader(iwc, iwc.getIWMainApplication().getBundle(IWBundleStarter.BUNDLE_IDENTIFIER).getVirtualPathWithFileNameString("style/webfacestyle.css"));

		ViewManager viewManager = getViewManager(iwc);
		ViewNode node = viewManager.getViewNodeForContext(iwc);
		
		addSessionPollingDWRFiles(iwc);
		addNotifications(iwc);
		enableReverseAjax(iwc);
		enableChromeFrame(iwc);
		
		UIComponent layoutContainer = getLayoutContainer();
		layoutContainer.encodeBegin(context);
		
		UIForm form = getForm();
		//if(this.embedForm){
			form.encodeBegin(context);
		//}
		//super.encodeChildren(context);
		
		if(layoutContainer.getRendersChildren()){
			layoutContainer.encodeChildren(context);
		}
			
		UIComponent bar = getHead();
		this.renderChild(context,bar);
		UIComponent fMenu = getFunctionMenu(node);
		this.renderChild(context,fMenu);
		UIComponent mainArea = getMainArea();
		if(mainArea==null){
			mainArea = new WFContainer();
			((WFContainer)mainArea).setStyleClass(FACET_MAIN);
		}
		
		mainArea.encodeBegin(context);
		
		if(mainArea.getRendersChildren()){
			mainArea.encodeChildren(context);
		}
		//super.encodeChildren(context);
		
		//form.encodeChildren(context);
		for (UIComponent child: form.getChildren()) {
			renderChild(context, child);
		}
		
		mainArea.encodeEnd(context);
		
		
		//if(this.embedForm){
			//form.encodeChildren(context);
			form.encodeEnd(context);
		//}
			
		layoutContainer.encodeEnd(context);
	}
	
	@Override
	public void encodeEnd(FacesContext context) throws IOException{
		super.encodeEnd(context);
	}
	
	
	/* (non-Javadoc)
	 * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
	 */
	@Override
	public void restoreState(FacesContext ctx, Object state) {
		Object values[] = (Object[])state;
		super.restoreState(ctx, values[0]);
		Boolean bIsInitalized = (Boolean) values[1];
		this.isInitalized=bIsInitalized.booleanValue();
		this.layout=(String)values[2];
		this.embedForm=((Boolean)values[3]).booleanValue();
	}
	/* (non-Javadoc)
	 * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
	 */
	@Override
	public Object saveState(FacesContext ctx) {
		Object values[] = new Object[4];
		values[0] = super.saveState(ctx);
		values[1] = Boolean.valueOf(this.isInitalized);
		values[2] = this.layout;
		values[3] = Boolean.valueOf(this.embedForm);
		return values;
	}
	
	

	/* (non-Javadoc)
	 * @see javax.faces.component.UIComponent#processRestoreState(javax.faces.context.FacesContext, java.lang.Object)
	 */
	@Override
	public void processRestoreState(FacesContext fc, Object arg1) {
		super.processRestoreState(fc, arg1);
	}
	/* (non-Javadoc)
	 * @see javax.faces.component.UIComponent#processSaveState(javax.faces.context.FacesContext)
	 */
	@Override
	public Object processSaveState(FacesContext arg0) {
		return super.processSaveState(arg0);
	}
	/**
	 * 
	 *  Last modified: $Date: 2009/01/07 11:35:02 $ by $Author: valdas $
	 * 
	 * @author <a href="mailto:tryggvil@idega.com">tryggvil</a>
	 * @version $Revision: 1.30 $
	 */
	public class SpecialChildList implements List<UIComponent> {
		
		UIComponent parent;
		UIComponent child;
		List<UIComponent> list;
		
		public SpecialChildList(UIComponent parent,UIComponent child){
			this.parent=parent;
			this.child=child;
			List<UIComponent> children = parent.getChildren();
			this.list=children;
		}
		
		/**
		 * @param arg0
		 * @param arg1
		 */
		public void add(int arg0, UIComponent arg1) {
			if(arg1.equals(this.child)){
				this.list.add(arg0, arg1);
			}
			else{
				this.child.getChildren().add(arg0, arg1);
			}
		}
		/**
		 * @param arg0
		 * @return
		 */
		public boolean add(UIComponent arg0) {
			if(arg0.equals(this.child)){
				return this.list.add(arg0);
			}
			else{
				return this.child.getChildren().add(arg0);
			}
		}
		/**
		 * @param arg0
		 * @param arg1
		 * @return
		 */
		public boolean addAll(int index, Collection<? extends UIComponent> c) {
			return this.list.addAll(index, c);
		}
		
		/**
		 * @param arg0
		 * @return
		 */
		public boolean addAll(Collection<? extends UIComponent> c) {
			return this.list.addAll(c);
		}
		/**
		 * 
		 */
		public void clear() {
			this.list.clear();
		}
		/**
		 * @param arg0
		 * @return
		 */
		public boolean contains(Object arg0) {
			return this.list.contains(arg0);
		}
		/**
		 * @param arg0
		 * @return
		 */
		public boolean containsAll(Collection<?> c) {
			return this.list.containsAll(c);
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object arg0) {
			return this.list.equals(arg0);
		}
		/**
		 * @param arg0
		 * @return
		 */
		public UIComponent get(int arg0) {
			return this.list.get(arg0);
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return this.list.hashCode();
		}
		/**
		 * @param arg0
		 * @return
		 */
		public int indexOf(Object arg0) {
			return this.list.indexOf(arg0);
		}
		/**
		 * @return
		 */
		public boolean isEmpty() {
			return this.list.isEmpty();
		}
		/**
		 * @return
		 */
		public Iterator<UIComponent> iterator() {
			return this.list.iterator();
		}
		/**
		 * @param arg0
		 * @return
		 */
		public int lastIndexOf(Object arg0) {
			return this.list.lastIndexOf(arg0);
		}
		/**
		 * @return
		 */
		public ListIterator<UIComponent> listIterator() {
			return this.list.listIterator();
		}
		/**
		 * @param arg0
		 * @return
		 */
		public ListIterator<UIComponent> listIterator(int arg0) {
			return this.list.listIterator(arg0);
		}
		/**
		 * @param arg0
		 * @return
		 */
		public UIComponent remove(int arg0) {
			return this.list.remove(arg0);
		}
		/**
		 * @param arg0
		 * @return
		 */
		public boolean remove(Object arg0) {
			return this.list.remove(arg0);
		}
		/**
		 * @param arg0
		 * @return
		 */
		public boolean removeAll(Collection<?> c) {
			return this.list.removeAll(c);
		}
		/**
		 * @param arg0
		 * @return
		 */
		public boolean retainAll(Collection<?> c) {
			return this.list.retainAll(c);
		}
		/**
		 * @param arg0
		 * @param arg1
		 * @return
		 */
		public UIComponent set(int arg0, UIComponent arg1) {
			return this.list.set(arg0, arg1);
		}
		/**
		 * @return
		 */
		public int size() {
			return this.list.size();
		}
		/**
		 * @param arg0
		 * @param arg1
		 * @return
		 */
		public List<UIComponent> subList(int arg0, int arg1) {
			return this.list.subList(arg0, arg1);
		}
		/**
		 * @return
		 */
		public Object[] toArray() {
			return this.list.toArray();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return this.list.toString();
		}

		public <T> T[] toArray(T[] a) {
			return this.list.toArray(a);
		}
	}
	
	
	/**
	 * Sets the layout (style class) that will control how the main content of the page is displayed.
	 * <br>The standard style classes are defined as static contants called LAYOUT_... in this class.
	 * @param layoutClass
	 */
	public void setLayout(String layoutClass){
		this.layout=layoutClass;
	}
	
	/**
	 * Gets the set layout (style class).
	 * @return
	 */
	public String getLayout(){
		return this.layout;
	}
	
	public void setShowFunctionMenu(boolean showMenu){
		this.showFunctionMenu=new Boolean(showMenu);
	}
	
	
	public void setJavascripturls(String scriptUrls) {
		super.setJavascriptURLs(scriptUrls);
	}
	
	public void setStylesheeturls(String styleSheetUrls) {
		super.setStyleSheetURL(styleSheetUrls);
	}

	public void setUseHtmlTag(Boolean useHtmlTag) {
		this.useHtmlTag = useHtmlTag;
		super.setUseHtmlTag(this.useHtmlTag);
	}
	
}