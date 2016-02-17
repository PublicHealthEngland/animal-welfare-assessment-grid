package uk.gov.phe.erdst.sc.awag.businesslogic;

import java.util.List;
import java.util.Set;

import uk.gov.phe.erdst.sc.awag.datamodel.client.StudyGroupClientData;
import uk.gov.phe.erdst.sc.awag.datamodel.response.ResponsePayload;
import uk.gov.phe.erdst.sc.awag.dto.EntitySelectDto;
import uk.gov.phe.erdst.sc.awag.exceptions.AWNoSuchEntityException;
import uk.gov.phe.erdst.sc.awag.service.logging.LoggedUser;

public interface StudyGroupController
{
    void storeGroup(StudyGroupClientData clientData, ResponsePayload responsePayload, LoggedUser loggedUser);

    void updateStudyGroup(Long studyGroupId, StudyGroupClientData clientData, ResponsePayload responsePayload,
        LoggedUser loggedUser);

    List<EntitySelectDto> getStudyGroupsLikeDtos(String like, Integer offset, Integer limit,
        ResponsePayload responsePayload, boolean includeMetadata);

    StudyGroupClientData getStudyGroup(Long id) throws AWNoSuchEntityException;

    Set<StudyGroupClientData> getStudyGroupsLikeFull(String like, Integer offset, Integer limit,
        ResponsePayload responsePayload, boolean includeMetadata);

    Set<StudyGroupClientData> getStudyGroups(Integer offset, Integer limit, ResponsePayload responsePayload,
        boolean includeMetadata);

}
