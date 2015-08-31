/*
 * SimplePython - embeddable python interpret in java
 * Copyright (c) Peter Vanusanik, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package me.enerccio.sp.serialization;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.Tags;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xml.internal.security.utils.Base64;

public class XMLPySerializer extends BasePySerializer {
	
	private Stack<Element> rEl = new Stack<Element>();
	private File f;
	private Document doc;
	private Element rootElement;
	
	public XMLPySerializer(File file) {
		f = file;
	}

	@Override
	public void initializeSerialization() throws Exception {
		super.initializeSerialization();
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		doc = docBuilder.newDocument();
		rootElement = doc.createElement("python-state");
		rEl.add(rootElement);
		rEl.add(doc.createElement("python-runtime"));
		rootElement.appendChild(rEl.peek());
		doc.appendChild(rootElement);
	}

	@Override
	public void finishSerialization() throws Exception {
		super.finishSerialization();
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(f);
		
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		transformer.transform(source, result);
	}

	@Override
	public void serialize(String object) {
		Element parent = rEl.peek();
		Element e = doc.createElement("string");
		parent.appendChild(e);
		if (object == null){
			e.setAttribute("is-null", "true");
		} else {
			e.appendChild(doc.createTextNode(object));
		}
	}

	@Override
	protected void saveObjects(List<Object> olist) throws Exception {
		rEl.pop();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream owriter = new ObjectOutputStream(bos);
		for (Object key : olist){
			owriter.writeObject(key);
		}
		Element e = doc.createElement("serialized-java-objects");
		rEl.peek().appendChild(e);
		rEl.add(e);
		serialize(bos.toByteArray());
	}

	@Override
	public void serialize(boolean object) {
		Element parent = rEl.peek();
		Element e = doc.createElement(object ? "true" : "false");
		parent.appendChild(e);
	}

	@Override
	public void serialize(int object) {
		Element parent = rEl.peek();
		Element e = doc.createElement("int");
		e.appendChild(doc.createTextNode(Integer.toString(object)));
		parent.appendChild(e);
	}

	@Override
	public void serialize(byte object) {
		Element parent = rEl.peek();
		Element e = doc.createElement("byte");
		e.appendChild(doc.createTextNode(Byte.toString(object)));
		parent.appendChild(e);
	}

	@Override
	public void serialize(long object) {
		Element parent = rEl.peek();
		Element e = doc.createElement("long");
		e.appendChild(doc.createTextNode(Long.toString(object)));
		parent.appendChild(e);
	}

	@Override
	public void serialize(double object) {
		Element parent = rEl.peek();
		Element e = doc.createElement("double");
		e.appendChild(doc.createTextNode(Double.toString(object)));
		parent.appendChild(e);
	}

	@Override
	public void serialize(float object) {
		Element parent = rEl.peek();
		Element e = doc.createElement("float");
		e.appendChild(doc.createTextNode(Float.toString(object)));
		parent.appendChild(e);
	}

	@Override
	public void serialize(char object) {
		Element parent = rEl.peek();
		Element e = doc.createElement("char");
		e.appendChild(doc.createTextNode(Character.toString(object)));
		parent.appendChild(e);
	}

	@Override
	public void serialize(short object) {
		Element parent = rEl.peek();
		Element e = doc.createElement("short");
		e.appendChild(doc.createTextNode(Short.toString(object)));
		parent.appendChild(e);
	}

	@Override
	public void serialize(byte[] object) {
		Element parent = rEl.peek();
		Element e = doc.createElement("bytes");
		e.appendChild(doc.createTextNode(Base64.encode(object)));
		parent.appendChild(e);
	}

	@Override
	public OutputStream getOutput() {
		return null;
	}

	@Override
	public void serialize(PythonObject object) {
		Element parent = rEl.peek();
		if (object == null){
			Element e = doc.createElement("pyobject-null");
			parent.appendChild(e);
		} else {
			if (primitive(object)){
				object.serializeInnerState((PySerializer)this);
				NodeList nl = parent.getChildNodes();
				Element p = (Element)nl.item(nl.getLength()-1);
				p.setAttribute("pyprimitive", "true");
			} else {
				if (serialized.contains(object.linkName)){
					Element e = doc.createElement("python-link");
					e.setAttribute("link-type", Tags.tagDescName.get(object.getTag()));
					parent.appendChild(e);
					rEl.add(e);
					serialize(object.linkName);
					rEl.pop();
				} else {
					Element e = doc.createElement(Tags.tagDescName.get(object.getTag()));
					parent.appendChild(e);
					rEl.add(e);
					serialized.add(object.linkName);
					object.serializeInnerState((PySerializer)this);
					rEl.pop();
				}
			}
		}
	}
	
	@Override
	public void serializeJava(Object o){
		if (o instanceof PythonObject)
			super.serializeJava(o);
		else {
			Element parent = rEl.peek();
			Element e = doc.createElement("java-serialized-object-link");
			parent.appendChild(e);
			rEl.add(e);
			super.serializeJava(o);
			rEl.pop();
		}
	}
}
