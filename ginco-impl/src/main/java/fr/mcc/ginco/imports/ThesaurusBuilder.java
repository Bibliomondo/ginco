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
package fr.mcc.ginco.imports;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;

import fr.mcc.ginco.beans.Language;
import fr.mcc.ginco.beans.Thesaurus;
import fr.mcc.ginco.beans.ThesaurusFormat;
import fr.mcc.ginco.beans.ThesaurusOrganization;
import fr.mcc.ginco.beans.ThesaurusType;
import fr.mcc.ginco.dao.IGenericDAO;
import fr.mcc.ginco.dao.ILanguageDAO;
import fr.mcc.ginco.dao.IThesaurusTypeDAO;
import fr.mcc.ginco.exceptions.BusinessException;
import fr.mcc.ginco.log.Log;

@Service("skosThesaurusBuilder")
public class ThesaurusBuilder extends AbstractBuilder{

	@Log
	private Logger logger;

	@Inject
	@Named("thesaurusFormatDAO")
	private IGenericDAO<ThesaurusFormat, Integer> thesaurusFormatDAO;

	@Inject
	@Named("thesaurusTypeDAO")
	private IThesaurusTypeDAO thesaurusTypeDAO;

	@Inject
	@Named("languagesDAO")
	private ILanguageDAO languagesDAO;


	@Value("${import.default.top.concept}")
	private boolean defaultTopConcept;

	@Value("#{'${import.skos.date.formats}'.split(',')}")
	private List<String> skosDefaultDateFormats;

	@Value("${import.skos.default.format}")
	private Integer defaultThesaurusFormat;

	public ThesaurusBuilder() {
		super();
	}

	public Thesaurus buildThesaurus(Resource skosThesaurus, Model model)
			throws BusinessException {
		Thesaurus thesaurus = new Thesaurus();
		thesaurus.setIdentifier(skosThesaurus.getURI());
		thesaurus.setTitle(getSimpleStringInfo(skosThesaurus, DC.title));
		thesaurus.setSubject(getMultipleLineStringInfo(skosThesaurus,
				DC.subject));
		thesaurus.setContributor(getMultipleLineStringInfo(skosThesaurus,
				DC.contributor));
		thesaurus.setCoverage(getMultipleLineStringInfo(skosThesaurus,
				DC.coverage));
		thesaurus.setDescription(getMultipleLineStringInfo(skosThesaurus,
				DC.description));
		thesaurus
				.setPublisher(getSimpleStringInfo(skosThesaurus, DC.publisher));
		thesaurus
				.setRights(getMultipleLineStringInfo(skosThesaurus, DC.rights));
		ThesaurusType thesaurusType = thesaurusTypeDAO.getByLabel(getSimpleStringInfo(
				skosThesaurus, DC.type));
		if (thesaurusType == null) {
			throw new BusinessException("Unknown thesaurus type " + getSimpleStringInfo(
					skosThesaurus, DC.type) ,"import-unknown-thesaurus-type");

		}
		thesaurus.setType(thesaurusTypeDAO.getByLabel(getSimpleStringInfo(
				skosThesaurus, DC.type)));
		thesaurus.setRelation(getMultipleLineStringInfo(skosThesaurus,
				DC.relation));
		thesaurus.setSource(getSimpleStringInfo(skosThesaurus, DC.source));
		thesaurus.setCreated(getSkosDate(getSimpleStringInfo(skosThesaurus,
				DCTerms.created)));
		thesaurus.setDate(getSkosDate(getSimpleStringInfo(skosThesaurus,
				DCTerms.modified)));

		thesaurus.setLang(getLanguages(skosThesaurus));

		ThesaurusFormat format = thesaurusFormatDAO
				.getById(defaultThesaurusFormat);
		if (format == null) {
			throw new BusinessException("Configuration problem : the default import format " + defaultThesaurusFormat + "is unknown","import-unknown-default-format");
		}
		thesaurus.setFormat(format);

		thesaurus.setDefaultTopConcept(defaultTopConcept);

		thesaurus.setCreator(getCreator(skosThesaurus, model));

		return thesaurus;
	}

	private ThesaurusOrganization getCreator(Resource skosThesaurus, Model model) {
		Statement stmt = skosThesaurus.getProperty(DC.creator);
		RDFNode node = stmt.getObject();
		Resource cretaorResource = node.asResource();
		SimpleSelector organizationSelector = new SimpleSelector(
				cretaorResource, null, (RDFNode) null) {
			public boolean selects(Statement s) {
				if (s.getObject().isResource()) {
					Resource res = s.getObject().asResource();
					return res.equals(FOAF.Organization);
				} else {
					return false;
				}
			}
		};

		StmtIterator iter = model.listStatements(organizationSelector);
		while (iter.hasNext()) {
			Statement orgStms = iter.next();
			Resource organizationRes = orgStms.getSubject().asResource();
			Statement foafName = organizationRes.getProperty(FOAF.name);
			Statement foafHomepage = organizationRes.getProperty(FOAF.homepage);
			ThesaurusOrganization org = new ThesaurusOrganization();
			org.setHomepage(foafHomepage.getString());
			org.setName(foafName.getString());
			return org;
		}

		return null;
	}

	private Set<Language> getLanguages(Resource skosThesaurus) {
		Set<Language> langs = new HashSet<Language>();
		StmtIterator stmtIterator = skosThesaurus.listProperties(DC.language);
		while (stmtIterator.hasNext()) {
			Statement stmt = stmtIterator.next();
			Language lang = languagesDAO.getById(stmt.getString());
			if (lang==null) {
				throw new BusinessException("Specified thesaurus lanaguge is unknown :  " + stmt.getString(),"import-unknown-thesaurus-lang");
	}
			langs.add(lang);
		}

		return langs;
	}

	private Date getSkosDate(String skosDate) throws BusinessException {
		for (String skosDefaultDateFormat : skosDefaultDateFormats) {
			SimpleDateFormat sdf = new SimpleDateFormat(skosDefaultDateFormat);
			try {
				return sdf.parse(skosDate);
			} catch (ParseException e) {
				logger.warn("Invalid date format for skosDate : " + skosDate);
			}
		}
		throw new BusinessException("InvalidDateFormat for input string "
				+ skosDate, "import-invalid-date-format");
	}	
}