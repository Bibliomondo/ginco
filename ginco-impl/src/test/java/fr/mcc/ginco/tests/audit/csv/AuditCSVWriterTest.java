/**
 * Copyright or © or Copr. Ministère Français chargé de la Culture
 * et de la Communication (2013)
 * <p/>
 * contact.gincoculture_at_gouv.fr
 * <p/>
 * This software is a computer program whose purpose is to provide a thesaurus
 * management solution.
 * <p/>
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software. You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * <p/>
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited liability.
 * <p/>
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systemsand/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 * <p/>
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */
package fr.mcc.ginco.tests.audit.csv;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import fr.mcc.ginco.audit.csv.AuditCSVWriter;
import fr.mcc.ginco.audit.csv.JournalEventsEnum;
import fr.mcc.ginco.audit.csv.JournalLine;
import fr.mcc.ginco.utils.DateUtil;

public class AuditCSVWriterTest {

	private AuditCSVWriter auditCSVWriter = new AuditCSVWriter();
	
	@Test
	public void testWriteHeader() throws IOException {
		StringWriter sw = new StringWriter();
		BufferedWriter out = new BufferedWriter(sw);
		
		auditCSVWriter.writeHeader(out);
		out.flush();
		out.close();
		
		Assert.assertEquals(true, sw.toString().startsWith("log-journal.headers.event-type,log-journal.headers.date,log-journal.headers.author,log-journal.headers.conceptId,log-journal.headers.termId,log-journal.headers.role,log-journal.headers.status,log-journal.headers.oldlexicalvalue,log-journal.headers.newlexicalvalue,log-journal.headers.oldParent,log-journal.headers.newparent"));
		
	}	
	
	
	@Test
	public void testWriteJournalLine() throws IOException {
		StringWriter sw = new StringWriter();
		BufferedWriter out = new BufferedWriter(sw);
		
		JournalLine line = new JournalLine();
		line.setAuthorId("unknown.author");
		line.setConceptId("http://fakeconcept");
		line.setEventDate(DateUtil.dateFromString("2013-01-20 20:30:00"));
		line.setEventType(JournalEventsEnum.THESAURUSCONCEPT_CREATED);
		Set<String> newGenericTerms = new HashSet<String>();
		newGenericTerms.add("http://fakeconcept1");
		newGenericTerms.add("http://fakeconcept2");
		line.setNewGenericTerm(newGenericTerms);
		line.setNewLexicalValue("New term lexical Value");
		Set<String> oldGenericTerms = new HashSet<String>();
		oldGenericTerms.add("http://fakeconcept1");
		line.setOldGenericTerm(oldGenericTerms);
		line.setOldLexicalValue("Old term lexical value");
		line.setStatus(0);
		line.setTermId("http://faketerm");
		line.setTermRole("TP");
		
		auditCSVWriter.writeJournalLine(line, out);
		out.flush();
		out.close();

		Assert.assertEquals(true, sw.toString().startsWith("log-journal.thesaurus-concept-created-event,2013-01-20 20:30:00,unknown.author,http://fakeconcept,http://faketerm,TP,concept-status[0],Old term lexical value,New term lexical Value,http://fakeconcept1,http://fakeconcept1|http://fakeconcept2"));
		
	}
}
