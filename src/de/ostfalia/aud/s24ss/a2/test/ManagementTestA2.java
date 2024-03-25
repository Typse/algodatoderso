package de.ostfalia.aud.s24ss.a2.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.DisableOnDebug;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import de.ostfalia.aud.s24ss.a1.Employee;
import de.ostfalia.aud.s24ss.a2.Management;
import de.ostfalia.aud.s24ss.base.Department;
import de.ostfalia.aud.s24ss.base.IEmployee;
import de.ostfalia.aud.s24ss.base.IManagement;
import de.ostfalia.junit.annotations.AfterMethod;
import de.ostfalia.junit.annotations.Define;
import de.ostfalia.junit.annotations.TestDescription;
import de.ostfalia.junit.base.IMessengerRules;
import de.ostfalia.junit.base.IScoreRule;
import de.ostfalia.junit.base.ITraceRules;
import de.ostfalia.junit.common.Enumeration;
import de.ostfalia.junit.common.Fraction;
import de.ostfalia.junit.common.Version;
import de.ostfalia.junit.conditional.PassTrace;
import de.ostfalia.junit.evaluation.Evaluate;
import de.ostfalia.junit.evaluation.collectors.CsvCollector;
import de.ostfalia.junit.evaluation.collectors.FunctionalCollector;
import de.ostfalia.junit.evaluation.collectors.ICollected;
import de.ostfalia.junit.evaluation.collectors.ICollectorItem;
import de.ostfalia.junit.execution.TestCondition;
import de.ostfalia.junit.processing.Prevent;
import de.ostfalia.junit.rules.MessengerRule;
import de.ostfalia.junit.rules.RuleControl;
import de.ostfalia.junit.rules.ScoreRule;
import de.ostfalia.junit.rules.TraceRule;
import de.ostfalia.junit.runner.TopologicalSortRunner;

@RunWith(TopologicalSortRunner.class)
public class ManagementTestA2 {

	public static final int MAXVIEW = 12;
	
	public RuleControl opt = RuleControl.NONE;
	public IMessengerRules messenger = MessengerRule.newInstance(opt);	
	public ITraceRules trace = TraceRule.newInstance(opt);
	private ITraceRules traceL1 = trace.newSubtrace(opt);
	private ITraceRules traceL2 = traceL1.newSubtrace(opt);
	public static IScoreRule score = ScoreRule.newInstance(RuleControl.NONE);

	@Rule
	public TestRule chain = RuleChain
							.outerRule(trace)
							.around(score)
							.around(messenger);
	
	@Rule
    public TestRule timeout = new DisableOnDebug(
                              new Timeout(1000, TimeUnit.MILLISECONDS));
	
	private String callMsg = "Sammeln der Datensaetze.";
	private String evalMsg = "Auswerten der erhaltenen Datensaetze.";
	private String duplMsg = "Es druerfen keine doppelten Schluessel vorkommen.";
	private String errMsg  = "Fehlerhafte(r) Datensaetze/-satz in der Mitarbeiterverwaltung.";
	
	private CsvCollector<Integer, IEmployee> csvCollector = 
			new CsvCollector<>(traceL1, "%s;%s;%s;%s;%s;%s;%s", 
					          (c, p) -> Integer.parseInt((String) p[0]));
	
	private FunctionalCollector<Integer, IEmployee> funcCollector = 
			new FunctionalCollector<>(traceL2, "%s", 
					(c, o) -> (o != null) ? o.getKey() : c.index());
	
	private Comparator<Integer> reverseComp = (e1, e2) -> -Integer.compare(e1, e2);
	
	private Evaluate eval = new Evaluate(traceL2).occurrenceLimits(MAXVIEW);
	
	/**
	 * Datensatz mit 10 Eintraegen als Testdaten fuer die JUnit-Tests.
	 */
	private static String[] data = {
			/*[0]*/ "10855;1957-08-07;Breannda;Billingsley;F;1991-08-05;Finance",
			/*[1]*/ "10041;1959-08-27;Uri;Lenart;F;1989-11-12;Sales",
			/*[2]*/ "10942;1952-08-08;Toshimitsu;Larfeldt;F;1989-09-08;Development",
			/*[3]*/ "10034;1962-12-29;Bader;Swan;M;1988-09-21;Sales",
			/*[4]*/ "10943;1955-11-19;Berna;Skafidas;M;1988-02-19;Development",
			/*[5]*/ "10938;1958-05-11;Shaowei;Iisaku;F;1985-09-24;Marketing",
			/*[6]*/ "10796;1959-06-30;Breannda;Billingsley;F;1990-11-08;Management",
			/*[7]*/ "10005;1955-01-21;Kyoichi;Maliniak;M;1989-09-12;Manpower",
			/*[8]*/ "10060;1961-10-15;Breannda;Billingsley;F;1987-11-02;Service",
			/*[9]*/ "10948;1952-12-23;Shigehito;Brodie;M;1996-09-30;Development",
	};
	 
	public static ICollected<Integer, IEmployee> smallData = null;
	public static ICollected<Integer, IEmployee> sortData = null;
	
	@BeforeClass
	public static void beforeClass() {
		TestCondition.clear();
		score.onEvent(ScoreRule.Event.PASS, t -> t.inc("1"));
	}
	
	@Before
	public void setUp() throws Exception {
		assertTrue(Version.INCOMPATIBLE, Version.request("4.6.3"));
		score.testScale(new Fraction("1/20"));
		PassTrace.preProcessor(PassTrace.CONDITIONS, Prevent.signalNull);
		csvCollector.useSeparator("\\s*;\\s*")
				    .useIdentifier("employee", "[", "]");
		funcCollector.define("toString", (obj, c) -> obj.toString());
		if (smallData == null) {			
			trace.add("Erstellen von %d Mitarbeitern.", data.length);
			smallData = csvCollector.collectAll(data);
			sortData = smallData.sort();
		}		
	}
	
	/**
	 * <ul>
	 * 	<li>Ueberpruefen der Klasse Employee: <br>
	 * 		Konstruktoraufruf der Klasse Employee und auswerten
	 * 		der Rueckgaben der Methoden getKey() und toString().</li>
	 *	<li>Erwartet: <br>
	 *		Konstruktor wird erfolgreich durchlaufen, die
	 *		Rueckgaben der Methoden entsprechen den erwarteten Werten.</li>
	 *	<li>Beispiel: "10855;1957-08-07;Breannda;Billingsley;F;1991-08-05;Finance"</li>
	 * </ul>
	 */
	@Test (timeout = 1000)	
	@TestDescription("Testen der Employee-Klasse.")
	public void testEmployee() {
		if (!TestCondition.isDefined()) {
			TestCondition.set("Employee");
			score.suspendTest();
			trace.add("Ueberpruefen der Employee-Klasse.");
			traceL1.add(Evaluate.callConstruktor("employee",Employee.class, data[0]));
			IEmployee empl = new Employee(data[0]);
			traceL1.add(Evaluate.callConstruktor(Employee.class, data[1]));
			new Employee(data[1]);

			traceL1.add(Evaluate.callMethod("employee", "getKey"));
			int key = empl.getKey();
			traceL1.addInfo(PassTrace.ifEquals("Rueckgabe der Methode.", 10855, key));

			traceL1.add(Evaluate.callMethod("employee", "toString"));
			String str = empl.toString();
			traceL1.addInfo(PassTrace.ifEquals("Rueckgabe der Methode.", data[0], str));
			trace.addDependentTrace(true);

			assertFalse("Employee-Klasse fehlerhaft implementiert.", trace.hasOccurrences());
			TestCondition.set("EmployeeSuccess");
		}
	}
	
	/**
	 * <ul>
	 * 	<li>Testfall: <br>
	 * 		Standard-Konstruktoraufruf Management().<br> 
	 * 		Nach dem Aufruf des Standard-Konstruktors duerfen sich keine Datensaetze
	 *      in der Mitarbeiterverwaltung befinden.</li>
	 *	<li>Erwartet: <br>
	 *			 Anzahl der Datensaetze = 0.</li>

	 * </ul>
	 */
	@Test
	@Define("EmployeeSuccess")
	@AfterMethod("testEmployee")
	@TestDescription("Testen des Kontruktors().")
	public void testKonstruktorOhneParameter() {
		testEmployee();
		trace.add("Konstruktoraufruf Management().");
		IManagement mgnt = new Management();
		
		evaluate(mgnt, true, sortData.with());
	}
	
	/**
	 * <ul>
	 * 	<li>Testfall: <br>
	 * 		Konstruktoraufruf Management(String[]) mit einem Datensatz.<br> 
	 * 		Nach dem Aufruf des Konstruktors muss sich genau ein Datensatz (10796)
	 * 		in der Mitarbeiterverwaltung befinden.</li>
	 *	<li>Erwartet: <br>	
	 *	 	10796;1959-06-30;Breannda;Billingsley;F;1990-11-08;Management.</li>
	 * </ul>
	 */
	@Test
	@Define("EmployeeSuccess")
	@AfterMethod("testKonstruktorOhneParameter")
	@TestDescription("Testen des Kontruktors(String[]) mit einem Datensatz.")
	public void testKonstruktorEinDatensatz() {
		testEmployee();
		trace.add("Konstruktoraufruf Management(String[]).");
		String[] array = new String[] {smallData.get(10796).getPresentation()};
		IManagement mgnt = new Management(array);

		evaluate(mgnt, true, sortData.with(10796));
	}	
	
	/**
	 * <ul>
	 * 	<li>Testfall: <br>
	 * 		Konstruktoraufruf Management(String[]) mit 10 Datensaetzen.<br> 
	 * 		Nach dem Aufruf des Konstruktors mussen sich alle 10 Datensaetze in der 
	 * 		Mitarbeiterverwaltung befinden.</li>
	 *	<li>Erwartet: <br>
	 *		data[0] bis data[9] in der Mitarbeiterverwaltung.</li>
	 * </ul>
	 */
	@Test
	@Define("EmployeeSuccess")
	@AfterMethod("testKonstruktorEinDatensatz")
	@TestDescription("Testen des Kontruktors(String[]) mit 10 Datensaetzen.")
	public void testKonstruktorZehnDatensaetze() {
		testEmployee();
		trace.add("Konstruktoraufruf Management(String[]).");
		IManagement mgnt = new Management(data); 
		
		evaluate(mgnt, true, sortData);
	}
	
	/**
	 * <ul>
	 * 	<li>Testfall: <br>
	 * 		Methode size() mit 10 Datensaetzen.<br> 
	 * 		Nach dem Aufruf des Konstruktors muessen sich alle 10 Datensaetze in der 
	 * 		Mitarbeiterverwaltung befinden.</li>
	 *	<li>Erwartet: <br>
	 *		size() = 10</li>
	 * </ul>
	 */
	@Test
	@Define("EmployeeSuccess")
	@AfterMethod("testKonstruktorZehnDatensaetze")
	@TestDescription("Testen size() mit 10 Datensaetzen.")
	public void testSize() {
		testEmployee();
		trace.add("Konstruktoraufruf Management(String[]).");
		IManagement mgnt = new Management(data); 
		trace.add("Aufruf der Methode size().");
		trace.addInfo(PassTrace.ifEquals("size() muss 10 liefern", 10, mgnt.size()));
		assertFalse("Methode size() liefert falsches Ergebnis.",
				trace.hasOccurrences());
	}
		
	/**
	 * <ul>
	 * 	<li>Testfall: <br>
	 * 		Standard-Konstruktoraufruf Management() und anschliessendes
	 * 		einfuegen von 10 Datensaetzen.<br> 
	 * 		Alle Datensaetze muessen in die Mitarbeiterverwaltung eingefuegt 
	 * 		werden koennen.</li>
	 *	<li>Erwartet: <br>
	 *		data[0] bis data[9] in der Mitarbeiterverwaltung.</li>
	 * </ul>
	 */
	@Test
	@Define("EmployeeSuccess")
	@AfterMethod("testSize")
	@TestDescription("Testen der insert(IEmployee)-Methode mit 10 Datensaetzen.")
	public void testInsert() {
		testEmployee();
		trace.add("Konstruktoraufruf Management().");
		IManagement mgnt = new Management(); 
		evaluate(mgnt, true, sortData.with());
		
		trace.add("Datensaetze in die Mitarbeiterverwaltung einfuegen.");
		for (ICollectorItem<Integer, IEmployee> item : smallData) {
			traceL1.add("Konstruktoraufruf Employee(\"%s\").", item);
			IEmployee emp = new Employee(item.toString());
			traceL1.addInfo("Aufruf von insert(<%d>).", item.getKey());
			mgnt.insert(emp);
		}
		trace.addDependentTrace(true);
		
		evaluate(mgnt, true, smallData);
	}
	
	/**
	 * <ul>
	 * 	<li>Testfall: <br>
	 * 		Standard-Konstruktoraufruf Management() und anschliessendes
	 * 		einfuegen von 10 Datensaetzen in umgekehrter Reihenfolge.<br> 
	 * 		Alle Datensaetze muessen in die Mitarbeiterverwaltung eingefuegt 
	 * 		werden koennen.</li>
	 *	<li>Erwartet: <br>
	 *		data[0] bis data[9] in der Mitarbeiterverwaltung.</li>
	 * </ul>
	 */
	@Test
	@Define("EmployeeSuccess")
	@AfterMethod("testInsert")
	@TestDescription("Testen der insert(IEmployee)-Methode mit 10 Datensaetzen.")
	public void testInsertReverse() {
		testEmployee();
		trace.add("Konstruktoraufruf Management().");
		IManagement mgnt = new Management(); 
		evaluate(mgnt, true, sortData.with());
		
		trace.add("Datensaetze in die Mitarbeiterverwaltung einfuegen.");
		ICollected<Integer, IEmployee> reverseData = smallData.sortByKey(reverseComp);
		for (ICollectorItem<Integer, IEmployee> item : reverseData) {
			traceL1.add("Konstruktoraufruf Member(\"%s\").", item);
			IEmployee emp = new Employee(item.toString());
			traceL1.addInfo("Aufruf von insert(<%d>).", item.getKey());
			mgnt.insert(emp);
		}
		trace.addDependentTrace(true);
		
		evaluate(mgnt, true, reverseData);
	}
	
	/**
	 * <ul>
	 * 	<li>Testfall: <br>
	 * 		Fortlaufendes Einfuegen von Mitarbeitern und suchen nach Abteilungen,
	 *      Schluesseln und Mitarbeiternamen.</li>
	 *	<li>Erwartet: <br>
	 *		Die Rueckgaben der Methoden entsprechen den erwarteten Werten.</li>
	 *	<li>Beispiel: <br> 
	 *		"10855;1957-08-07;Breannda;Billingsley;F;1991-08-05;Finance"</li>
	 * </ul>
	 */
	@Test
	@Define("EmployeeSuccess")
	@AfterMethod("testInsertReverse")
	@TestDescription("Einfuegen von Datensaetzen, suchen nach Abteilung, Schluesseln und Namen.")
	public void testInsertSearch() {
		testEmployee();		
		trace.add("Konstruktoraufruf Management().");
		IManagement mgnt = new Management(); 
		traceL1.enumeration(new Enumeration(0, Enumeration.letters));
		int idx = 0;
		for (ICollectorItem<Integer, IEmployee> item : smallData) {
			ICollected<Integer, IEmployee> section = smallData.section(0, ++idx);
			trace.add("Erstellen und einfuegen von Employee(\"%s\").", item.getPresentation());
			IEmployee emp = new Employee(item.getPresentation());
			mgnt.insert(emp);			
			//---------------------------------------------
			Department dep = Department.valueOf(item.getPart(6).toString().toUpperCase());		
			traceL1.add("Aufruf von member(%s).", dep.name());	
			evaluate(mgnt.members(dep), false, section.where((c, it) -> it.getPart(6).equals(dep.toString())));
			traceL1.separator();
			//---------------------------------------------
			int key = item.getKey();
			traceL1.separator();
			traceL1.add("Aufruf von search(%d).", key);
			traceL1.addInfo(PassTrace.ifEquals(evalMsg, item.getPresentation(), mgnt.search(key)));
			trace.addDependentTrace(true);
			//---------------------------------------------			
			String nachname = item.getPart(3).toString();
			String vorname  = item.getPart(2).toString();
			traceL1.separator();
			traceL1.add("Aufruf von search(\"%s, %s\")", nachname, vorname);
			evaluate(mgnt.search(nachname, vorname), false, 
					section.where((c, it) -> it.getPart(2).equals(vorname) && 
                    					     it.getPart(3).equals(nachname)));
			//---------------------------------------------
			trace.addDependentTrace(true);
		}		
		assertFalse(errMsg,	trace.hasOccurrences());
	}
	
	/**
	 * <ul>
	 * 	<li>Testfall: <br>
	 * 		Standard-Konstruktoraufruf Management() und anschliessendes
	 * 		einfuegen von 10 Datensaetzen in sortierter Reihenfoge nach aufsteigenden Ids.<br> 
	 * 		Alle Datensaetze muessen in die Mitarbeiterverwaltung eingefuegt 
	 * 		werden koennen.</li>
	 *	<li>Erwartet: <br>
	 *		data[0] bis data[9] in der Mitarbeiterverwaltung.
	 *  </li>
	 * </ul>
	 */
	@Test
	@Define("EmployeeSuccess")
	@AfterMethod("testInsertSearch")
	@TestDescription("Testen der insert(IEmployee)-Methode mit 10 Datensaetzen mit aufsteigenden Ids.")
	public void testInsertSorted() {
		testEmployee();
		trace.add("Konstruktoraufruf Management().");
		IManagement mgnt = new Management(); 
		evaluate(mgnt, true, sortData.with());
		
		trace.add("Datensaetze in die Mitarbeiterverwaltung einfuegen.");
		for (ICollectorItem<Integer, IEmployee> item : sortData) {
			traceL1.add("Konstruktoraufruf Employee(\"%s\").", item);
			IEmployee emp = new Employee(item.toString());
			traceL1.addInfo("Aufruf von insert(<%s>).", item.getKey());
			mgnt.insert(emp);
		}
		trace.addDependentTrace(true);
		evaluate(mgnt, true, sortData);
	}
	
	/**
	 * <ul>
	 * 	<li>Testfall: <br>
	 * 		Konstruktoraufruf Management(String[]) mit 10 Datensaetzen
	 * 		und anschliessendes Loeschen der Eintraegen aus der Liste.
	 *  </li>
	 *	<li>Erwartet: 
	 *		<ul>
	 *			<li>Die Methode delete(int) muss true liefern.</li>
	 *			<li>Die geloeschten Eintraege duerfen sicht nicht mehr 
	 *				in der Liste befinden.</li>
	 *		</ul> 
	 *	</li>
	 *  </li>
	 * </ul>
	 */
	@Test
	@Define("EmployeeSuccess")
	@AfterMethod("testInsertSorted")
	@TestDescription("Testen der delete(int)-Methode mit 10 Datensaetzen.")
	public void testDelete() {
		testEmployee();
		int[] keys = {10855, 10948, 10943, 10041, 10060, 10938, 10942, 10005, 10034, 10796};   
		trace.add("Konstruktoraufruf Management(String[]).");
		IManagement mgnt = new Management(data); 
		evaluate(mgnt, true, sortData);
		showKeyOrder(sortData);
				
		ICollected<Integer, IEmployee> localData = sortData.withOut();
		String msg = "Loeschen des Datensatzes mit dem Schluessel %d.";
		for (int key : keys) {
			trace.add("Aufruf von delete(%d).", key);			
			boolean got = mgnt.delete(key);
			trace.addInfo(PassTrace.ifTrue(msg, got, key));
			localData = localData.withOut(key);
			evaluate(mgnt, true, localData);
			trace.separator();
		}		
		assertFalse("Methode delete(int) fehlerhaft implementiert.",
				trace.hasOccurrences());
	}
	
	/**
	 * <ul>
	 * 	<li>Testfall: <br>
	 * 		Konstruktoraufruf Management(String[]) mit 3 Datensaetzen
	 * 		und anschliessendes Loeschen von Eintraegen.
	 *  </li>
	 *	<li>Erwartet: 
	 *		<ul>
	 *			<li>Die Methode delete(int) muss bei vorhandenen Eintraegen 
	 *				true liefern.</li>
	 *			<li>Die Methode delete(int) muss bei nicht mehr vorhandenen 
	 *				Eintraegen false liefern.</li>
	 *			<li>Der geloeschte Eintraege duerfen sicht nicht mehr in der 
	 *				Liste befinden.</li>
	 *		</ul> 
	 *	</li>
	 *  </li>
	 * </ul>
	 */
	@Test
	@Define("EmployeeSuccess")
	@AfterMethod("testDelete")
	@TestDescription("Testen der delete(int)-Methode mit 3 Datensaetzen.")
	public void testDeleteZweimal() {
		testEmployee();
		ICollected<Integer, IEmployee> section = smallData.section(0, 3);
		ICollected<Integer, IEmployee> localData = section.sort();
		trace.add("Konstruktoraufruf Management(String[]).");
		IManagement mgnt = new Management(section.presentationOrderAsArray());
		evaluate(mgnt, true, localData);
		showKeyOrder(localData);
				
		String msg = "Loeschen des %2$s mit dem Schluessel %1$d.";
		for (ICollectorItem<Integer, IEmployee> item : section) {
			int key = item.getKey();
			trace.add("Aufruf von delete(%d).", key);
			
			boolean got1 = mgnt.delete(key);
			traceL1.add(PassTrace.ifTrue(msg, got1, key, "Datensatzes"));
			boolean got2 = mgnt.delete(key);
			traceL1.add(PassTrace.ifFalse(msg, got2, key, "bereits geloeschten Datensatzes"));
			
			trace.addDependentTrace(true);
			localData = localData.withOut(key);
			evaluate(mgnt, true, localData);
			trace.separator();
		}
		assertFalse("Methode delete(int) fehlerhaft implementiert.",
				trace.hasOccurrences());
	}
	
	/**
	 * <ul>
	 * 	<li>Testfall: <br>
	 * 		Konstruktoraufruf Management(String[]) mit 10 Datensaetzen
	 * 		und anschliessendes Suchen nach den Schuesselwerten in der 
	 *		Mitarbeiterverwaltung.
	 *  </li>
	 *	<li>Erwartet: 
	 *		<ul>
	 *			<li>Alle Schuesselwerte muessen in der Mitarbeiterverwaltung
	 *				gefunden werden.</li>
	 *			<li>Die Methode search(int) muss den zugehoerigen 
	 *				Datensatz zurueckliefern.</li>
	 *		</ul> 
	 *	</li>
	 * </ul>
	 */
	@Test
	@Define("EmployeeSuccess")
	@AfterMethod("testDeleteZweimal")
	@TestDescription("Testen der search(int)-Methode mit 10 Datensaetzen.")
	public void testSearchSchluessel() {
		testEmployee();
		trace.add("Konstruktoraufruf Management(String[]).");
		IManagement mgnt = new Management(data); 
		evaluate(mgnt, true, sortData);
		showKeyOrder(sortData);
		
		trace.add("Suchen nach Schluesseln in der Mitarbeiterverwaltung.");
		String msg = "Erhaltener Datensatz bei der Suche nach %d.";
		for (ICollectorItem<Integer, IEmployee> entry : smallData) {
			traceL1.add("Aufruf von search(%d).", entry.getKey());
			String exp    = entry.getPresentation();
			IEmployee got = mgnt.search(entry.getKey());
			traceL1.addInfo(PassTrace.ifEquals(msg, exp, got, entry.getKey()));
		}
		assertFalse("Methode search(int) liefert falschen Datensatz.",
				trace.addDependentTrace(true).hasOccurrences());
	}
	
	/**
	 * <ul>
	 * 	<li>Testfall: <br>
	 * 		Konstruktoraufruf Management(String[]) mit 10 Datensaetzen
	 * 		und anschliessendes Suchen nach Schuesselwerten, die nicht in der
	 * 		Mitarbeiterverwaltung existieren.</li>
	 *  <li>Erwartet: 
	 *		<ul>
	 *			<li>Kein Schuesselwert darf in der Mitarbeiterverwaltung
	 *				gefunden werden.</li>
	 *			<li>Methode search(int) muss null liefern.</li>
	 *		</ul> 
	 *	</li>
	 * </ul>
	 */
	@Test
	@Define("EmployeeSuccess")
	@AfterMethod("testSearchSchluessel")
	@TestDescription("Methode search(int) muss bei nicht vorhandenen Schluesseln null liefern.")
	public void testSearchUnbekannterSchluessel() {
		testEmployee();
		trace.add("Konstruktoraufruf Management(String[]).");
		IManagement mgnt = new Management(data); 
		evaluate(mgnt, true, sortData);
		showKeyOrder(sortData);
		
		trace.add("Suchen nach nicht vorhandenen Schluesseln in der Mitarbeiterverwaltung.");
		String msg = "Erhaltener Datensatz bei der Suche nach Schluessel %d.";
		int ofs = 10000;
		for (ICollectorItem<Integer, IEmployee> entry : smallData) {
			int key = entry.getKey() + ofs;
			traceL1.add("Aufruf von search(%d)", key);			
			IEmployee got = mgnt.search(key);
			traceL1.addInfo(PassTrace.ifNull(msg, got, key));
		}
		assertFalse("Methode search(int) liefert falschen Datensatz.",
				trace.addDependentTrace(true).hasOccurrences());
	}

	/**
	 * <ul>
	 * 	<li>Testfall: <br> 
	 * 		Konstruktoraufruf Management(String[]) mit 10 Datensaetzen
	 * 		und anschliessendes Suchen nach dem Nach- und Vornamen in der 
	 *		Mitarbeiterverwaltung.</li>
	 * <li>Erwartet: 
	 *		<ul>
	 *			<li>Alle Namen muessen in der Mitarbeiterverwaltung
	 *				gefunden.</li>
	 *			<li>Die Methode search(String, String) muss die zugehoerigen
	 *				Datensaetze zurueckliefern.</li>
	 *		</ul> 
	 *	</li>
	 * </ul>
	 */
	@Test
	@Define("EmployeeSuccess")
	@AfterMethod("testSearchUnbekannterSchluessel")
	@TestDescription("Testen der search(String, String)-Methode mit 10 Datensaetzen.")
	public void testSearchName() {
		testEmployee();
		trace.add("Konstruktoraufruf Management(String[]).");
		IManagement mgnt = new Management(data); 
		evaluate(mgnt, true, sortData);
				
		for (ICollectorItem<Integer, IEmployee> entry : smallData.withOut(10855, 10796)) {
			String nachname = entry.getPart(3).toString();
			String vorname  = entry.getPart(2).toString();
			trace.add("Suchen nach %s %s in der Mitarbeiterverwaltung.", nachname, vorname);
			traceL1.add(Evaluate.callMethod("",  "search", nachname, vorname));
			IEmployee[] got = mgnt.search(nachname, vorname);			
			ICollected<Integer, IEmployee> exp = smallData.where((c, item) -> 
				nachname.equals(item.getPart(3).toString()) && 
				vorname.equals (item.getPart(2).toString()));
			evaluate(got, false, exp);
		}
		assertFalse("Methode search(String, String) liefert falschen Datensatz.",
				trace.addDependentTrace(true).hasOccurrences());
	}
		
	/**
	 * <ul>
	 * 	<li>Testfall: <br>
	 * 		Konstruktoraufruf Management(String[]) mit 10 Datensaetzen
	 * 		und anschliessendes Suchen nach unbekannten Nachnamen in der 
	 *		Mitarbeiterverwaltung.</li>	 
	 *	<li>Erwartet: 
	 *		<ul>
	 *			<li>Kein Namen darf in der Mitarbeiterverwaltung gefunden werden.</li>
	 *			<li>Methode search(String, String) muss ein leeres Array liefern.</li>
	 *		</ul> 
	 *	</li>
	 * </ul>
	 */
	@Test
	@Define("EmployeeSuccess")
	@AfterMethod("testSearchName")
	@TestDescription("Methode search(String, String) muss bei unbekannten Nachnamen ein leeres Array liefern.")
	public void testSearchUnbekannterNachname() {
		testEmployee();
		trace.add("Konstruktoraufruf Management(String[]).");
		IManagement mgnt = new Management(data); 
		evaluate(mgnt, true, sortData);
		
		for (ICollectorItem<Integer, IEmployee> entry : smallData) {
			String unknown = entry.getPart(3) + "XX";
			trace.add("Aufruf von search(\"%s\", \"%s\")", unknown, entry.getPart(2));			
			IEmployee[] got = mgnt.search(unknown, entry.getPart(2).toString());			
			evaluate(got, false, sortData.with());
		}		
		assertFalse("Methode search(String, String) liefert falschen Datensatz.",
				trace.addDependentTrace(true).hasOccurrences());
	}
	
	/**
	 * <ul>
	 * 	<li>Testfall: <br>
	 * 		Konstruktoraufruf Management(String[]) mit 10 Datensaetzen
	 * 		und anschliessendes Suchen nach unbekannten Vornamen in der 
	 *		Mitarbeiterverwaltung.</li>	 
	 *	<li>Erwartet: 
	 *		<ul>
	 *			<li>Kein Namen darf in der Mitarbeiterverwaltung gefunden werden.</li>
	 *			<li>Methode search(String, String) muss ein leeres Array liefern.</li>
	 *		</ul> 
	 *	</li>
	 * </ul>
	 */
	@Test
	@Define("EmployeeSuccess")
	@AfterMethod("testSearchUnbekannterNachname")
	@TestDescription("Methode search(String, String) muss bei unbekannten Vornamen ein leeres Array liefern.")
	public void testSearchUnbekannterVorname() {
		testEmployee();
		trace.add("Konstruktoraufruf Management(String[]).");
		IManagement mgnt = new Management(data); 
		evaluate(mgnt, true, sortData);
		
		for (ICollectorItem<Integer, IEmployee> entry : smallData) {
			String unknown = entry.getPart(2) + "XX";
			trace.add("Aufruf von search(\"%s, %s\")",entry.getPart(3), unknown);			
			IEmployee[] got = mgnt.search(entry.getPart(3).toString(), unknown);			
			evaluate(got, false, sortData.with());
		}		
		assertFalse("Methode search(String, String) liefert falschen Datensatz.",
				trace.addDependentTrace(true).hasOccurrences());
	}
	
	/**
	 * <ul>
	 * 	<li>Testfall: <br>
	 * 		Konstruktoraufruf Management(String[]) mit 10 Datensaetzen.
	 * 		Loeschen von Eintraegen aus der Liste und anschliessendes suchen
	 * 		nach benachbarten Schluesseln.
	 *  </li>
	 *	<li>Erwartet: 
	 *		<ul>
	 *			<li>Die Methode delete(int) muss true liefern.</li>
	 *			<li>Die geloeschten Eintraege duerfen sicht nicht mehr 
	 *				in der Liste befinden.</li>
	 *			<li>Alle weiteren Eintraege muessen weiterhin in der Liste
	 *				vorhanden sein.</li>
	 *		</ul> 
	 *	</li>
	 *  </li>
	 * </ul>
	 */
	@Test
	@Define("EmployeeSuccess")
	@AfterMethod("testSearchUnbekannterVorname")
	@TestDescription("Testen der delete(int) und search(int)-Methode mit 10 Datensaetzen.")
	public void testDeleteAndSearch() {
		testEmployee();
		String msgDel  = "Loeschen des Datensatzes mit dem Schluessel %d.";
		String msgSrch = "Suchen nach dem Datensatz mit dem Schluessel %d.";
		int[] delKeys  = {10005, 10855, 10948};   
		int[] srchKeys = {10034, 10938, 10943};   
		trace.add("Konstruktoraufruf Management(String[]).");
		IManagement mgnt = new Management(data); 
		evaluate(mgnt, true, sortData);
		showKeyOrder(sortData);
				
		ICollected<Integer, IEmployee> localData = sortData.withOut();
		for (int i = 0; i < delKeys.length; i++) {			
			trace.add("Aufruf von delete(%d).", delKeys[i]);			
			boolean del = mgnt.delete(delKeys[i]);
			trace.addInfo(PassTrace.ifTrue(msgDel, del, delKeys[i]));
			
			trace.add("Aufruf von search(%d).", srchKeys[i]);			
			IEmployee empl = mgnt.search(srchKeys[i]);
			trace.addInfo(PassTrace.ifEquals(msgSrch, smallData.get(srchKeys[i]), empl, srchKeys[i]));
			
			localData = localData.withOut(delKeys[i]);
			evaluate(mgnt, true, localData);
			trace.separator();
		}			
		assertFalse("Methode delete(int)/search(int) fehlerhaft implementiert.",
				trace.hasOccurrences());
	}

	/**
	 * <ul>
	 * 	<li>Testfall: <br>
	 * 		Konstruktoraufruf Management(String[]) mit 10 Datensaetzen
	 * 		und anschliessendes ermitteln der Anzahl der Eintraege fuer die 
	 * 		Departments Development, Sales, Manpower und Research in der 
	 * 		Mitarbeiterverwaltung.</li>
	 *	<li>Erwartet: <br> 
	 *		Die Methode size(Department) muss folgende Werte liefern:
	 *		<ul>
	 *			<li>Development: 3</li>
	 *			<li>Sales: 2</li>
	 *			<li>Manpower: 1</li>
	 *			<li>Research: 0</li>
	 *		</ul> 
	 *	</li>
	 * </ul>
	 */
	@Test
	@Define("EmployeeSuccess")
	@AfterMethod("testDeleteAndSearch")
	@TestDescription("Testen der size(Department)-Methode mit 10 Datensaetzen.")
	public void testSizeDepartment() {
		testEmployee();
		trace.add("Konstruktoraufruf Management(String[]).");
		IManagement mgnt = new Management(data); 
		evaluate(mgnt, true, sortData);
		
		String msg = "Erhaltene Anzahl der Mitarbeiter fuer das Department %s.";
		
		Department d = Department.DEVELOPMENT;
		trace.add("Aufruf von size(%s).", d);
		trace.addInfo(PassTrace.ifEquals(msg, 3, mgnt.size(d), d));
		
		d = Department.SALES;
		trace.add("Aufruf von size(%s).", d);
		trace.addInfo(PassTrace.ifEquals(msg, 2, mgnt.size(d), d));
		
		d = Department.MANPOWER;
		trace.add("Aufruf von size(%s).", d);
		trace.addInfo(PassTrace.ifEquals(msg, 1, mgnt.size(d), d));
		
		d = Department.RESEARCH;
		trace.add("Aufruf von size(%s).", d);
		trace.addInfo(PassTrace.ifEquals(msg, 0, mgnt.size(d), d));
		
		assertFalse("Methode size(Department) liefert falsches Ergebnis.",
				trace.hasOccurrences());
	}
	
	/**
	 * <ul>
	 * 	<li>Testfall: Konstruktoraufruf Management(String[]) mit 10 Datensaetzen
	 * 		und anschliessendes ermitteln der Eintraege fuer die 
	 * 		Departments Development, Sales, Human Resources und Research in der 
	 * 		Mitarbeiterverwaltung.</li>
	 *	<li>Erwartet: Die Methode members(Department) muss folgende Werte liefern:
	 *		<ul>
	 *			<li>Development: [10948, 10943, 10948]</li>
	 *			<li>Sales: [10034, 10041]</li>
	 *			<li>Manpower: [10005]</li>
	 *			<li>Research: []</li>
	 *		</ul> 
	 *	</li>
	 * </ul>
	 */
	@Test
	@Define("EmployeeSuccess")
	@AfterMethod("testSizeDepartment")
	@TestDescription("Testen der members(Department)-Methode mit 10 Datensaetzen.")
	public void testMemberDepartment() {
		testEmployee();
		trace.add("Konstruktoraufruf Management(String[]).");
		IManagement mgnt = new Management(data); 
		evaluate(mgnt, true, sortData);
		
		Department d = Department.DEVELOPMENT;
		trace.add("Aufruf von member(%s).", d);
		evaluate(mgnt.members(d), false, sortData.with(10942, 10943, 10948));
		
		d = Department.SALES;
		trace.add("Aufruf von member(%s).", d);
		evaluate(mgnt.members(d), false, sortData.with(10041, 10034));
		
		d = Department.MANPOWER;
		trace.add("Aufruf von member(%s).", d);
		evaluate(mgnt.members(d), false, sortData.with(10005));
		
		d = Department.RESEARCH;
		trace.add("Aufruf von member(%s).", d);
		evaluate(mgnt.members(d), false, sortData.with());
		
		assertFalse("Methode member(Department) liefert falsches Ergebnis.",
				trace.hasOccurrences());
	}
	
	/**
	 * 
	 * <ul>
	 * 	<li>Testfall: <br>
	 * 		Konstruktoraufruf Management(String[]) mit 10 Datensaetzen.
	 * 		Suchen nach Namen,  Department und Schluessel in der Mitarbeiterverwaltung.
	 * </li>
	 *	<li>Erwartet: <br>
	 *		Es muessen folgende Eintraege liefert werden:
	 *		<ul>
	 *			<li>search("Billingsley", "Breannda"): {10060}</li>
	 *			<li>members(DEVELOPMENT): {10942, 10943, 10948}</li>
	 *			<li>search(10943): "Berna, Skafidas"</li>
	 *		</ul> 
	 *	</li>
	 * </ul>
	 */
	@Test
	@Define("EmployeeSuccess")
	@AfterMethod("testMemberDepartment")
	@TestDescription("Aufeinander folgendes Suchen nach Namen, Abteilung und Schluessel.")
	public void testSearchMulti() {
		testEmployee();
		trace.add("Konstruktoraufruf Management(String[]).");
		IManagement mgnt = new Management(data); 
		evaluate(mgnt, true, sortData);
		showKeyOrder(sortData);

		trace.separator();
		trace.add("Suche nach Vor- und Nachnamen.");
		String vormane = "Breannda";
		String nachmane = "Billingsley";
		traceL1.add("Aufruf von search(\"%s\", \"%s\").", nachmane, vormane);
		IEmployee[] emps = mgnt.search(nachmane, vormane);
		evaluate(emps, false, sortData.with(10855, 10796, 10060));
		
		trace.add("Suche nach Mitarbeitern einer Abteilung.");
		Department d = Department.DEVELOPMENT;
		traceL1.add("Aufruf von member(%s).", d);
		emps = mgnt.members(d);
		evaluate(emps, false, sortData.with(10942, 10943, 10948));
		
		trace.add("Suche nach einem Schluessel.");
		int key = 10943;
		traceL1.add("Aufruf von search(%d).", key);
		IEmployee emp = mgnt.search(key);
		evaluate(new IEmployee[]{emp}, false, sortData.with(key));
	}
	
	/**
	 * <ul>
	 * 	<li>Testfall: <br>
	 * 		Konstruktoraufruf Management(String[]) mit 6 Datensaetzen 
	 * 		fuer die erste Instanz und Konstruktoraufruf Management(String[]) 
	 * 		mit 4 Datensaetzen fuer die zweite Instanz.
	 * </li>
	 *	<li>Erwartet: <br>
	 *		Die Instanzen von Management muessen unabhaengig voneinander 
	 *		arbeiten. 
	 *	</li>
	 * </ul>
	 */
	@Test
	@Define("EmployeeSuccess")
	@AfterMethod("testSearchMulti")
	@TestDescription("Instanzen von Management muessen unabhaengig voneinander arbeiten.")
	public void testZweiInstanzen() {

		ICollected<Integer, IEmployee> data1 = smallData.section(0, 6);
		trace.add(Evaluate.callConstruktor("mgnt1", Management.class, data1.getKeys()));
		IManagement mgnt1 = new Management(data1.presentationOrderAsArray());
		trace.addDependentTrace(true);
		
		ICollected<Integer, IEmployee> data2 = smallData.section(6, data.length);
		trace.add(Evaluate.callConstruktor("mgnt2", Management.class, data2.getKeys()));
		IManagement mgnt2 = new Management(data2.presentationOrderAsArray());
		trace.addDependentTrace(true);
		
		trace.separator();
		trace.add("Instanz mgnt1 muss 6 Datensaetze enthalten.");
		trace.separator();
		evaluate(mgnt1, true, data1.sort());
		
		trace.separator();
		trace.add("Instanz mgnt2 muss 4 Datensaetze enthalten.");
		trace.separator();
		evaluate(mgnt2, true, data2.sort());
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Ueberpruefung der durch die Methode toArray() der Mitarbeiterverwaltung
	 * zurueckgelieferten Datensaetze.
	 * @param mgnt - Mitarbeiterverwaltung: IManagement.
	 * @param byOder - Ueberuefung nach Reihenfolge oder Schluessel: boolean.
	 * @param exp - erwarteten Testdatensaetze: ICollected<>.
	 */
	private void evaluate(IManagement mgnt, boolean byOder, ICollected<Integer, IEmployee> exp) {
		trace.add("Aufruf der toArray()-Methode.");
		evaluate(mgnt.toArray(), byOder, exp);
	}
	
	/**
	 * Ueberpruefung der uebergebenen Datensaetze anhand der Schluessel der 
	 * erwarteten Testdatensaetze.
	 * @param emps - Array mit Datensaetze: IEmployee[].
	 * @param byOder - Ueberuefung nach Reihenfolge oder Schluessel: boolean.
	 * @param exp - erwarteten Testdatensaetze: ICollected<>.
	 */
	private void evaluate(IEmployee[] emps, boolean byOder, ICollected<Integer, IEmployee> exp) {	
		traceL1.add(callMsg);
		funcCollector.clear().useIdentifier("employee", "{", "}");
		ICollected<Integer, IEmployee> got = funcCollector.collectAll(emps);	
		assertFalse(errMsg,	funcCollector.addToParentTrace(true).hasOccurrences());			
		
		traceL1.add(evalMsg);
		if (byOder) {
			eval.equalsByOrder(exp, got);			
		} else {
			eval.equalsByKeys(exp, got);
		}
		eval.addToParentTrace(true);
		traceL1.add(PassTrace.ifEquals(duplMsg,	"[]", got.multipleKeys()));
		trace.addDependentTrace(true);

		assertFalse(errMsg,	trace.hasOccurrences());
	}
	
	private void showKeyOrder(ICollected<Integer, IEmployee> data) {
		trace.separator();
		trace.add("Reihenfolge der Schluessel in der Mitarbeiterverwaltung.");
		trace.addInfo(data.keyOrder());
		trace.separator();
	}
	
}
