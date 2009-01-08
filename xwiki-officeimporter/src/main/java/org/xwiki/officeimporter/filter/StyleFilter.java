/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.officeimporter.filter;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This particular filter searches HTML tags containing style attributes and removes such attributes if present.
 * 
 * @version $Id$
 * @since 1.8M1
 */
public class StyleFilter implements HTMLFilter
{
    /**
     * The html_tag_name->allowed_attribute_names mappings for strict filtering mode. This is used to filter out all
     * unnecessary attributes. The mapped object is a '|' separated string of all allowed attributes. If a particular
     * tag name is not present in this map, all of it's attributes will be filtered.
     */
    private Map<String, String> attributeMappingsModerate;

    /**
     * The html_tag_name->allowed_attribute_names mappings for moderate filtering mode. This is used to filter out all
     * unnecessary attributes. The mapped object is a '|' separated string of all allowed attributes. If a particular
     * tag name is not present in this map, all of it's attributes will be filtered.
     */
    private Map<String, String> attributeMappingsStrict;

    /**
     * Filtering mode (strict, moderate, etc). Controls how much is filtered.
     */
    private String mode;

    public StyleFilter(String mode)
    {
        this.mode = mode;
        attributeMappingsStrict = new HashMap<String, String>();
        attributeMappingsStrict.put("a", "|href|");
        attributeMappingsStrict.put("img", "|alt|src|");
        attributeMappingsStrict.put("td", "|colspan|rowspan|");
        attributeMappingsModerate = new HashMap<String, String>();
        attributeMappingsModerate.put("a", "|href|");
        attributeMappingsModerate.put("img", "|alt|src|width|height|align|");
        attributeMappingsModerate.put("p", "|align|");
        attributeMappingsModerate.put("table", "|align|");
        attributeMappingsModerate.put("td", "|colspan|rowspan|");
    }

    /**
     * {@inheritDoc}
     */
    public void filter(Document document)
    {
        if(null != mode && mode.equals("strict")) {
            filter(document.getDocumentElement(), attributeMappingsStrict);
        } else if(null != mode && mode.equals("moderate")) {
            filter(document.getDocumentElement(), attributeMappingsModerate);
        }
    }

    /**
     * Removes style attributes from this node and it's children recursively.
     * 
     * @param node The node being filtered.
     */
    private final void filter(Node node, Map<String, String> attributeMappings)
    {
        if (node instanceof Element) {
            Element element = (Element) node;
            String allowedAttributes = attributeMappings.get(element.getNodeName().toLowerCase());
            NamedNodeMap currentAttributes = element.getAttributes();
            if (null == allowedAttributes) {
                // Strip off all attributes.
                while (currentAttributes.getLength() > 0) {
                    currentAttributes.removeNamedItem(currentAttributes.item(0).getNodeName());
                }
            } else {
                // Strip all attributes except those allowed.
                for (int i = 0; i < currentAttributes.getLength(); i++) {
                    String attributeName = currentAttributes.item(i).getNodeName();
                    if (allowedAttributes.indexOf("|" + attributeName.toLowerCase() + "|") == -1) {
                        currentAttributes.removeNamedItem(attributeName);
                        i--;
                    }
                }
            }
            if (node.hasChildNodes()) {
                NodeList children = node.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    filter(children.item(i), attributeMappings);
                }
            }
        }
    }
}
