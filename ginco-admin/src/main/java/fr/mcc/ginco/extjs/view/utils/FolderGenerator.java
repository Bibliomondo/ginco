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
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import fr.mcc.ginco.exceptions.BusinessException;
import fr.mcc.ginco.extjs.view.enums.ClassificationFolderType;
import fr.mcc.ginco.extjs.view.enums.ThesaurusListNodeType;
import fr.mcc.ginco.extjs.view.node.IThesaurusListNode;
import fr.mcc.ginco.extjs.view.node.ThesaurusListBasicNode;

@Component(value="folderGenerator")
public class FolderGenerator {

	@Inject
	@Named("orphansGenerator")
    private OrphansGenerator orphansGenerator;

    @Inject
    @Named("topTermGenerator")
    private TopTermGenerator topTermGenerator;
	
    /**
     * Creates categorization folders.
     * @param parentId id of top node.
     * @return created list of folders.
     */
    public List<IThesaurusListNode> generateFolders(String parentId)  throws BusinessException {
        List<IThesaurusListNode> list = new ArrayList<IThesaurusListNode>();

        IThesaurusListNode concepts = new ThesaurusListBasicNode();
        concepts.setTitle("Arborescence des concepts");
        concepts.setId(ClassificationFolderType.CONCEPTS.toString() + "_" + parentId);
        concepts.setType(ThesaurusListNodeType.FOLDER);
        concepts.setExpanded(false);
        concepts.setChildren(topTermGenerator.generateTopTerm(parentId));
        list.add(concepts);

        IThesaurusListNode sandbox = new ThesaurusListBasicNode();
        sandbox.setTitle("Bac à sable");
        sandbox.setId(ClassificationFolderType.SANDBOX.toString() + "_" + parentId);
        sandbox.setType(ThesaurusListNodeType.FOLDER);
        sandbox.setExpanded(false);
        sandbox.setChildren(new ArrayList<IThesaurusListNode>());
        list.add(sandbox);

        IThesaurusListNode orphans = new ThesaurusListBasicNode();
        orphans.setTitle("Concepts orphelins");
        orphans.setId(ClassificationFolderType.ORPHANS.toString() + "_" + parentId);
        orphans.setType(ThesaurusListNodeType.FOLDER);
        orphans.setExpanded(true);        	
        orphans.setChildren(orphansGenerator.generateOrphans(parentId));
        list.add(orphans);        

        IThesaurusListNode groups = new ThesaurusListBasicNode();
        groups.setTitle("Groupes");
        groups.setId(ClassificationFolderType.GROUPS.toString() + "_" + parentId);
        groups.setType(ThesaurusListNodeType.FOLDER);
        groups.setExpanded(false);
        groups.setChildren(new ArrayList<IThesaurusListNode>());
        list.add(groups);

        return list;
    }

}
