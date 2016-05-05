package ca.uqac.lif.cornipickle;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ca.uqac.lif.bullwinkle.BnfParser;
import ca.uqac.lif.bullwinkle.ParseNode;
import ca.uqac.lif.bullwinkle.BnfParser.ParseException;

public class SubOperationTest {
	CornipickleParser parser;
	SubOperation so;
	@Before
	public void setUp() throws Exception {
		parser=new CornipickleParser();

	    String line = "(3-3)\n";

	    ParseNode pn = shouldParseAndNotNull(line, "<sub>");
	    LanguageElement e = parser.parseStatement(pn);

	    if (e == null)
	    {
	      fail("Parsing returned null");
	    }
	    if (!(e instanceof SubOperation))
	    {
	      fail("Got wrong type of object");
	    }
	    so=(SubOperation)e;

	}
	@Test
	public void testGetClone(){
		SubOperation so2;
		so2=so.getClone();
		assertTrue(so2.toString().equals(so.toString()));
	}
	@Test
	public void testSubOperation(){
		SubOperation so2=new SubOperation();
		assertTrue(so2 instanceof Operation);
	}
	@Test
	public void testToStringString() {
		assertTrue(so.toString().equals("3.0 - 3.0"));
	}
	/*@Test
	public void test() {
		fail("Not yet implemented");
	}*/
	
	  public ParseNode shouldParseAndNotNull(String line, String start_symbol){
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
