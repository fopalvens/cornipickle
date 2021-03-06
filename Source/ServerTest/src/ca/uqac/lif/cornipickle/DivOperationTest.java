package ca.uqac.lif.cornipickle;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ca.uqac.lif.bullwinkle.BnfParser;
import ca.uqac.lif.bullwinkle.ParseNode;
import ca.uqac.lif.bullwinkle.BnfParser.ParseException;
import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonNumber;

public class DivOperationTest {
	CornipickleParser parser;
	DivOperation op;
	@Before
	public void setUp() throws Exception {
		parser=new CornipickleParser();
		String line = "(3/3)\n";

	    ParseNode pn = shouldParseAndNotNull(line, "<div>");
	    LanguageElement e = parser.parseStatement(pn);

	    if (e == null)
	    {
	      fail("Parsing returned null");
	    }
	    if (!(e instanceof DivOperation))
	    {
	      fail("Got wrong type of object");
	    }
	    op=(DivOperation)e;

	}

	@Test
	public void testToStringString() {
		assertTrue(op.toString().equals("3.0 / 3.0"));
	}

	@Test
	public void testEvaluateJsonElementMapOfStringJsonElement() {
		JsonElement je= new JsonNumber(21);
		JsonElement je2= new JsonNumber(15);		
		Map<String,JsonElement> test = new HashMap<String, JsonElement>();
		test.put("2", je2);			
		assertTrue(op.evaluate(je, test).toString().equals("1"));	
	}


	@Test
	public void testGetClone() {
		DivOperation op2=new DivOperation();
		op2=op.getClone();
		assertTrue(op.toString().equals(op2.toString()));
	}
	
	
	  public ParseNode shouldParseAndNotNull(String line, String start_symbol)
	  {
	    BnfParser p = parser.getParser();
	    //p.setDebugMode(true);
	    p.setStartRule(start_symbol);
	    ParseNode pn = null;

	    try
	    {
	      pn = p.parse(line);
	    } catch (ParseException e)
	    {
	      fail(e.toString());
	    }
	    if (pn == null)
	    {
	      fail("Failed parsing expression through grammar: returned null");
	    }
	    return pn;
	  }

}
