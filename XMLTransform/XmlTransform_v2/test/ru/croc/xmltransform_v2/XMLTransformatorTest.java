package ru.croc.xmltransform_v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;

public class XMLTransformatorTest {
	
	public static void main(String[] args) throws Exception {
		
		List<Map<String, String>> nsMap = formManualNSMap();// new ArrayList<Map<String,String>>(); //formManualNSMap();
		XMLTransformator tr = new XMLTransformator(nsMap, false);
		Document xml = 	DocumentHelper.parseText("<putUpdateTable><RqUID>5af36faa-398c-485e-a729-1514f822f30a</RqUID><RqTm>2014-12-16T18:59:51</RqTm><SourceSystem>LNSI</SourceSystem><m:Stations xmlns:m=\"http://www.croc.ru/cppk/sac/lnsi/service/\"></m:Stations></putUpdateTable>");
		tr.importNode("putUpdateTable/m:Stations", "root/n:test", 
				xml, 
				DocumentHelper.parseText("<root><n:test xmlns:n=\"http://www.croc.ru/cppk/sac/lnsi/service/\">TESTSTETE</n:test></root>"));
		
		
		System.out.println(xml.asXML());
		
	}
	
	private static List<Map<String, String>> formManualNSMap() {
		List<Map<String,String>> list = new LinkedList<Map<String,String>>();
		
		Map<String,String> map1 = new HashMap<String, String>();
		list.add(map1);
		map1.put("NS_PREFIX", "m");
		map1.put("NS_URI", "http://www.croc.ru/cppk/sac/lnsi/service/");
		map1.put("DOCUMENT", "thisXML");
		
		
		map1 = new HashMap<String, String>();
		list.add(map1);
		map1.put("NS_PREFIX", "n");
		map1.put("NS_URI", "http://www.croc.ru/cppk/sac/lnsi/service/");
		map1.put("DOCUMENT", "childXML");
		
		return list;
	}
	
	private static List<Map<String,String>> formAutoNSMap() {
		List<Map<String,String>> list = new LinkedList<Map<String,String>>();
		
		Map<String,String> map1 = new HashMap<String, String>();
		list.add(map1);
		map1.put("NS_XPATH", "/putUpdateTable/namespace::*");
		map1.put("DOCUMENT", "thisXML");
		
		
		Map<String,String> map2= new HashMap<String, String>();
		list.add(map2);
		map2.put("NS_XPATH", "//namespace::*");
		map2.put("DOCUMENT", "childXML");
		
		return list;
	}
}
