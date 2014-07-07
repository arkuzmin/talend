package ru.croc.xmltransform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class XMLTransformator {
	public boolean checkString(String str) {
		return str != null && !"".equals(str);
	}

	private org.dom4j.XPath getXPathWithNS(String xPath, org.dom4j.Document xml) {
		org.dom4j.XPath xp = xml.createXPath(xPath);
		xp.setNamespaceURIs(formNSMap(xml));
		return xp;
	}
	
	
	private Map<String, String> formNSMap(org.dom4j.Document xml) {
		List list = xml.selectNodes("//namespace::*");
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
	public org.dom4j.Element createNewElement(Object p1, Object p2) {
		
		if (!(p1 instanceof String && 
			p2 instanceof String )) {
			return null;
		}
		
		String name = (String) p1;
		String text = (String) p2;
		
		org.dom4j.Element newEl = null;

		if (checkString(name)) {
			newEl = org.dom4j.DocumentHelper.createElement(name);
			if (text != null && !"".equals(text)) {
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
			p2 instanceof String &&
			p3 instanceof String && 
			p4 instanceof org.dom4j.Document)) {
			return;
		}
		
		String name = (String) p1;
		String text = (String) p2;
		String xPath = (String) p3;
		org.dom4j.Document xml = (org.dom4j.Document) p4;
		
		org.dom4j.XPath xp = getXPathWithNS(xPath, xml);
		
		List list = xp.selectNodes(xml);
		if (list !=  null) {
			for (Object el : list) {
				// Если найденный узел - не root
				if (el instanceof org.dom4j.tree.DefaultElement) {
					org.dom4j.Element newEl = createNewElement(name, text);
					if (newEl != null ){
						((org.dom4j.tree.DefaultElement) el).add(newEl);
					}
				}
				// Если root
				else if (el instanceof org.dom4j.tree.DefaultDocument) {
					org.dom4j.Element root = xml.getRootElement();
					if (root == null) {
						org.dom4j.Element newEl = createNewElement(name, text);
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
			p2 instanceof String &&
			p3 instanceof String && 
			p4 instanceof org.dom4j.Document)) {
			return;
		}
		
		String name = (String) p1;
		String text = (String) p2;
		String xPath = (String) p3;
		org.dom4j.Document xml = (org.dom4j.Document) p4;
		org.dom4j.XPath xp = getXPathWithNS(xPath, xml);
		
		List list = xp.selectNodes(xml);
		//List list =  xml.selectNodes(xPath);
		if (list !=  null) {
			for (Object el : list) {
				if (el instanceof org.dom4j.tree.DefaultElement) {
					org.dom4j.Element newEl = createNewElement(name, text);
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
			p2 instanceof String &&
			p3 instanceof String &&
			p4 instanceof org.dom4j.Document)) {
			return;
		}
		
		String name = (String) p1;
		String text = (String) p2;
		String xPath = (String) p3;
		org.dom4j.Document xml = (org.dom4j.Document) p4;
		org.dom4j.XPath xp = getXPathWithNS(xPath, xml);
		
		List list = xp.selectNodes(xml);
	
		//List list =  xml.selectNodes(xPath);
		if (list !=  null) {
			for (Object el : list) {
				if (el instanceof org.dom4j.tree.DefaultElement) {
					org.dom4j.Element newEl = createNewElement(name, text);
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
		org.dom4j.XPath xp = getXPathWithNS(xPath, xml);
		
		List list = xp.selectNodes(xml);
	
		//List list =  xml.selectNodes(xPath);
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
	
		if (!(p1 instanceof String && 
			  p2 instanceof String &&
			  p3 instanceof org.dom4j.Document)) {
			return;
		}
		
		String name = (String) p1;
		String xPath = (String) p2;
		org.dom4j.Document xml = (org.dom4j.Document) p3;
		org.dom4j.XPath xp = getXPathWithNS(xPath, xml);
		
		List list = xp.selectNodes(xml);
	
		//List list =  xml.selectNodes(xPath);
		if (list !=  null) {
			for (Object el : list) {
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
		org.dom4j.XPath xp = getXPathWithNS(xPath, xml);
		
		List list = xp.selectNodes(xml);
	
		//List list =  xml.selectNodes(xPath);
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
			  p2 instanceof String &&
			  p3 instanceof String &&
			  p4 instanceof org.dom4j.Document)) {
			return;
		}
		
		String name = (String) p1;
		String value = (String) p2;
		String xPath = (String) p3;
		org.dom4j.Document xml = (org.dom4j.Document) p4;
		org.dom4j.XPath xp = getXPathWithNS(xPath, xml);
		
		List list = xp.selectNodes(xml);
	
		//List list =  xml.selectNodes(xPath);
		if (list !=  null) {
			for (Object el : list) {
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
	
		if (!(p1 instanceof String && 
			  p2 instanceof String &&
			  p3 instanceof org.dom4j.Document)) {
			return;
		}
		
		String text = (String) p1;
		String xPath = (String) p2;
		org.dom4j.Document xml = (org.dom4j.Document) p3;
		org.dom4j.XPath xp = getXPathWithNS(xPath, xml);
		
		List list = xp.selectNodes(xml);
	
		//List list =  xml.selectNodes(xPath);
		if (list !=  null) {
			for (Object el : list) {
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
		org.dom4j.XPath xp = getXPathWithNS(xPath, xml);
		org.dom4j.XPath xp2 = getXPathWithNS(xPath2, child);
		
		List list = xp.selectNodes(xml);
		List list2 = xp2.selectNodes(child);
		//List list =  xml.selectNodes(xPath);
    	//List list2 = child.selectNodes(xPath2);
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
		org.dom4j.XPath xp = getXPathWithNS(xPath, xml);
		org.dom4j.XPath xp2 = getXPathWithNS(xPath2, child);
		
		List list = xp.selectNodes(xml);
		List list2 = xp2.selectNodes(child);
		//List list =  xml.selectNodes(xPath);
    	//List list2 = child.selectNodes(xPath2);
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
		org.dom4j.XPath xp = getXPathWithNS(xPath, xml);
		org.dom4j.XPath xp2 = getXPathWithNS(xPath2, child);
		
		List list = xp.selectNodes(xml);
		List list2 = xp2.selectNodes(child);
		//List list =  xml.selectNodes(xPath);
   		//List list2 = child.selectNodes(xPath2);
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
		org.dom4j.XPath xp = getXPathWithNS(xPath, xml);
		
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

	
	
	
}
