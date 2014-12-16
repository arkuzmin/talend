package ru.croc.xmltransform;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.XPath;

public class XMLTransformatorTest {
	
	public static void main(String[] args) throws Exception {
		XMLTransformator tr = new XMLTransformator();
		Document xml = 	DocumentHelper.parseText("<putUpdateTable><RqUID>5af36faa-398c-485e-a729-1514f822f30a</RqUID><RqTm>2014-12-16T18:59:51</RqTm><SourceSystem>LNSI</SourceSystem><Stations/></putUpdateTable>");
		tr.importNode("/putUpdateTable/Stations", "/*", 
				xml, 
				DocumentHelper.parseText("<test>TESTTETST</test>"), "/");
		
		
		System.out.println(xml.asXML());
		
	}
	}
