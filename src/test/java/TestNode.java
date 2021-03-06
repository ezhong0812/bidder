package test.java;

import static org.junit.Assert.*;


import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jacamars.dsp.rtb.common.Campaign;
import com.jacamars.dsp.rtb.common.Configuration;
import com.jacamars.dsp.rtb.common.Node;
import com.jacamars.dsp.rtb.common.SortNodesFalseCount;
import com.jacamars.dsp.rtb.db.DataBaseObject;
import com.jacamars.dsp.rtb.db.User;
import com.jacamars.dsp.rtb.pojo.BidRequest;
import com.jacamars.dsp.rtb.tools.DbTools;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the constraint node processing.
 * @author Ben M. Faul
 *
 */
public class TestNode {
	/** The GSON object the class will use */
	/** The list of constraint nodes */
	static List<Node> nodes = new ArrayList<Node>();
	
	@BeforeClass
	public static void setup() {
		try {
			Config.setup();
			System.out.println("******************  TestNode");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void stop() {
		Config.teardown();
	}
	
	  
/*	@Test
	public void testOs() throws Exception {
		List<String> parts = new ArrayList();
		parts.add("Android");
		Node node = new Node("test","device.os","MEMBER",parts);
		node.notPresentOk = false;
		BufferedReader br = new BufferedReader(new FileReader("../requests"));
		for(String line; (line = br.readLine()) != null; ) {
			BidRequest bidR = new BidRequest(new StringBuilder(line));
			boolean x = node.test(bidR);
			if (x == true) {
				JsonNode os = (JsonNode)bidR.interrogate("device.os");
				if (os.textValue() == null) {
					System.out.println("NULL");
					return;
				}
				assertTrue(os.textValue().equals("Android"));
			}
		}
		
	} */
	
	/**
	 * Trivial test of the payload atributes  
	 * @throws Exception on configuration file errors.
	 */
	@Test 
	public void makeSimpleCampaign() throws Exception {
		
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/nexage.txt"));
		assertNotNull(br);
		
		List<Campaign> camps = DataBaseObject.getInstance().getCampaigns();
		assertNotNull(camps);
		
		Campaign c = null;
		for (Campaign x : camps) {
			if (x.adId.equals("ben:payday")) {
				c = x;
				break;
			}
		}
		
		Node n = c.getAttribute("site.domain");
		assertNotNull(n);                          
		List<String> list = (List<String>)n.value;
		assertNotNull(list);
		String op = (String)n.op;
		assertTrue(op.equals("NOT_MEMBER"));
	
	}

	@Test
	public void testSubDomain() throws Exception {
		List<String> list = new ArrayList<String>();
		list.add("yahoo.com");
		list.add("finance.yahoo.com");
		Node n =  new Node("subdomainTest", "site.domain","MEMBER", list);
		boolean b = n.testInternal("yahoo.com");
		assertTrue(b);
		b = n.testInternal("finance.yahoo.com");
		assertTrue(b);
		b = n.testInternal("this.notmatch.com");
	}

	
	@Test
	public void getIntAndDoubleValue() throws Exception {
		Node node = new Node("intTest","user.yob","EQUALS",1961);
		Integer ix = node.intValue();
		assertEquals(ix.intValue(),1961);
		
		Double dx = node.doubleValue();
		assertTrue(dx.doubleValue()==1961.0);
	}
	
	@Test
	public void testOperatorsInImpressions() throws Exception {
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/c1x.txt"));
		br.setExchange( "c1x" );
		assertNotNull(br);
		
		
	/*	Node node = new Node("=","imp.0.banner.tagid","EQUALS","14404-loge-300x25");
		boolean b = node.test(br);	   // true means the constraint is satisfied.
		assertTrue(b);    
		
		br = new BidRequest(Configuration.getInputStream("SampleBids/c1xMulti.txt"));
		br.setExchange( "c1x" );
		assertNotNull(br);
		node = new Node("=","imp.0.banner.id","EQUALS","2");
		b = node.test(br);	   // true means the constraint is satisfied.
		assertFalse(b); 
		
		node = new Node("=","imp.1.banner.id","EQUALS","2");
		b = node.test(br);	   // true means the constraint is satisfied.
		assertTrue(b); */
		 
		br = new BidRequest(Configuration.getInputStream("SampleBids/c1xMulti.txt"));
		ArrayList<String> list = new ArrayList<String>();
		list.add("2");
		Node node = new Node("=","imp.*.banner.id",Node.INTERSECTS,list);
		boolean b = node.test(br,null);	   // true means the constraint is satisfied.
		assertTrue(b);
	}

	/**
	 * Tests that br does not have the app key
	 * @throws Exception on io errors.
	 */
	@Test
	public void testNotMemberWithBidRequest() throws Exception {
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/nexage.txt"));
		br.setExchange( "nexage" );
		assertNotNull(br);
		Node node = new Node("nm","app",Node.NOT_EXISTS,null);
		boolean b = node.test(br,null);	   // true means the constraint is satisfied.
		assertTrue(b);
	}

	@Test
	public void testMobile() throws Exception {
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/nexage.txt"));
		br.setExchange( "nexage" );
		assertNotNull(br);
		Node node = new Node("nm","device.ua",Node.REGEX, ".*Mobi.*");
		boolean b = node.test(br,null);	   // true means the constraint is satisfied.
		assertTrue(b);
	}
	
/**
 * Test the various operators of the constraints.
 * @throws Exception on file errors in configuration file.
 */
	@Test
	public void testOperators() throws Exception {
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/nexage.txt"));
		BidRequest brFloor = new BidRequest(Configuration.getInputStream("SampleBids/nexageBidFloor.txt"));
		br.setExchange( "nexage" );
		brFloor.setExchange("nexage");
		assertNotNull(br);

		List<Campaign> camps = DataBaseObject.getInstance().getCampaigns();

		assertNotNull(camps);
		
		Campaign c = null;
		for (Campaign x : camps) {
			if (x.adId.equals("ben:payday")) {
				c = x;
				break;
			}
		}
		
		Node n = c.getAttribute("site.domain");
		List<String> list = (List<String>)n.value;
		list.add("junk1.com");										// add this to the campaign blacklist
		String op = "NOT_MEMBER";
		Node node = new Node("blacklist","site.domain",op,list);
		
		list.add("junk1.com");
		Boolean b = node.test(br,null);	   // true means the constraint is satisfied.
		assertFalse(b);                // should be on blacklist and will not bid
		
		op = "MEMBER";
		node = new Node("blacklist","site.domain",op,list);
		b = node.test(br,null);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid
		
		// Test emptry string not allowed.
		list.clear();
		list.add("");
		node = new Node("blacklist","site.domain",op,list);
		b = node.test(br,null);	   // true means the constraint is satisfied.
		assertFalse(b);         // should be on blacklist and will not bid
		
		op = "EQUALS";
		node = new Node("=","user.yob",op,1961);
		b = node.test(br,null);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid */
		
		String str = node.getLucene();
		System.out.println(str);
		
		node = new Node("=","user.yob",op,1960);
		b = node.test(br,null);	   // true means the constraint is satisfied.
		assertFalse(b);         // should be on blacklist and will not bid */
		node = new Node("=","user.yob",op,1962);
		b = node.test(br,null);	   // true means the constraint is satisfied.
		assertFalse(b);         // should be on blacklist and will not bid */
		
		op = "NOT_EQUALS";
		node = new Node("!=","user.yob",op,1901);
		b = node.test(br,null);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid */
		
		op = "LESS_THAN";
		node = new Node("<","user.yob",op,1960);
		b = node.test(br,null);	   // true means the constraint is satisfied.
		assertFalse(b);         // should be on blacklist and will not bid */
		
		op = "LESS_THAN_EQUALS";
		node = new Node("<=","user.yob",op,1960);
		b = node.test(br,null);	   // true means the constraint is satisfied.
		assertFalse(b);         // should be on blacklist and will not bid */

		op = "LESS_THAN_EQUALS";
		node = new Node("LESS_THAN_EQUALS","imp.0.bidfloor",op,.4);
		node.notPresentOk = false;
		b = node.test(br,null);	   // true means the constraint is satisfied.
		assertFalse(b);         // should be on blacklist and will not bid */

		////////////////////////////////////////////////
		op = "LESS_THAN_EQUALS";
		node = new Node("LESS_THAN_EQUALS","imp.0.bidfloor",op,.5);
		node.notPresentOk = false;
		b = node.test(brFloor,null);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid */

		op = "LESS_THAN_EQUALS";
		node = new Node("LESS_THAN_EQUALS","imp.0.bidfloor",op,.6);
		node.notPresentOk = false;
		b = node.test(brFloor,null);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid */
		
		op = "GREATER_THAN";
		node = new Node(">","user.yob",op,1960);
		b = node.test(brFloor,null);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid */
		
		op = "GREATER_THAN_EQUALS";
		node = new Node(">=","user.yob",op,1960);
		b = node.test(brFloor,null);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid */
		////////////////////////////////////////////////////////////////
		
		op = "DOMAIN";
		List<Double> range = new ArrayList<Double>();
		range.add(new Double(1960));
		range.add(new Double(1962));
		node = new Node("inrangetest","user.yob",op,range);
		b = node.test(br,null);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid */
		
		
		BidRequest brx = new BidRequest(Configuration.getInputStream("SampleBids/msie.txt"));
		brx.setExchange( "nexage" );
		op = "REGEX";
		node = new Node("regex","device.ua",op,".*MSIE.*");
		b = node.test(brx,null);
		assertTrue(b);
		
		op = "NOT_REGEX";
		node = new Node("regex","device.ua",op,".*MSIE.*");
		b = node.test(brx,null);
		assertFalse(b);
		
		op = "NOT_REGEX";
		node = new Node("regex","device.ua",op,".*MSIE.*");
		b = node.test(br,null);
		assertTrue(b);
		
		op = "STRINGIN";
		node = new Node("stringintest","site.page",op,"xxx");
		b = node.test(br,null);	   // true means the constraint is satisfied.
		assertFalse(b);         // should be on blacklist and will not bid */
		
		op = "NOT_STRINGIN";
		node = new Node("stringintest","site.page",op,"xxx");
		b = node.test(br,null);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid */
		
		op = "STRINGIN";
		node = new Node("stringintest","site.page",op,"nexage");
		b = node.test(br,null);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid */
		
		op = "NOT_STRINGIN";
		node = new Node("stringintest","site.page",op,"nexage");
		b = node.test(br,null);	   // true means the constraint is satisfied.
		assertFalse(b);         // should be on blacklist and will not bid */
		
		
		op = "STRINGIN";
		list = new ArrayList<String>();
		list.add("nexage");
		list.add("xxx");
		
		node = new Node("stringintest","site.page",op,list);
		b = node.test(br,null);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid */
		
		op = "STRINGIN";
		String [] parts = new String[2];
		parts[0] = "nexage";
		parts[1] = "xxx";
		
		node = new Node("stringintest","site.page",op,parts);
		b = node.test(br,null);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid */
		
		node = new Node("exchangetest","exchange","EQUALS","smartyads");
		b = node.test(br,null);
		assertFalse(b);
		
		Arrays.asList("site.name", "app.name");
	//	node = new Node("eitheror",Arrays.asList("site.domain", "app.domain"),"EQUALS","smartyads");
		b = node.test(br,null);
		assertFalse(b);
		
		
		br = new BidRequest(Configuration.getInputStream("SampleBids/atomx.txt"));
		br.setExchange( "atomx" );
		assertNotNull(br);
		List<Integer> ilist = new ArrayList<Integer>();
		ilist.add(2);
		node = new Node("devicetypes", "device.devicetype", Node.INTERSECTS,ilist);
		b = node.test(br,null);
		assertTrue(b);
		
		br = new BidRequest(Configuration.getInputStream("SampleBids/atomx.txt"));
		br.setExchange("atomx" );
		assertNotNull(br);
	    ilist = new ArrayList<Integer>();
		ilist.add(1);
		ilist.add(4);
		ilist.add(5);
		node = new Node("devicetypes", "device.devicetype", Node.INTERSECTS,ilist);
		b = node.test(br,null);
		assertFalse(b);
		
		br = new BidRequest(Configuration.getInputStream("SampleBids/atomx.txt"));
		br.setExchange( "atomx" );
		assertNotNull(br);
	    ilist = new ArrayList<Integer>();
		ilist.add(1);
		ilist.add(2);
		ilist.add(5);
		node = new Node("devicetypes", "device.devicetype", Node.INTERSECTS,ilist);
		b = node.test(br,null);
		assertTrue(b);
		
		br = new BidRequest(Configuration.getInputStream("SampleBids/atomx.txt"));
		br.setExchange( "atomx" );
		assertNotNull(br);
		node = new Node("site-test", "site", Node.EXISTS,null);
		b = node.test(br,null);
		assertTrue(b);
		
		br = new BidRequest(Configuration.getInputStream("SampleBids/atomx.txt"));
		br.setExchange( "atomx" );
		assertNotNull(br);
		node = new Node("aoo-test", "app", Node.EXISTS,null);
		node.notPresentOk = false;
		b = node.test(br,null);
		assertFalse(b);
		
		br = new BidRequest(Configuration.getInputStream("SampleBids/atomx.txt"));
		br.setExchange( "atomx" );
		assertNotNull(br);
		node = new Node("aoo-test", "app", Node.NOT_EXISTS,null);
		b = node.test(br,null);
		assertTrue(b);
		
		ilist.clear();
		ilist.add(1);
		ilist.add(2);
		ilist.add(3);
		node = new Node("aoo-test", "member test", Node.MEMBER,1);
		boolean test = node.testInternal(ilist);
	

	} 
	
	@Test
	public void testMember() throws Exception {
		ArrayList<Comparable> ilist = new ArrayList<Comparable>();
		ilist.clear();
		ilist.add(1);
		ilist.add(2);
		ilist.add(3);
		Node node = new Node("aoo-test", "member test", Node.MEMBER,1);
		boolean test = node.testInternal(ilist);
		assertTrue(test);
		
		node = new Node("aoo-test", "member test", Node.NOT_MEMBER,1);
		test = node.testInternal(ilist);
		assertFalse(test);
		
		ilist.clear();
		ilist.add("one");
		ilist.add("two");
		ilist.add("three");
		node = new Node("aoo-test", "member test", Node.MEMBER,"two");
		test = node.testInternal(ilist);
		assertTrue(test);
		
		node = new Node("aoo-test", "member test", Node.NOT_MEMBER,"two");
		test = node.testInternal(ilist);
		assertFalse(test);
	}
	
	@Test
	public void testOr() throws Exception {
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/atomx.txt"));
		br.setExchange( "atomx" );
		assertNotNull(br);
	    List<Node> ilist = new ArrayList<Node>();
	    Node a = new Node("site","site.publisher.id",Node.EQUALS,"3456");
	    ilist.add(a);
	    
	    a = new Node("site","app.publisher.id",Node.EQUALS,"3456");
	    ilist.add(a);

		Node node = new Node("ortest", null, Node.OR,ilist);
		Boolean b = node.test(br,null);
		assertTrue(b);
		
		// reverse order
		ilist = new ArrayList<Node>();
		a = new Node("site","app.publisher.id",Node.EQUALS,"3456");
		ilist.add(a);

		a = new Node("site","site.publisher.id",Node.EQUALS,"3456");
		ilist.add(a);
		    
		  
		node = new Node("ortest", null, Node.OR,ilist);
		b = node.test(br,null);
		assertTrue(b);
		
		// actual is second and bad
		ilist = new ArrayList<Node>();
		a = new Node("app","app.publisher.id",Node.EQUALS,"3456");
		ilist.add(a);

		a = new Node("site","site.publisher.id",Node.EQUALS,"666");
		ilist.add(a);
		    	  
		node = new Node("ortest", null, Node.OR,ilist);
		b = node.test(br,null);
		assertFalse(b);
		
		// actual is first and bad
		ilist = new ArrayList<Node>();
		a = new Node("site","site.publisher.id",Node.EQUALS,"666");
		ilist.add(a);
		
		a = new Node("app","app.publisher.id",Node.EQUALS,"3456");
		ilist.add(a);
				    	  
		node = new Node("ortest", null, Node.OR,ilist);
		b = node.test(br,null);
		assertFalse(b);
		
	}
	
	@Test
	public void testQueryMap() throws Exception {
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/atomx.txt"));
		br.setExchange( "atomx" );
		assertNotNull(br);
		List<Comparable> ilist = new ArrayList<Comparable>();
		ilist.add("builtin");
		ilist.add("test");
		ilist.add("EQUALS");
		ilist.add(1);
		
		Node node = new Node("query", "site.publisher.id", Node.QUERY,ilist);
		boolean b = node.test(br,null);
		assertTrue(b);

	}
	
	/**
	 * Test the set operations.
	 * @throws Exception on configuration file errors.
	 */
	@Test
	public void testSets() throws Exception {
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/nexage.txt"));
		assertNotNull(br);

		List<Campaign> camps = DataBaseObject.getInstance().getCampaigns();
		assertNotNull(camps);
		
		Campaign c = null;
		for (Campaign x : camps) {
			if (x.adId.equals("ben:payday")) {
				c = x;
				break;
			}
		}

		Node n = c.getAttribute("site.domain");
		List<String> list = (List<String>)n.value;
		
		list.add("junk.com");							
		String op = "INTERSECTS";
		Node node = new Node("blacklist","site.domain",op,list);
		Boolean b = node.test(br,null);	   	// true means the constraint is satisfied.
		assertFalse(b);         		// should be on blacklist and will not bid 
		
		
		/** 
		 * Test adding an array of objects
		 */
		String [] parts = new String[1];
		op = "INTERSECTS";
		parts[0] = "junk.com";
		node = new Node("blacklist-array","site.domain",op,parts);
		b = node.test(br,null);	   // true means the constraint is satisfied.
		assertFalse(b);         // should be on blacklist and will not bid 
		
		n = new Node("matching-categories","site.cat",Node.INTERSECTS,parts);
		
		
		list.add("junk1.com");
		node = new Node("blacklist","site.domain",op,list);
		b = node.test(br,null);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid 
		
		list.clear();
		node = new Node("blacklist","site.domain",op,list);
		b = node.test(br,null);	   // true means the constraint is satisfied.
		assertFalse(b);         // should be on blacklist and will not bid 
		
		op = "NOT_INTERSECTS";
		node = new Node("blacklist","site.domain",op,list);
		b = node.test(br,null);	   // true means the constraint is satisfied.
		assertTrue(b);         // should be on blacklist and will not bid 


		op = "MEMBER";
		node = new Node("mimes","imp.0.banner.mimes",op,"image/jpg");
		b = node.test(br,null);
		assertTrue(b);
	}

	@Test
    public void testSorter() throws Exception {
	    List<Node> list = new ArrayList<Node>();

        Node node = new Node("5","device.ip","MEMBER", "@CIDR");
        node.setFalseCount(5);
        list.add(node);

        node = new Node("10","device.ip","MEMBER", "@CIDR");
        node.setFalseCount(10);
        list.add(node);

        node = new Node("20","device.ip","MEMBER", "@CIDR");
        node.setFalseCount(20);
        list.add(node);

        node = new Node("20-1","device.ip","MEMBER", "@CIDR");
        node.setFalseCount(20);
        list.add(node);

        node = new Node("13","device.ip","MEMBER", "@CIDR");
        node.setFalseCount(13);
        list.add(node);

        node = new Node("100","device.ip","MEMBER", "@CIDR");
        node.setFalseCount(100);
        list.add(node);

        SortNodesFalseCount k = new SortNodesFalseCount();

        Collections.sort(list,k);

        node = list.get(0);
        assertTrue(node.name.equals("100"));
        node = list.get(list.size()-1);
        assertTrue(node.name.equals("5"));
        for (int i=0;i<list.size();i++) {
            System.out.println(list.get(i).name);
        }

    }
	
	@Test
	public void testNavMap() throws Exception {
		String content = new String(Files.readAllBytes(Paths.get("SampleBids/nexage.txt")));
		String test = content.replace("166.137.138.18", "45.33.224.0");
		
		BidRequest br = new BidRequest(new StringBuilder(test));
		assertNotNull(br);
		
		String op = "MEMBER";
		Node node = new Node("navmap","device.ip","MEMBER", "@CIDR");
		boolean b = node.test(br,null);
		assertTrue(b);
		
		test = content.replace("166.137.138.18", "45.33.239.255");
		br = new BidRequest(new StringBuilder(test));
		b = node.test(br,null);
		assertTrue(b);
		
		test = content.replace("166.137.138.18", "166.55.255.255");
		br = new BidRequest(new StringBuilder(test));
		b = node.test(br,null);
		assertFalse(b);
		
	}
	
	@Test
	public void testMemberOfBuiltin() throws Exception {
		BidRequest br = new BidRequest(Configuration.getInputStream("SampleBids/nexage.txt"));
		assertNotNull(br);
		String op = "MEMBER";
		Node node = new Node("mimes","imp.0.banner.mimes",op,"image/jpg");
		boolean b = node.test(br,null);
		assertTrue(b);
	}
	
	/**
	 * Get the attributes of the bidRequestValues of the specified name 'what'/
	 * @param attr List. The list of various attributes.
	 * @param what String. The name you are looking for.
	 * @return Map. The attributes of the requested name.
	 */
	public Map<String, Object> getAttr(List<Map<String,Object>> attr, String what) {
		Map<String, Object> m = null;
		for (int i = 0; i< attr.size(); i++) {
			m = attr.get(i);
			List<String>brv = (List<String>)m.get("bidRequestValues");
			String s = "";
			for (int j=0;j<brv.size();j++) {
				s = s + brv.get(j);
				if (j != brv.size()-1)
					s += ".";
			}
			if (what.equals(s))
				return m;
		}
		return m;
	}
}
