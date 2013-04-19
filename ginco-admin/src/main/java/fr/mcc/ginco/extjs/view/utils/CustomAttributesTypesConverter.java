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

import fr.mcc.ginco.beans.CustomConceptAttributeType;
import fr.mcc.ginco.beans.CustomTermAttributeType;
import fr.mcc.ginco.beans.Thesaurus;
import fr.mcc.ginco.beans.generic.GenericCustomAttributeType;
import fr.mcc.ginco.extjs.view.pojo.GenericCustomAttributeTypeView;
import fr.mcc.ginco.services.ICustomConceptAttributeTypeService;
import fr.mcc.ginco.services.ICustomTermAttributeTypeService;
import fr.mcc.ginco.services.IThesaurusService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Component(value = "customAttributesTypesConverter")
public class CustomAttributesTypesConverter {

    @Inject
    @Named("customConceptAttributeTypeService")
    private ICustomConceptAttributeTypeService customConceptAttributeTypeService;

    @Inject
    @Named("thesaurusService")
    private IThesaurusService thesaurusService;

    @Inject
    @Named("customTermAttributeTypeService")
    private ICustomTermAttributeTypeService customTermAttributeTypeService;

    public GenericCustomAttributeTypeView convert(GenericCustomAttributeType source) {
        GenericCustomAttributeTypeView view = new GenericCustomAttributeTypeView();
        view.setCode(source.getCode());
        view.setIdentifier(source.getIdentifier());
        view.setThesaurusId(source.getThesaurus().getIdentifier());
        view.setValue(source.getValue());

        return view;
    }

    public List<GenericCustomAttributeTypeView> convertList(List<? extends GenericCustomAttributeType> sourceList) {
        List<GenericCustomAttributeTypeView> list = new ArrayList<GenericCustomAttributeTypeView>();
        for(GenericCustomAttributeType attributeType : sourceList) {
            list.add(convert(attributeType));
        }

        return list;
    }

    public GenericCustomAttributeType convert(GenericCustomAttributeTypeView view, boolean isConcept) {

        Thesaurus thesaurus = thesaurusService.getThesaurusById(view.getThesaurusId());

        if(isConcept) {
            GenericCustomAttributeType hibernateRes = customConceptAttributeTypeService.getAttributeTypeById(view.getIdentifier());
            if(hibernateRes == null) {
                hibernateRes = new CustomConceptAttributeType();
                hibernateRes.setThesaurus(thesaurus);
            }

            hibernateRes.setCode(view.getCode());
            hibernateRes.setValue(view.getValue());

            return hibernateRes;
        } else {
            GenericCustomAttributeType hibernateRes = customTermAttributeTypeService.getAttributeTypeById(view.getIdentifier());
            if(hibernateRes == null) {
                hibernateRes = new CustomTermAttributeType();
                hibernateRes.setThesaurus(thesaurus);
            }

            hibernateRes.setCode(view.getCode());
            hibernateRes.setValue(view.getValue());

            return hibernateRes;
        }
    }
}