package ru.croc.xmltransform;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.XPath;

public class XMLTransformatorTest {
	
	public static void main(String[] args) throws DocumentException {
		XPath xp = DocumentHelper.createXPath("/");
		String text = "<ns1:WSDataModel xmlns:ns1=\"urn-com-amalto-xtentis-webservice\" "
				+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
				+ "<name>EEC_PROD</name><description/>"
				+ "<xsdSchema>"
					+ "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" version=\"1.0.0\">"
						+ "<xsd:element name=\"NSIObjects\" type=\"NSIObjectsType\"></xsd:element>"
						+ "<xsd:element name=\"NSIObjectsType\">"
						+ "<xsd:element name=\"OLOLO\" type=\"TROLOLO\"></xsd:element>"
						+ "</xsd:element>"
						+ "<xsd:element name=\"TROLOLO\"></xsd:element>"
					+ "</xsd:schema>"
				+ "</xsdSchema>"
				+ "</ns1:WSDataModel>";
		//String text = "<person> <name>James<subname ololo =\"trololo\">olol</subname><su</name> </person>";
	//	String text2 = "<f:org> <f:address type=\"addr\">ADDRESS</f:address></f:org>";
		//String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        Document document = DocumentHelper.parseText(text);
      //  Document document2 = DocumentHelper.parseText(text2);
        XMLTransformator tr = new XMLTransformator();
        
        //org.dom4j.Node nd = (org.dom4j.Node) xp.evaluate(document);

       // tr.importNode("/", document, "//xsdSchema/xsd:schema/child::*", );
        //tr.addNewElement("ololo", "trololo", "ns1:WSDataModel/xsdSchema/xsd:schema/child::*", document);
        
        //System.out.println(document.asXML());
        
        //tr.filter(document, "/ns1:WSDataModel/xsdSchema/xsd:schema", "NSIObjects");
        
        System.out.println("TEXT: \n" + (String)tr.extract(document, "/ns1:WSDataModel/name", "TEXT"));
        System.out.println("---");
        System.out.println("NODE: \n" + ((org.dom4j.Document)tr.extract(document, "/ns1:WSDataModel/xsdSchema/xsd:schema", "NODE")).asXML());
        System.out.println(document.asXML());
        System.out.println("---");
        System.out.println("NODE_TEXT: \n" + (String)tr.extract(document, "/ns1:WSDataModel/xsdSchema/xsd:schema", "NODE_TEXT"));
        System.out.println("---");
        System.out.println("SET_ROOT: \n" + ((org.dom4j.Document)tr.setRoot(document, "/ns1:WSDataModel/xsdSchema/xsd:schema")).asXML());
        System.out.println(document.asXML());
        
        
      
        
      /*  List list =  document.selectNodes("/person");
        List list2 = document2.selectNodes("/org/address");
		if (list !=  null) {
			for (Object node : list) {
				for (Object node2 : list2) {
					if (node instanceof org.dom4j.tree.DefaultElement && node2 instanceof org.dom4j.tree.DefaultElement) {
						((org.dom4j.tree.DefaultElement)node).add(((org.dom4j.tree.DefaultElement)node2).createCopy());
				}		
			} 
		} 
					
					/*org.dom4j.Element element = ((org.dom4j.tree.DefaultElement) node).getParent();
					if (element != null) {
						element.remove((org.dom4j.tree.DefaultElement) node);
					} else {
						org.dom4j.Element root = document.getRootElement();
						if (root != null) {
							document.remove(root);
						}
						
					}*/
					
					
					/*org.dom4j.Element element = ((org.dom4j.tree.DefaultElement) node).getParent();
					if (element != null) {
						element.remove((org.dom4j.tree.DefaultElement) node);
					} else {
						org.dom4j.Element root = document.getRootElement();
						if (root != null) {
							document.remove(root);
						}
						
					}*/
					
					
					
					/*
					org.dom4j.Element newEl = org.dom4j.DocumentHelper.createElement("dfdfdf");
					newEl.setText("text");
					((org.dom4j.tree.DefaultElement )node).addAttribute("fdfd", "fdfdfd");
					org.dom4j.Attribute att = ((org.dom4j.tree.DefaultElement )node).attribute("fdfd");
					//((org.dom4j.tree.DefaultElement )node).remove(att);
					 */

//} */
		

		
        //document.normalize();
		//System.out.println(document.asXML());
	}
}
