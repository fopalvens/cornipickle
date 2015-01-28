/*
    Cornipickle, validation of layout bugs in web applications
    Copyright (C) 2015 Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.uqac.lif.cornipickle.server;

import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ca.uqac.lif.cornipickle.Interpreter;
import ca.uqac.lif.cornipickle.Interpreter.StatementMetadata;
import ca.uqac.lif.cornipickle.PredicateDefinition;
import ca.uqac.lif.cornipickle.SetDefinition;
import ca.uqac.lif.httpserver.RequestCallback;
import ca.uqac.lif.httpserver.Server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

class StatusPageCallback extends RequestCallback<CornipickleServer>
{
  
  public StatusPageCallback(CornipickleServer s)
  {
    super(s);
  }

  @Override
  public boolean fire(HttpExchange t)
  {
    URI u = t.getRequestURI();
    String path = u.getPath();
    return path.compareTo("/status") == 0;
  }

  @Override
  public boolean process(HttpExchange t)
  {
    StringBuilder page = new StringBuilder();
    page.append(pageHead("Cornipickle status"));
    page.append("<h1>Cornipickle Status</h1>");
    
    // Show last contact with probe
    createProbeContactMessage(m_server.getLastProbeContact(), page);
    
    // Compute verdicts
    Map<StatementMetadata,Interpreter.Verdict> verdicts = m_server.m_interpreter.getVerdicts();
    createStatusMessage(verdicts, page);
    
    //page.append("<div id=\"main-accordion\" class=\"ui-accordion\">\n");
    
    // Show properties
    page.append("<h2 class=\"ui-accordion-header\">Properties</h2>\n");
    //page.append("<div class=\"ui-accordion-content\">\n");
    createPropertyList(verdicts, page);
    //page.append("</div>\n");
    page.append("<div class=\"clearer\"></div>\n");
    
    // Show predicates
    page.append("<h2 class=\"ui-accordion-header\">Predicates</h2>\n");
    //page.append("<div class=\"ui-accordion-content\">\n");
    createPredicateList(m_server.m_interpreter.getPredicates(), page);
    //page.append("</div>\n");
    page.append("<div class=\"clearer\"></div>\n");
    
    // Show sets
    page.append("<h2 class=\"ui-accordion-header\">Sets</h2>\n");
    //page.append("<div class=\"ui-accordion-content\">\n");
    createSetList(m_server.m_interpreter.getSetDefinitions(), page);
    //page.append("</div>\n");
    page.append("<div class=\"clearer\"></div>\n");
    
    //page.append("</div> <!-- /main-accordion -->\n");

    // Show box to add new properties
    page.append("\n<div id=\"add-properties\">\n");
    page.append("<h2>Add properties</h2>\n\n");
    page.append("<p>Type here the Cornipickle properties you want to add.</p>\n");
    page.append("<form method=\"post\" action=\"add\">\n");
    page.append("<div><textarea name=\"properties\" rows=\"10\" cols=\"40\"></textarea></div>\n");
    page.append("<input type=\"submit\" />\n");
    page.append("</form>\n");
    page.append("</div>\n");
    page.append(pageFoot());
    String page_string = page.toString();
    // Disable caching on the client
    Headers h = t.getResponseHeaders();
    h.add("Pragma", "no-cache");
    h.add("Cache-Control", "no-cache, no-store, must-revalidate");
    h.add("Expires", "0"); 
    m_server.sendResponse(t, Server.HTTP_OK, page_string, "text/html");
    return true;
  }

  protected void createSetList(List<SetDefinition> sets, StringBuilder page)
  {
    page.append("<ul class=\"sets\">\n");
    for (SetDefinition sd : sets)
    {
        page.append("<li>").append(HtmlFormatter.format(sd)).append("</li>\n");
    }
    page.append("</ul>"); 
  }
  
  protected void createPredicateList(List<PredicateDefinition> preds, StringBuilder page)
  {
    page.append("<ul class=\"predicates\">\n");
    for (PredicateDefinition pd : preds)
    {
        page.append("<li class=\"predicate-definition-item ").append(pd.getRuleName()).append("\">");
        page.append(HtmlFormatter.format(pd)).append("</li>\n");
    }
    page.append("</ul>");    
  }
  
  protected static void createProbeContactMessage(Date last_contact, StringBuilder page)
  {
    if (last_contact == null)
    {
        page.append("<p id=\"last-probe-contact\">No contact with probe so far.</p>\n");
    }
    else
    {
        page.append("<p id=\"last-probe-contact\">Last contact with probe on ");
        page.append(last_contact).append("</p>\n");
    }
  }
  
  protected static void createStatusMessage(Map<StatementMetadata,Interpreter.Verdict> verdicts, StringBuilder page)
  {
    int num_errors = 0;
    for (StatementMetadata key : verdicts.keySet())
    {
      Interpreter.Verdict v = verdicts.get(key);
      if (v == Interpreter.Verdict.FALSE)
      {
        num_errors++;
      }
    }
    if (num_errors == 0)
    {
        if (verdicts.isEmpty())
        {
            page.append("<p class=\"verdicts-empty\">Cornipickle has no property to evaluate.</p>");
        }
        else
        {
            page.append("<p class=\"verdicts-no-error\">All's well! All properties evaluate to true.</p>"); 
        }
    }
    else if (num_errors == 1)
    {
        page.append("<p class=\"verdicts-errors\">Oops! There is a problem with some property.</p>");
    }
    else
    {
        page.append("<p class=\"verdicts-errors\">Oops! There is a problem with ").append(num_errors).append(" properties.</p>");
    }
  }
  
  protected void createPropertyList(Map<StatementMetadata,Interpreter.Verdict> verdicts, StringBuilder verdict_string)
  {
    // Ignore unique ID given by interpreter to each statement
    HashSet<String> ignored_attributes = new HashSet<String>();
    ignored_attributes.add("uniqueid");
    verdict_string.append("<ul class=\"verdicts\">\n");
    for (StatementMetadata key : verdicts.keySet())
    {
      Interpreter.Verdict v = verdicts.get(key);
      String class_name = "inconclusive";
      if (v == Interpreter.Verdict.TRUE)
      {
        class_name = "true";
      }
      else if (v == Interpreter.Verdict.FALSE)
      {
        class_name = "false";
      }
      verdict_string.append("<li class=\"").append(class_name).append("\">");
      verdict_string.append("<div class=\"property-metadata\">").append(HtmlFormatter.format(key, ignored_attributes)).append("</div>\n");
      verdict_string.append("<div class=\"property-contents\">\n");
      verdict_string.append(HtmlFormatter.format(m_server.m_interpreter.getProperty(key)));
      verdict_string.append("</div>\n");
      verdict_string.append("</li>");
    }
    verdict_string.append("</ul>\n");    
  }
  
  protected StringBuilder pageHead(String title)
  {
    StringBuilder page = new StringBuilder();
    page.append("<!DOCTYPE html>\n");
    page.append("<html>\n");
    page.append("<head>\n");
    page.append("<title>").append(title).append("</title>\n");
    page.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"https://code.jquery.com/ui/1.11.1/themes/smoothness/jquery-ui.css\" />\n");
    page.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"screen.css\" />\n");
    page.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"colouring.css\" />\n");
    page.append("<script src=\"https://code.jquery.com/jquery-1.11.2.min.js\"></script>\n");
    page.append("<script src=\"https://code.jquery.com/ui/1.11.1/jquery-ui.min.js\"></script>\n");
    page.append("<script src=\"highlight.js\"></script>\n");
    page.append("</head>\n<body>\n");
    return page;
  }
  
  protected StringBuilder pageFoot()
  {
    StringBuilder page = new StringBuilder();
    page.append("<div id=\"footer\">\n");
    page.append("<hr />\n");
    Date d = new Date();
    page.append(d);
    page.append("</div>\n");
    page.append("</body>\n</html>\n");
    return page;
  }
} 