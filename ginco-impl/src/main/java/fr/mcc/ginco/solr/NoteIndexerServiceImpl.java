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
package fr.mcc.ginco.solr;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import fr.mcc.ginco.beans.Note;
import fr.mcc.ginco.exceptions.TechnicalException;

@Service("noteIndexerService")
public class NoteIndexerServiceImpl implements INoteIndexerService {

	private static Logger logger = LoggerFactory.getLogger(NoteIndexerServiceImpl.class);

	@Inject
	private NoteSolrConverter noteSolrConverter;

	@Inject
	@Named("solrServer")
	private SolrServer solrServer;

	@Override
	@Async
	public void addNote(Note note) throws TechnicalException {
		try {
			addNotes(Arrays.asList(note));
		} catch (TechnicalException ex) {
			logger.error(ex.getMessage(), ex.getCause().getMessage());
			throw ex;
		}
	}

	@Override
	public void addNotes(List<Note> notes) {
		try {
			for (Note note : notes) {

				logger.info("Indexing note :" + note.getIdentifier());
				if (note.getTerm() != null || note.getConcept() != null) {
					SolrInputDocument doc = noteSolrConverter.convertSolrNote(note);
					solrServer.add(doc);
				}
			}
			solrServer.commit();
		} catch (SolrServerException e) {
			throw new TechnicalException("Error during adding to SOLR!", e);
		} catch (IOException e) {
			throw new TechnicalException("IO error!", e);
		}
	}

	@Override
	@Async
	public void removeNote(Note note)
			throws TechnicalException {
		try {
			solrServer.deleteById(note.getIdentifier());
			solrServer.commit();
		} catch (SolrServerException e) {
			throw new TechnicalException(
					"Error executing query for removing Concept from index!", e);
		} catch (IOException e) {
			throw new TechnicalException(
					"IO error during executing query for removing Concept from index!",
					e);
		}
	}
}
