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
package fr.mcc.ginco.extjs.view.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fr.mcc.ginco.beans.ThesaurusConceptGroup;
import fr.mcc.ginco.beans.ThesaurusConceptGroupLabel;
import fr.mcc.ginco.extjs.view.enums.ThesaurusListNodeType;
import fr.mcc.ginco.extjs.view.node.IThesaurusListNode;
import fr.mcc.ginco.extjs.view.node.ThesaurusListBasicNode;
import fr.mcc.ginco.extjs.view.node.ThesaurusListNodeFactory;
import fr.mcc.ginco.services.IThesaurusConceptGroupLabelService;
import fr.mcc.ginco.services.IThesaurusConceptGroupService;
import fr.mcc.ginco.utils.LabelUtil;

/**
 * Generator in charge of building thesaurus groups list
 */
@Component(value = "groupsGenerator")
public class GroupsGenerator {

	 public static final String ID_PREFIX = ThesaurusListNodeType.GROUPS
	            .toString() + "_";
  	@Inject
	@Named("thesaurusConceptGroupService")
	private IThesaurusConceptGroupService thesaurusConceptGroupService;
  	
	@Inject
	@Named("thesaurusConceptGroupLabelService")
	private IThesaurusConceptGroupLabelService thesaurusConceptGroupLabelService;
	
	@Inject
	@Named("thesaurusListNodeFactory")
	private ThesaurusListNodeFactory thesaurusListNodeFactory;

	@Value("${ginco.default.language}") private String defaultLanguage;

	private Logger logger  = LoggerFactory.getLogger(GroupsGenerator.class);

	/**
	 * Creates the list of groups for a given thesaurus
	 * 
	 * @param parentId
	 *            id of the thesaurus.
	 * @return created list of leafs.
	 */
	public List<IThesaurusListNode> generateGroups(String thesaurusId) {
		logger.debug("Generating thesaurus groups list for vocabularyId : " + thesaurusId);
		List<ThesaurusConceptGroup> groups = thesaurusConceptGroupService.getAllThesaurusConceptGroupsByThesaurusId(null, thesaurusId);
		logger.debug(groups.size() + " groups found");

		List<IThesaurusListNode> newGroups = new ArrayList<IThesaurusListNode>();
		for (ThesaurusConceptGroup group : groups) {
			ThesaurusListBasicNode groupNode = thesaurusListNodeFactory.getListBasicNode();
			ThesaurusConceptGroupLabel label = thesaurusConceptGroupLabelService.getByThesaurusConceptGroup(group.getIdentifier());
			groupNode.setTitle(LabelUtil.getLocalizedLabel(label.getLexicalValue(), label.getLanguage(), defaultLanguage));			
			groupNode.setId(group.getIdentifier());
			groupNode.setType(ThesaurusListNodeType.GROUPS);
			groupNode.setExpanded(false);
			groupNode.setThesaurusId(group.getThesaurus().getIdentifier());

			groupNode.setChildren(new ArrayList<IThesaurusListNode>());
			groupNode.setLeaf(true);  
			groupNode.setDisplayable(true);

			newGroups.add(groupNode);
		}
		Collections.sort(newGroups);
		return newGroups;
	}
}
