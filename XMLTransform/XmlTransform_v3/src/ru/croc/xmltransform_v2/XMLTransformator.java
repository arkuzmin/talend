package ru.croc.xmltransform_v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Namespace;

@SuppressWarnings("rawtypes")
public class XMLTransformator {
	private static final String NSM_THIS_XML_DOC_TYPE = "thisXML";
	private static final String NSM_CHILD_XML_DOC_TYPE = "childXML";
	private static final String ANSM_DOC_TYPE_KEY = "DOCUMENT";
	private static final String ANSM_NS_XPATH_KEY = "NS_XPATH";
	private static final String MNSM_NS_PREFIX_KEY = "NS_PREFIX";
	private static final String MNSM_NS_URI_KEY = "NS_URI";
	private static final String MNSM_DOC_TYPE_KEY = "DOCUMENT";
	
	private boolean autoSearchNS;
	private String thisXmlNsXPath = "//namespace::*";
	private String childXmlNsXPath = "//namespace::*";
	
	private Map<String, String> xmlNSMap = new HashMap<String, String>();
	private Map<String, String> childNSMap = new HashMap<String, String>();
	
	public XMLTransformator () {
	}
	
	public XMLTransformator(List<Map<String, String>> nsMapsList, boolean autoSearchNS) {
		this.autoSearchNS = autoSearchNS;
		if (nsMapsList != null) {
			if (autoSearchNS) {
				formAutoNSMap(nsMapsList);
			} else {
				formManualNSMap(nsMapsList);
			}
		}
	}
	
	private void formAutoNSMap(List<Map<String, String>> nsMapsList) {	
		for (Map<String, String> map : nsMapsList) {
			String docType = map.get(ANSM_DOC_TYPE_KEY);
			String nsXPath = map.get(ANSM_NS_XPATH_KEY);
			if (NSM_THIS_XML_DOC_TYPE.equals(docType)) {
				this.thisXmlNsXPath = nsXPath;
			} else {
				this.childXmlNsXPath = nsXPath;
			}
		}
	}
	
	private void formManualNSMap(List<Map<String, String>> nsMapsList) {
		for (Map<String, String> map : nsMapsList) {
			String docType = map.get(MNSM_DOC_TYPE_KEY);
			String nsPrefix = map.get(MNSM_NS_PREFIX_KEY);
			String nsUri = map.get(MNSM_NS_URI_KEY);
			if (NSM_THIS_XML_DOC_TYPE.equals(docType)) {
				xmlNSMap.put(nsPrefix, nsUri);
			} else {
				childNSMap.put(nsPrefix, nsUri);
			}
		}
	}
	
	private boolean stringIsNotEmpty(String str) {
		return str != null && !"".equals(str);
	}

	private org.dom4j.XPath formXPathWithNS(String xPath, org.dom4j.Document xml, String docType) {
		org.dom4j.XPath xp = xml.createXPath(xPath);
		if (this.autoSearchNS) {
			xp.setNamespaceURIs(formNSMapByDocType(xml, docType));
		} else {
			xp.setNamespaceURIs(NSM_THIS_XML_DOC_TYPE.equals(docType) ? xmlNSMap : childNSMap);
		}
		return xp;
	}
	
	private Map<String, String> formNSMapByDocType(org.dom4j.Document xml, String docType) {
		List list = xml.selectNodes(NSM_THIS_XML_DOC_TYPE.equals(docType) ? thisXmlNsXPath : childXmlNsXPath);
        Map<String, String> nsmap = new HashMap<String, String>();
        for (Object obj : list) {
        	if (obj instanceof org.dom4j.Namespace) {
        		org.dom4j.Namespace ns = (org.dom4j.Namespace) obj;
        		nsmap.put(ns.getPrefix(), ns.getURI());
        	}
        }
        return nsmap;
	}
	
	/**
	* Создание нового элемента.
	**/
	private org.dom4j.Element createNewElement(Object p1, Object p2, org.dom4j.Document xml) {
		
		if (!(p1 instanceof String && 
			p2 instanceof String )) {
			return null;
		}
		
		String name = (String) p1;
		String text = (String) p2;
		
		Map<String, String> nsm = xmlNSMap;
		if (this.autoSearchNS) {
			nsm = formNSMapByDocType(xml, NSM_THIS_XML_DOC_TYPE);
		}
		
		org.dom4j.Element newEl = null;

		if (stringIsNotEmpty(name)) {
			String[] un = name.split(":");
			if (un.length == 2) 
				newEl = org.dom4j.DocumentHelper.createElement(
							org.dom4j.DocumentHelper.createQName(
									un[1], 
									Namespace.get(
											un[0],
											nsm.get(un[0])
											)
							)
						);
			else 
				newEl = org.dom4j.DocumentHelper.createElement(name);
			
			if (stringIsNotEmpty(text)) {
				newEl.setText(text);
			}	
		}

		return newEl;
	}
	
	/**
	* Добавление нового элемента в конец.
	**/
	public void addNewElement(Object p1, Object p2, Object p3, Object p4) {
		
		if (!(p1 instanceof String && 
			(p2 instanceof String || p2 instanceof List) &&
			p3 instanceof String && 
			p4 instanceof org.dom4j.Document)) {
			return;
		}
		
		String name = (String) p1;
		String text = "";
		String xPath = (String) p3;
		List<String> textList;
		if (p2 instanceof String) {
			textList = new ArrayList<String>();
			textList.add((String)p2);
		} else
			textList = (List<String>) p2;
		if (textList == null) textList = new ArrayList<String>();
		
		org.dom4j.Document xml = (org.dom4j.Document) p4;
		
		org.dom4j.XPath xp = formXPathWithNS(xPath, xml, NSM_THIS_XML_DOC_TYPE);
		
		List list = xp.selectNodes(xml);
		int ind = -1;
		if (list !=  null) {
			for (Object el : list) {
				ind++;
				text = ind < textList.size() ? textList.get(ind) : "";
				// Если найденный узел - не root
				if (el instanceof org.dom4j.tree.DefaultElement) {
					org.dom4j.Element newEl = createNewElement(name, text, xml);
					if (newEl != null ){
						((org.dom4j.tree.DefaultElement) el).add(newEl);
					}
				}
				// Если root
				else if (el instanceof org.dom4j.tree.DefaultDocument) {
					org.dom4j.Element root = xml.getRootElement();
					if (root == null) {
						org.dom4j.Element newEl = createNewElement(name, text, xml);
						if (newEl != null) {
							xml.add(newEl);
						}
					}
				}
			}
		}
	}
	
	/**
	* Вставка нового элемента в конец.
	**/
	public void insertBefore(Object p1, Object p2, Object p3, Object p4) {
	
		if (!(p1 instanceof String && 
			(p2 instanceof String || p2 instanceof List) &&
			p3 instanceof String && 
			p4 instanceof org.dom4j.Document)) {
			return;
		}
		
		String name = (String) p1;
		String text = "";
		String xPath = (String) p3;
		List<String> textList;
		if (p2 instanceof String) {
			textList = new ArrayList<String>();
			textList.add((String)p2);
		} else
			textList = (List<String>) p2;
		if (textList == null) textList = new ArrayList<String>();
		
		org.dom4j.Document xml = (org.dom4j.Document) p4;
		org.dom4j.XPath xp = formXPathWithNS(xPath, xml, NSM_THIS_XML_DOC_TYPE);
		
		List list = xp.selectNodes(xml);
		int ind = -1;
		if (list !=  null) {
			for (Object el : list) {
				ind++;
				text = ind < textList.size() ? textList.get(ind) : "";
				// Если найденный узел - не root
				if (el instanceof org.dom4j.tree.DefaultElement) {
					org.dom4j.Element newEl = createNewElement(name, text, xml);
					org.dom4j.Element parent = ((org.dom4j.tree.DefaultElement)el).getParent();
					if (parent != null && newEl != null) {
						int idx = parent.indexOf((org.dom4j.tree.DefaultElement)el);
						parent.content().add(idx, newEl);
					}
				}
			}
		}
	}
	
	/**
	* Вставка нового элемента в начало.
	**/ 
	public void insertAfter(Object p1, Object p2, Object p3, Object p4) {
	
		if (!(p1 instanceof String && 
			( p2 instanceof String || p2 instanceof List) &&
			p3 instanceof String &&
			p4 instanceof org.dom4j.Document)) {
			return;
		}
		
		String name = (String) p1;
		String text = "";
		String xPath = (String) p3;
		List<String> textList;
		if (p2 instanceof String) {
			textList = new ArrayList<String>();
			textList.add((String)p2);
		} else
			textList = (List<String>) p2;
		if (textList == null) textList = new ArrayList<String>();
		
		org.dom4j.Document xml = (org.dom4j.Document) p4;
		org.dom4j.XPath xp = formXPathWithNS(xPath, xml, NSM_THIS_XML_DOC_TYPE);
		
		List list = xp.selectNodes(xml);
		int ind = -1;
		if (list !=  null) {
			for (Object el : list) {
				ind++;
				text = ind < textList.size() ? textList.get(ind) : "";
				// Если найденный узел - не root
				if (el instanceof org.dom4j.tree.DefaultElement) {
					org.dom4j.Element newEl = createNewElement(name, text, xml);
					org.dom4j.Element parent = ((org.dom4j.tree.DefaultElement)el).getParent();
					if (newEl != null && parent != null) {
						int idx = parent.indexOf((org.dom4j.tree.DefaultElement)el);
						parent.content().add(idx + 1, newEl);
					}
				}
			}
		}
	}
	
	/**
	* Удаление атрибута.
	**/
	public void removeAttr(Object p1, Object p2, Object p3) {
	
		if (!(p1 instanceof String && 
			p2 instanceof String &&
			p3 instanceof org.dom4j.Document)) {
			return;
		}
		
		String name = (String) p1;
		String xPath = (String) p2;
		org.dom4j.Document xml = (org.dom4j.Document) p3;
		org.dom4j.XPath xp = formXPathWithNS(xPath, xml, NSM_THIS_XML_DOC_TYPE);
		
		List list = xp.selectNodes(xml);
	
		if (list !=  null) {
			for (Object el : list) {
				if (el instanceof org.dom4j.tree.DefaultElement) {
					org.dom4j.Attribute att = ((org.dom4j.tree.DefaultElement) el).attribute(name);
				    if (att != null) {
						((org.dom4j.tree.DefaultElement) el).remove(att);
					}
				}
			}
		}
	}
	
	/** 
	* Переименование узла.
	**/
	public void renameElement(Object p1, Object p2, Object p3) {
	
		if (!( ( p1 instanceof String || p1 instanceof List) && 
			  p2 instanceof String &&
			  p3 instanceof org.dom4j.Document)) {
			return;
		}
		
		String name = "";
		String xPath = (String) p2;
		org.dom4j.Document xml = (org.dom4j.Document) p3;
		List<String> nameList;
		if (p1 instanceof String) {
			nameList = new ArrayList<String>();
			nameList.add((String)p1);
		} else
			nameList = (List<String>) p1;
		if (nameList == null) nameList = new ArrayList<String>();
		
		org.dom4j.XPath xp = formXPathWithNS(xPath, xml, NSM_THIS_XML_DOC_TYPE);
		
		List list = xp.selectNodes(xml);
	
		int ind = -1;
		if (list !=  null) {
			for (Object el : list) {
				ind++;
				if (ind >= nameList.size()) break;
				name = nameList.get(ind);
				// Если найденный узел - не root
				if (el instanceof org.dom4j.tree.DefaultElement) {
					((org.dom4j.tree.DefaultElement)el).setName(name);
				}
			}
		}
	}
	
	/** 
	* Удаление узла.
	**/
	public void removeElement(Object p1, Object p2) {
		
		if (!(p1 instanceof String && 
			  p2 instanceof org.dom4j.Document)) {
			return;
		}
		
		String xPath = (String) p1;
		org.dom4j.Document xml = (org.dom4j.Document) p2;
		org.dom4j.XPath xp = formXPathWithNS(xPath, xml, NSM_THIS_XML_DOC_TYPE);
		
		List list = xp.selectNodes(xml);
	
		if (list !=  null) {
			for (Object el : list) {
				if (el instanceof org.dom4j.tree.DefaultElement) {
					org.dom4j.Element rEl = ((org.dom4j.tree.DefaultElement) el).getParent();
					if (rEl != null) {
						rEl.remove((org.dom4j.tree.DefaultElement) el);
					} else {
						org.dom4j.Element root = xml.getRootElement();
						if (root != null) {
							xml.remove(root);
						}
					}
				}
			}
		}
	}
	
	/**
	* Добавление нового атрибута к узлу.
	**/ 
	public void addAttr(Object p1, Object p2, Object p3, Object p4) {
		
		if (!(p1 instanceof String && 
			  ( p2 instanceof String || p2 instanceof List) &&
			  p3 instanceof String &&
			  p4 instanceof org.dom4j.Document)) {
			return;
		}
		
		String name = (String) p1;
		String value = "";
		String xPath = (String) p3;
		List<String> valueList;
		if (p2 instanceof String) {
			valueList = new ArrayList<String>();
			valueList.add((String)p2);
		} else
			valueList = (List<String>) p2;
		if (valueList == null) valueList = new ArrayList<String>();
		
		org.dom4j.Document xml = (org.dom4j.Document) p4;
		org.dom4j.XPath xp = formXPathWithNS(xPath, xml, NSM_THIS_XML_DOC_TYPE);
		
		List list = xp.selectNodes(xml);
		int ind = -1;
		if (list !=  null) {
			for (Object el : list) {
				ind++;
				value = ind < valueList.size() ? valueList.get(ind) : "";
				// Если найденный узел - не root
				if (el instanceof org.dom4j.tree.DefaultElement) {
					((org.dom4j.tree.DefaultElement) el).addAttribute(name, value);
			    }
			}
		}
	}
	
	/**
	* Добавление текста к элементу.
	**/ 
	public void setNodeText(Object p1, Object p2, Object p3) {
	
		if (!( ( p1 instanceof String || p1 instanceof List) && 
			  p2 instanceof String &&
			  p3 instanceof org.dom4j.Document)) {
			return;
		}
		
		String text = "";
		String xPath = (String) p2;
		org.dom4j.Document xml = (org.dom4j.Document) p3;
		List<String> textList;
		if (p1 instanceof String) {
			textList = new ArrayList<String>();
			textList.add((String)p1);
		} else
			textList = (List<String>) p1;
		if (textList == null) textList = new ArrayList<String>();
		
		org.dom4j.XPath xp = formXPathWithNS(xPath, xml, NSM_THIS_XML_DOC_TYPE);
		
		List list = xp.selectNodes(xml);
		int ind = -1;
		if (list !=  null) {
			for (Object el : list) {
				ind++;
				text = ind < textList.size() ? textList.get(ind) : "";
				// Если найденный узел - не root
				if (el instanceof org.dom4j.tree.DefaultElement) {
					((org.dom4j.tree.DefaultElement) el).setText(text);
			    }
			}
		}
	}
	
	/**
	* Импорт узла из одного документа в другой - перед.
	**/
	public void importBefore(Object p1, Object p2, Object p3, Object p4) {
	
		if (!(p1 instanceof String && 
			  p2 instanceof String &&
			  p3 instanceof org.dom4j.Document &&
			  p4 instanceof org.dom4j.Document)) {
			return;
		}
		
		String xPath = (String) p1;
		String xPath2 = (String) p2;
		org.dom4j.Document xml = (org.dom4j.Document) p3;
		org.dom4j.Document child = (org.dom4j.Document) p4;
		org.dom4j.XPath xp = formXPathWithNS(xPath, xml, NSM_THIS_XML_DOC_TYPE);
		org.dom4j.XPath xp2 = formXPathWithNS(xPath2, child, NSM_CHILD_XML_DOC_TYPE);
		
		List list = xp.selectNodes(xml);
		List list2 = xp2.selectNodes(child);
		if (list !=  null && list2 != null) {
			for (Object node : list) {
				for (Object node2 : list2) {
					if (node instanceof org.dom4j.tree.DefaultElement && node2 instanceof org.dom4j.tree.DefaultElement) {
						org.dom4j.Element parent = ((org.dom4j.tree.DefaultElement)node).getParent();
						if (parent != null) {
							int ind = parent.indexOf((org.dom4j.tree.DefaultElement)node);
							parent.content().add(ind, ((org.dom4j.tree.DefaultElement)node2).createCopy());
						}
					}		
				} 
			} 
		}
	}
	
	/**
	* Импорт узла из одного документа в другой - после.
	**/
	public void importAfter(Object p1, Object p2, Object p3, Object p4) {
		
		if (!(p1 instanceof String && 
			  p2 instanceof String &&
			  p3 instanceof org.dom4j.Document &&
			  p4 instanceof org.dom4j.Document)) {
			return;
		}
		
		String xPath = (String) p1;
		String xPath2 = (String) p2;
		org.dom4j.Document xml = (org.dom4j.Document) p3;
		org.dom4j.Document child = (org.dom4j.Document) p4;
		org.dom4j.XPath xp = formXPathWithNS(xPath, xml, NSM_THIS_XML_DOC_TYPE);
		org.dom4j.XPath xp2 = formXPathWithNS(xPath2, child, NSM_CHILD_XML_DOC_TYPE);
		
		List list = xp.selectNodes(xml);
		List list2 = xp2.selectNodes(child);
		if (list !=  null && list2 != null) {
			for (Object node : list) {
				for (Object node2 : list2) {
					if (node instanceof org.dom4j.tree.DefaultElement && node2 instanceof org.dom4j.tree.DefaultElement) {
						org.dom4j.Element parent = ((org.dom4j.tree.DefaultElement)node).getParent();
						if (parent != null) {
							int ind = parent.indexOf((org.dom4j.tree.DefaultElement)node);
							parent.content().add(ind + 1, ((org.dom4j.tree.DefaultElement)node2).createCopy());
						}
					}		
				} 
			} 
		}
	}
	
	/**
	* Импорт узла из одного документа в другой.
	**/
	public void importNode(Object p1, Object p2, Object p3, Object p4) {
		
		if (!(p1 instanceof String && 
			  p2 instanceof String &&
			  p3 instanceof org.dom4j.Document &&
			  p4 instanceof org.dom4j.Document)) {
			return;
		}
		
		String xPath = (String) p1;
		String xPath2 = (String) p2;
		org.dom4j.Document xml = (org.dom4j.Document) p3;
		org.dom4j.Document child = (org.dom4j.Document) p4;
		
		org.dom4j.XPath xp = formXPathWithNS(xPath, xml, NSM_THIS_XML_DOC_TYPE);
		org.dom4j.XPath xp2 = formXPathWithNS(xPath2, child, NSM_CHILD_XML_DOC_TYPE);
		
		List list = xp.selectNodes(xml);
		List list2 = xp2.selectNodes(child);

		if (list !=  null && list2 != null) {
			for (Object node : list) {
				for (Object node2 : list2) {
					if (node instanceof org.dom4j.tree.DefaultElement && node2 instanceof org.dom4j.tree.DefaultElement) {
						((org.dom4j.tree.DefaultElement)node).add(((org.dom4j.tree.DefaultElement)node2).createCopy());
					}		
				} 
			}
		}	 
	}
	
	private  List<String> getRefsToInt(org.dom4j.Element el) {
    	List<String> refsTo = new ArrayList<String>();
    	org.dom4j.Attribute attrType = el.attribute("type");
    	if (attrType != null)
    		refsTo.add(attrType.getValue());
    	for (Object o : el.selectNodes(".//*")) {
    		org.dom4j.Node n = (org.dom4j.Node)o;
    		for (String refType : new String[]{"ref", "base", "type"}) {
    			org.dom4j.Attribute attr = ((org.dom4j.Element)n).attribute(refType);
    			if (attr != null)
	    			refsTo.add(attr.getValue());
	    	}
    	}
    	return refsTo;
	}
	
	private  void fillRefs(List<String> parentRefs, String name,
			Map<String, List<String>> allRefsFrom, Map<String, List<String>> allRefsTo) {
		if (!allRefsFrom.containsKey(name))
			allRefsFrom.put(name, new ArrayList<String>());
		List<String> parents = allRefsFrom.get(name);
		
		for (String parent : parentRefs)
			if (!parents.contains(parent))
				parents.add(parent);
		
		parentRefs.add(name);
		
		if (allRefsTo.containsKey(name))
			for (String child : allRefsTo.get(name))
				fillRefs(parentRefs, child, allRefsFrom, allRefsTo);
		
		parentRefs.remove(name);
	}
	
    private  Map<String, List<String>> getRefsFrom(org.dom4j.Document xsd, org.dom4j.XPath xp) {
    	Map<String, List<String>> allRefsFrom = new HashMap<String, List<String>>();
    	Map<String, List<String>> allRefsTo = new HashMap<String, List<String>>();
    	List<String> notRoots = new ArrayList<String>();
    	
    	for (Object o : xp.selectNodes(xsd)) {
    		org.dom4j.Node n = (org.dom4j.Node) o;
    		String name = getAttrValue((org.dom4j.Element)n, "name");
    		if (name == null) {
    			continue;
    		}
    		
    		List<String> refsTo = getRefsToInt((org.dom4j.Element)n);
    		allRefsTo.put(name, refsTo);
    		
    		for (String refTo : refsTo){
    			if (!notRoots.contains(refTo))
    				notRoots.add(refTo);
    		}
    	}
    	// now allRefsFrom contains direct links from elements
    	// let's build full reference list
    	
    	for (Map.Entry<String, List<String>> kv : allRefsTo.entrySet())
    		if (!notRoots.contains(kv.getKey()))
    			fillRefs(new ArrayList<String>(), kv.getKey(), allRefsFrom, allRefsTo);	
    	
    	return allRefsFrom;
    }
	
    private  boolean canPassElement(String elname, Map<String, List<String>> refsFrom, List<String> filter) {
    	if (!refsFrom.containsKey(elname)) return false;
    	if (filter.contains(elname)) return true;
    	for (String ref : refsFrom.get(elname)) {
    		if (filter.contains(ref))
    			return true;
    	}
    	return false;
    }
    
    /**
     * Возвращает значение атрибута для елемента.
     * @param el
     * @param atName
     * @return
     */
    private String getAttrValue(org.dom4j.Element el, String atName) {
    	org.dom4j.Attribute attrName = el.attribute(atName);
		if (attrName == null) {
			return null;
		}
		return attrName.getValue();
    }
    
    
	public void filter(Object p1, Object p2, Object p3) {
	
		if (!(p1 instanceof org.dom4j.Document && 
			  p2 instanceof String && 
			  p3 instanceof String)) {
				return;
		}
		
		org.dom4j.Document xml = (org.dom4j.Document) p1;
		String xPath = (String) p2 + "/child::*";
		String filterStr = (String) p3;
		org.dom4j.XPath xp = formXPathWithNS(xPath, xml, NSM_THIS_XML_DOC_TYPE);
		
		List<String> filter = new LinkedList<String>();
		
		//Формируем фильтр
		String[] filterArr = filterStr.split(",");
		for (String str : filterArr) {
			filter.add(str);
		}
		
		//Получаем ссылки
		Map<String, List<String>> refsFrom = getRefsFrom(xml, xp);
		
		List list = xp.selectNodes(xml);
		if (list != null) {
			for (Object node : list) {
				if (node instanceof org.dom4j.tree.DefaultElement) {
					org.dom4j.tree.DefaultElement el = (org.dom4j.tree.DefaultElement) node;
					String elname = getAttrValue(el, "name");
					// Если узел не нужен - удаляем
					if (!canPassElement(elname, refsFrom, filter)) {
						el.getParent().remove(el);
					}
				}		
			}
		}
	}
	
	
	/**
	 * Замена namespace в документе	
	 * @param p1 - документ	
	 * @param p2 - старый нс
	 * @param p3 - префикс нового нс	
	 * @param p4 - новый нс
	 */
	public void changeNamespace(Object p1, Object p2, Object p3, Object p4) {
		if (!(p1 instanceof org.dom4j.Document && 
			  p2 instanceof String &&
			  p3 instanceof String &&
			  p4 instanceof String)) {
				return;
			}
		
		org.dom4j.Document doc = (org.dom4j.Document) p1;
		String nsOld = (String) p2;
		String nsNewPrefix = (String) p3;
		String nsNew = (String) p4;
		
	    // retrieve the old Namespace
	    org.dom4j.Namespace oldNs = org.dom4j.Namespace.get(nsOld);
	    //Add the new name space with new prefix
	    org.dom4j.Namespace newNs = org.dom4j.Namespace.get(nsNewPrefix, nsNew);
	    // In DOM4J Navigation uses a Visitor pattern to travserse through the documnet,
	    // the same we will use to chnage the name sapce
	    org.dom4j.Visitor visitor = new NamespaceChangingVisitor(oldNs, newNs);
	    doc.getDocument().accept(visitor);
	}
	 
    class NamespaceChangingVisitor extends org.dom4j.VisitorSupport {
    	private org.dom4j.Namespace from;
        private org.dom4j.Namespace to;

        public NamespaceChangingVisitor(org.dom4j.Namespace from, org.dom4j.Namespace to) {
        	this.from = from;
            this.to = to;
        }

        public void visit(org.dom4j.Element node) {
        	org.dom4j.Namespace ns = node.getNamespace();

            if (ns.getURI().equals(from.getURI())) {
            	org.dom4j.QName newQName = new org.dom4j.QName(node.getName(), to);
                node.setQName(newQName);
            }
            
            for (org.dom4j.Attribute a : ((List<org.dom4j.Attribute>)node.attributes())) {
            	org.dom4j.Namespace ans = a.getNamespace();
                        
                if (ans.getURI().equals(from.getURI())) {
                	node.remove(a);
                    node.addAttribute(new org.dom4j.QName(a.getName(), to), a.getValue());
                }
            }
        }
    }
    
    /**
	 * Установить корневой элемент
	 * @param p1 - документ	
	 * @param p2 - XPath	
	 */
    public Object setRoot(Object p1, Object p2) {
    	if (!(p1 instanceof org.dom4j.Document && 
  			  p2 instanceof String)) {
  				return null;
  			}
  		
  		org.dom4j.Document doc = (org.dom4j.Document) p1;
  		org.dom4j.XPath xp = formXPathWithNS((String)p2, doc, NSM_THIS_XML_DOC_TYPE);
  		
  			org.dom4j.Node n = xp.selectSingleNode(doc);
  			doc.setRootElement((org.dom4j.Element)n);
  			return doc;
  		
	}

	/**
	 * Получение значения
	 * @param p1 - документ	
	 * @param p2 - XPath	
	 * @param p3 - метод (
	 * 				TEXT - получить текст 
	 * 				NODE_TEXT - получить узел в виде текста, 
	 * 				NODE_PRETTY_TEXT - получить узел в виде красивого текста,
	 * 				NODE - получить узел в новом документе org.dom4j.Document, 
	 * 			   )
	 * @param p4 - isList
	 */
    public Object extract(Object p1, Object p2, Object p3, Object p4) {
    	if (!(p1 instanceof org.dom4j.Document && 
  			  p2 instanceof String && 
  			  p3 instanceof String &&
  			  p4 instanceof Boolean)) {
  				return null;
  			}
  		
  		org.dom4j.Document doc = (org.dom4j.Document) p1;
  		org.dom4j.XPath xp = formXPathWithNS((String)p2, doc, NSM_THIS_XML_DOC_TYPE);
  		String mthd = (String)p3;
  		boolean isList = p4 == null ? true : (Boolean)p4; 
  		List<org.dom4j.Node> nList = new ArrayList<org.dom4j.Node>();
  		List<Object> rList = new ArrayList<Object>();
  		
  		if (!isList) {
  			org.dom4j.Node n_ = xp.selectSingleNode(doc);
  			if (n_ != null) nList.add(n_);
  		} else
  			nList = xp.selectNodes(doc);
  		
  		for (org.dom4j.Node n : nList) {
  		
	  		if ("TEXT".equals(mthd)) {
	  			rList.add( n.getText() );
	  		} else if ("NODE_TEXT".equals(mthd)) {
	  			rList.add( n.asXML() );
	  		} else if ("NODE_PRETTY_TEXT".equals(mthd)) {
	  			org.dom4j.io.OutputFormat format = org.dom4j.io.OutputFormat.createPrettyPrint();
	  	    	java.io.StringWriter out = new java.io.StringWriter();
	  			org.dom4j.io.XMLWriter writer = new org.dom4j.io.XMLWriter( out, format );
	  	        try {
	  	        	writer.write( n );
	  	        	writer.close();
	  	        	return out.toString();
	  	        } catch (Exception e) {
	  	        	rList.add( "<EXCEPTION><![CDATA[" + e.getMessage() + "]]></EXCEPTION>" );
	  	        }
	  		} else if ("NODE".equals(mthd)) {
	  			try {
	  				rList.add( DocumentHelper.parseText(n.asXML()) );
				} catch (DocumentException e) {
					rList.add( "<EXCEPTION><![CDATA[" + e.getMessage() + "]]></EXCEPTION>" );
				}
	  		}
	  		
  		}
  		
  		return isList ? rList : rList.size() > 0 ? rList.get(0) : null; 
	}
}
