package org.eclipse.uide.parser;

import lpg.javaruntime.IToken;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
 */

/**
 * 
 *  @author Chris Laffra
 */
public class Ast {
	public final static Ast EMPTY = new Ast("");
	public static char[] contents;
	public List children;
	public IToken token;	 
	public String ruleName;
	public IRule rule;
	public Ast parent;

	public static void setContents(char[] c) {
		contents= c;
	}

	public Ast(String ruleName) { this.ruleName = ruleName; }

	public String getRuleName() { return ruleName; }
	
	public IRule getRule() { return rule; }		
	public boolean isTerminal() { return children == null || children.size() == 0; }
	public void setRule(IRule rule) { this.rule = rule; }	
	public void setToken(IToken token) { this.token = token; }	
	public IToken getToken() { return token; }	
	public Ast getParent() { return parent; }
	public Ast getChild(int nr) { if (children == null) return null; return children.get(nr); }
	public Ast findChild(int kind) { 
		if (children != null)
			for (Ast.List.Element e = children.elements; e != null; e = e.next)
				if (e.ast.token != null && e.ast.token.getKind() == kind)
					return e.ast;
		return null;
	}
	public boolean hasOneChild() { return children != null && children.elements != null && children.elements.next == null; }
	public boolean isRule() { return rule != null; }
	public String getLhs(char contents[]) { 
		return (rule == null) ? token.getValue(contents) : rule.getLeftHandSide(); 
	}
	public String toString() {
		if (token != null) return token.getValue(contents);

		StringBuffer buf= new StringBuffer();

		for(int i=0; i < children.size(); i++) {
			Ast child = children.get(i);
			buf.append(child.toString());
		}
		return buf.toString();
	}
	public String toString(char contents[]) {
		if (ruleName != null) return ruleName;
		return rule != null ? rule.getLeftHandSide() : 
			token != null ? token.getKind()+"(\""+token.getValue(contents)+"\")" 
					: "<epsilon production>";
	}
			
	public void addChild(Ast child) {
		child.parent = this;
		if (children == null) 
			children = new List();
		children.add(child);
	}
	
	public void addChild(IToken token) {
		Ast child = new Ast("");
		child.parent = this;
		child.setToken(token);
		if (children == null) 
			children = new List();
		children.add(child);
	}
	
	private void flattenChild(Ast child) {
		if (children == null)
			children = child.children;
		else
			children.add(child.children);
		if (children != null)
			for (Ast.List.Element e = children.elements; e != null; e = e.next)
				e.ast.parent = this;
	}

	/**
	 * Get the start offset for this Ast node (and its children)
	 * @return
	 */
	public int getStartOffset() {
		if (token != null)
			return token.getStartOffset();
		if (children == null)
			return -1;
		for (Ast.List.Element e = children.elements; e != null; e = e.next) {
			int start = e.ast.getStartOffset();
			if (start != -1)
				return start;
		}
		return -1;
	}
	
	/**
	 * Get the end offset for this Ast node (and its children)
	 * @return
	 */
	public int getEndOffset() {
		if (token != null)
			return token.getEndOffset();
		if (children == null)
			return -1;
		for (Ast.List.Element e = children.elements; e != null; e = e.next) {
			int end = e.ast.getEndOffset();
			if (end != -1)
				return end;
		}
		return -1;
	}
		
	public static class List {

		public Element elements;
		
		public static class Element {
			public Ast ast;
			public Element next;
			public Element(Ast element) {
				this.ast = element;
			}
		}
		
		public Ast get(int nr) {
			for (Element e=elements; e != null; e = e.next, nr--)
				if (nr == 0) return e.ast;
			return null;
		}

		public int size() {
			int size = 0;
			for (Element e=elements; e != null; e = e.next, size++)
				;
			return size;
		}

		public void add(Ast child) {
			Element newElement = new Element(child);
			if (elements == null) {
				elements = newElement;
			}
			else {
				for (Element e=elements; true; e = e.next)
					if (e.next == null) {
						e.next = newElement;
						break;
					}
			}
		}

		public void add(List children) {
			if (children == null || children.elements == null)
				return;
			if (elements == null) {
				elements = children.elements;
			}
			else {
				for (Element e=elements; true; e = e.next)
					if (e.next == null) {
						e.next = children.elements;
						break;
					}
			}
		}

	}

}

